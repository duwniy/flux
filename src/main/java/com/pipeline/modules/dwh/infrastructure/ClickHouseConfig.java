package com.pipeline.modules.dwh.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Profile("!test")
public class ClickHouseConfig {

    @Value("${clickhouse.datasource.url:jdbc:ch://localhost:8123/default}")
    private String url;

    @Value("${clickhouse.datasource.username:default}")
    private String username;

    @Value("${clickhouse.datasource.password:}")
    private String password;

    @Bean("clickHouseDataSource")
    public DataSource clickHouseDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setPoolName("clickhouse-pool");
        config.setConnectionTimeout(5000);
        return new HikariDataSource(config);
    }

    @Bean("clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate() {
        return new JdbcTemplate(clickHouseDataSource());
    }
}
