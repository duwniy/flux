package com.pipeline.modules.dwh.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.util.FileCopyUtils;

@Component
public class ClickHouseSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseSchemaInitializer.class);
    private final JdbcTemplate clickHouseJdbcTemplate;

    public ClickHouseSchemaInitializer(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate clickHouseJdbcTemplate) {
        this.clickHouseJdbcTemplate = clickHouseJdbcTemplate;
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initializeSchema() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:clickhouse/schema/*.sql");
            Arrays.sort(resources, (left, right) -> left.getFilename().compareTo(right.getFilename()));

            for (Resource resource : resources) {
                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    String sql = FileCopyUtils.copyToString(reader);
                    // Split SQL by semicolon in case of multiple statements
                    String[] statements = sql.split(";");
                    for (String statement : statements) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty()) {
                            clickHouseJdbcTemplate.execute(trimmed);
                        }
                    }
                    log.info("ClickHouse Cloud: Table verified/created based on file {}", resource.getFilename());
                } catch (Exception e) {
                    log.error("Failed to execute ClickHouse migration script: {}", resource.getFilename(), e);
                    throw new RuntimeException("ClickHouse migration failed for " + resource.getFilename(), e);
                }
            }
        } catch (Exception e) {
            log.error("Could not read ClickHouse migration resources", e);
            throw new RuntimeException("ClickHouse migration initialization failed", e);
        }
    }
}
