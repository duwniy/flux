package com.pipeline.modules.dwh.application;

import com.pipeline.modules.dwh.domain.InteractionEventFact;
import com.pipeline.modules.dwh.domain.ListingPerformanceFact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class DataSyncService {

    private static final String LISTING_SYNC = "listing_versions";
    private static final String EVENTS_SYNC = "interaction_events";
    private static final int CHUNK_SIZE = 1_000;

    private final JdbcTemplate postgresJdbc;
    private final JdbcTemplate clickHouseJdbc;
    private final AtomicBoolean syncRunning = new AtomicBoolean(false);

    public DataSyncService(JdbcTemplate postgresJdbc,
                           @Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbc) {
        this.postgresJdbc = postgresJdbc;
        this.clickHouseJdbc = clickHouseJdbc;
    }

    /**
     * Раз в час: ETL из Postgres → ClickHouse.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void syncAll() {
        if (!syncRunning.compareAndSet(false, true)) {
            log.warn("Sync already running, skipping trigger");
            return;
        }
        try {
            syncListingPerformance();
            syncInteractionEvents();
            log.info("Data sync completed");
        } finally {
            syncRunning.set(false);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void syncListingPerformance() {
        Instant lastSyncVersions = getCheckpoint(LISTING_SYNC);
        List<ListingPerformanceFact> facts = postgresJdbc.query(
            """
            SELECT lv.id, lv.listing_id, l.district_id, l.seller_type, lv.price,
                   l.total_area_sqm, l.price_deviation_pct, COALESCE(lv.score, 0),
                   lv.scoring_model_id, lv.valid_from
            FROM listing_versions lv
            JOIN listings l ON l.id = lv.listing_id
            WHERE lv.valid_from > ?
            ORDER BY lv.valid_from, lv.id
            """,
            (rs, rowNum) -> new ListingPerformanceFact(
                java.util.UUID.fromString(rs.getString(1)),
                java.util.UUID.fromString(rs.getString(2)),
                rs.getString(3),
                rs.getString(4),
                rs.getBigDecimal(5),
                rs.getBigDecimal(6),
                rs.getBigDecimal(7),
                rs.getInt(8),
                rs.getString(9) != null ? java.util.UUID.fromString(rs.getString(9)) : null,
                rs.getTimestamp(10).toInstant()
            ),
            Timestamp.from(lastSyncVersions)
        );

        if (facts.isEmpty()) {
            log.debug("No new listing versions to sync");
            return;
        }

        batchInsertPerformance(facts);
        Instant latestTimestamp = facts.get(facts.size() - 1).timestamp();
        updateCheckpoint(LISTING_SYNC, latestTimestamp);
        log.info("Synced {} listing performance facts", facts.size());
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void syncInteractionEvents() {
        Instant lastSyncEvents = getCheckpoint(EVENTS_SYNC);
        List<InteractionEventFact> facts = postgresJdbc.query(
            """
            SELECT id, listing_id, listing_version_id, event_type,
                   COALESCE(payload->>'sessionId', payload->>'session_id'), occurred_at
            FROM interaction_events
            WHERE occurred_at > ?
            ORDER BY occurred_at, id
            """,
            (rs, rowNum) -> new InteractionEventFact(
                java.util.UUID.fromString(rs.getString(1)),
                java.util.UUID.fromString(rs.getString(2)),
                rs.getString(3) != null ? java.util.UUID.fromString(rs.getString(3)) : null,
                rs.getString(4),
                rs.getString(5),
                rs.getTimestamp(6).toInstant()
            ),
            Timestamp.from(lastSyncEvents)
        );

        if (facts.isEmpty()) {
            log.debug("No new interaction events to sync");
            return;
        }

        batchInsertInteractionEvents(facts);
        Instant latestTimestamp = facts.get(facts.size() - 1).occurredAt();
        updateCheckpoint(EVENTS_SYNC, latestTimestamp);
        log.info("Synced {} interaction event facts", facts.size());
    }

    private void batchInsertPerformance(List<ListingPerformanceFact> facts) {
        for (int i = 0; i < facts.size(); i += CHUNK_SIZE) {
            List<ListingPerformanceFact> chunk = facts.subList(i, Math.min(i + CHUNK_SIZE, facts.size()));
            clickHouseJdbc.batchUpdate(
                """
                INSERT INTO fact_listing_performance 
                (version_id, listing_id, district_id, seller_type, price, total_area_sqm, price_deviation_pct,
                 score, model_version, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                chunk.stream().map(f -> new Object[]{
                    f.versionId().toString(),
                    f.listingId().toString(),
                    f.districtId(),
                    f.sellerType(),
                    f.price(),
                    f.totalAreaSqm(),
                    f.priceDeviationPct(),
                    f.score(),
                    f.modelVersion() != null ? f.modelVersion().toString() : null,
                    Timestamp.from(f.timestamp())
                }).toList()
            );
            log.debug("Inserted listing chunk up to {}/{}", i + chunk.size(), facts.size());
        }
    }

    private void batchInsertInteractionEvents(List<InteractionEventFact> facts) {
        for (int i = 0; i < facts.size(); i += CHUNK_SIZE) {
            List<InteractionEventFact> chunk = facts.subList(i, Math.min(i + CHUNK_SIZE, facts.size()));
            clickHouseJdbc.batchUpdate(
                """
                INSERT INTO fact_interaction_events 
                (event_id, listing_id, version_id, event_type, session_id, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                chunk.stream().map(f -> new Object[]{
                    f.eventId().toString(),
                    f.listingId().toString(),
                    f.versionId() != null ? f.versionId().toString() : null,
                    f.eventType(),
                    f.sessionId(),
                    Timestamp.from(f.occurredAt())
                }).toList()
            );
            log.debug("Inserted interaction chunk up to {}/{}", i + chunk.size(), facts.size());
        }
    }

    private Instant getCheckpoint(String syncName) {
        postgresJdbc.update(
            """
            INSERT INTO sync_checkpoints(sync_name, last_synced)
            VALUES (?, ?)
            ON CONFLICT (sync_name) DO NOTHING
            """,
            syncName,
            Timestamp.from(Instant.EPOCH)
        );

        return postgresJdbc.queryForObject(
            "SELECT last_synced FROM sync_checkpoints WHERE sync_name = ?",
            (rs, rowNum) -> rs.getTimestamp(1).toInstant(),
            syncName
        );
    }

    private void updateCheckpoint(String syncName, Instant lastSynced) {
        postgresJdbc.update(
            "UPDATE sync_checkpoints SET last_synced = ? WHERE sync_name = ?",
            Timestamp.from(lastSynced),
            syncName
        );
    }
}
