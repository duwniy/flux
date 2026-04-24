package com.pipeline.modules.dwh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * В тестовом профиле используем основной H2 DataSource как "ClickHouse",
 * создавая таблицы с совместимым DDL.
 */
@Configuration
@Profile("test")
public class ClickHouseTestConfig {

    @Autowired
    private DataSource dataSource;

    @Bean("clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        // Создаём таблицы, совместимые с H2 (аналог CH DDL)
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS fact_listing_performance (
                version_id UUID,
                listing_id UUID,
                district_id VARCHAR(100),
                price DECIMAL(15,2),
                score INT,
                model_version UUID,
                timestamp TIMESTAMP
            )
        """);

        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS fact_interaction_events (
                event_id UUID,
                listing_id UUID,
                version_id UUID,
                event_type VARCHAR(50),
                occurred_at TIMESTAMP
            )
        """);

        return jdbc;
    }
}
