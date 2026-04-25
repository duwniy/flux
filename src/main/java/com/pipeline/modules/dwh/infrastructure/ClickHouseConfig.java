package com.pipeline.modules.dwh.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Profile("!test")
@ConditionalOnProperty(name = "app.dwh.enabled", havingValue = "true")
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
        config.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setPoolName("clickhouse-pool");
        config.setConnectionTimeout(5_000);
        config.setMaxLifetime(600_000);
        config.setKeepaliveTime(60_000);
        config.addDataSourceProperty("socket_timeout", "30000");
        return new HikariDataSource(config);
    }

    @Bean(name = "clickHouseJdbcTemplate")
    public JdbcTemplate clickHouseJdbcTemplate(
            @Qualifier("clickHouseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
