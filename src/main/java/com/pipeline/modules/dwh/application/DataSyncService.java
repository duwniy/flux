package com.pipeline.modules.dwh.application;

import com.pipeline.modules.dwh.domain.InteractionEventFact;
import com.pipeline.modules.dwh.domain.ListingPerformanceFact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class DataSyncService {

    private final JdbcTemplate postgresJdbc;
    private final JdbcTemplate clickHouseJdbc;

    private Instant lastSyncVersions = Instant.EPOCH;
    private Instant lastSyncEvents = Instant.EPOCH;

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
        syncListingPerformance();
        syncInteractionEvents();
        log.info("Data sync completed: versions since {}, events since {}",
                lastSyncVersions, lastSyncEvents);
    }

    public void syncListingPerformance() {
        List<ListingPerformanceFact> facts = postgresJdbc.query(
            """
            SELECT lv.id, lv.listing_id, l.district_id, lv.price, 
                   COALESCE(lv.score, 0), lv.scoring_model_id, lv.valid_from
            FROM listing_versions lv
            JOIN listings l ON l.id = lv.listing_id
            WHERE lv.valid_from > ?
            ORDER BY lv.valid_from
            """,
            (rs, rowNum) -> new ListingPerformanceFact(
                java.util.UUID.fromString(rs.getString(1)),
                java.util.UUID.fromString(rs.getString(2)),
                rs.getString(3),
                rs.getBigDecimal(4),
                rs.getInt(5),
                rs.getString(6) != null ? java.util.UUID.fromString(rs.getString(6)) : null,
                rs.getTimestamp(7).toInstant()
            ),
            Timestamp.from(lastSyncVersions)
        );

        if (facts.isEmpty()) {
            log.debug("No new listing versions to sync");
            return;
        }

        clickHouseJdbc.batchUpdate(
            """
            INSERT INTO fact_listing_performance 
            (version_id, listing_id, district_id, price, score, model_version, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            facts.stream().map(f -> new Object[]{
                f.versionId().toString(),
                f.listingId().toString(),
                f.districtId(),
                f.price(),
                f.score(),
                f.modelVersion() != null ? f.modelVersion().toString() : null,
                Timestamp.from(f.timestamp())
            }).toList()
        );

        lastSyncVersions = facts.get(facts.size() - 1).timestamp();
        log.info("Synced {} listing performance facts", facts.size());
    }

    public void syncInteractionEvents() {
        List<InteractionEventFact> facts = postgresJdbc.query(
            """
            SELECT id, listing_id, listing_version_id, event_type, occurred_at
            FROM interaction_events
            WHERE occurred_at > ?
            ORDER BY occurred_at
            """,
            (rs, rowNum) -> new InteractionEventFact(
                java.util.UUID.fromString(rs.getString(1)),
                java.util.UUID.fromString(rs.getString(2)),
                rs.getString(3) != null ? java.util.UUID.fromString(rs.getString(3)) : null,
                rs.getString(4),
                rs.getTimestamp(5).toInstant()
            ),
            Timestamp.from(lastSyncEvents)
        );

        if (facts.isEmpty()) {
            log.debug("No new interaction events to sync");
            return;
        }

        clickHouseJdbc.batchUpdate(
            """
            INSERT INTO fact_interaction_events 
            (event_id, listing_id, version_id, event_type, occurred_at)
            VALUES (?, ?, ?, ?, ?)
            """,
            facts.stream().map(f -> new Object[]{
                f.eventId().toString(),
                f.listingId().toString(),
                f.versionId() != null ? f.versionId().toString() : null,
                f.eventType(),
                Timestamp.from(f.occurredAt())
            }).toList()
        );

        lastSyncEvents = facts.get(facts.size() - 1).occurredAt();
        log.info("Synced {} interaction event facts", facts.size());
    }

    /**
     * Конверсионная воронка: средний скоринг vs количество звонков по районам.
     */
    public List<ConversionFunnelRow> queryConversionFunnel() {
        return clickHouseJdbc.query(
            """
            SELECT 
                p.district_id,
                AVG(p.score) AS avg_score,
                COUNT(e.event_id) AS total_calls
            FROM fact_listing_performance p
            LEFT JOIN fact_interaction_events e 
                ON p.listing_id = e.listing_id AND e.event_type = 'PHONE_CLICK'
            GROUP BY p.district_id
            ORDER BY avg_score DESC
            """,
            (rs, rowNum) -> new ConversionFunnelRow(
                rs.getString("district_id"),
                rs.getDouble("avg_score"),
                rs.getLong("total_calls")
            )
        );
    }

    /**
     * Топ-5 районов по качеству объявлений.
     */
    public List<TopDistrictRow> queryTopDistricts() {
        return clickHouseJdbc.query(
            """
            SELECT 
                district_id,
                AVG(score) AS avg_score,
                COUNT(DISTINCT listing_id) AS listing_count
            FROM fact_listing_performance
            GROUP BY district_id
            ORDER BY avg_score DESC
            LIMIT 5
            """,
            (rs, rowNum) -> new TopDistrictRow(
                rs.getString("district_id"),
                rs.getDouble("avg_score"),
                rs.getLong("listing_count")
            )
        );
    }
}
