package com.pipeline.modules.dwh.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DwhQueryService {

    private final JdbcTemplate postgresJdbc;
    private final JdbcTemplate clickHouseJdbc;

    public DwhQueryService(JdbcTemplate postgresJdbc,
                           @Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbc) {
        this.postgresJdbc = postgresJdbc;
        this.clickHouseJdbc = clickHouseJdbc;
    }

    public List<ConversionFunnelRow> queryConversionFunnel() {
        List<ConversionFunnelRow> rows = clickHouseJdbc.query(
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
                null,
                rs.getDouble("avg_score"),
                rs.getLong("total_calls")
            )
        );

        return enrichDistrictNames(rows);
    }

    public List<TopDistrictRow> queryTopDistricts() {
        List<TopDistrictRow> rows = clickHouseJdbc.query(
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
                null,
                rs.getDouble("avg_score"),
                rs.getLong("listing_count")
            )
        );

        return enrichTopDistrictNames(rows);
    }

    private List<ConversionFunnelRow> enrichDistrictNames(List<ConversionFunnelRow> rows) {
        Map<String, String> districtNames = getDistrictNames(
            rows.stream().map(ConversionFunnelRow::districtId).collect(Collectors.toSet()).stream().toList()
        );
        return rows.stream()
            .map(row -> new ConversionFunnelRow(
                row.districtId(),
                districtNames.getOrDefault(row.districtId(), row.districtId()),
                row.avgScore(),
                row.totalCalls()
            ))
            .toList();
    }

    private List<TopDistrictRow> enrichTopDistrictNames(List<TopDistrictRow> rows) {
        Map<String, String> districtNames = getDistrictNames(
            rows.stream().map(TopDistrictRow::districtId).collect(Collectors.toSet()).stream().toList()
        );
        return rows.stream()
            .map(row -> new TopDistrictRow(
                row.districtId(),
                districtNames.getOrDefault(row.districtId(), row.districtId()),
                row.avgScore(),
                row.listingCount()
            ))
            .toList();
    }

    private Map<String, String> getDistrictNames(List<String> districtIds) {
        if (districtIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = districtIds.stream().map(id -> "?").collect(Collectors.joining(", "));
        List<Map<String, Object>> rows = postgresJdbc.queryForList(
            "SELECT id, name FROM districts WHERE id IN (" + placeholders + ")",
            districtIds.toArray()
        );

        Map<String, String> districtNames = new HashMap<>();
        for (Map<String, Object> row : rows) {
            districtNames.put((String) row.get("id"), (String) row.get("name"));
        }
        return districtNames;
    }
}
