package com.web.hotdeal.commons.config;

import com.web.hotdeal.deal.model.DealSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresEnumConstraintSync implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        if (!isPostgres()) {
            return;
        }

        String enumInClause = Arrays.stream(DealSource.values())
                .map(DealSource::name)
                .map(value -> "'" + value + "'")
                .collect(Collectors.joining(", "));

        syncDealsSourceConstraint(enumInClause);
        syncCrawlRunsSourceConstraint(enumInClause);
    }

    private void syncDealsSourceConstraint(String enumInClause) {
        if (!tableExists("deals")) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE deals DROP CONSTRAINT IF EXISTS deals_source_type_check");
        jdbcTemplate.execute(
                "ALTER TABLE deals ADD CONSTRAINT deals_source_type_check " +
                        "CHECK (source_type IN (" + enumInClause + "))"
        );
        log.info("Synced enum check constraint: deals_source_type_check");
    }

    private void syncCrawlRunsSourceConstraint(String enumInClause) {
        if (!tableExists("crawl_runs")) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE crawl_runs DROP CONSTRAINT IF EXISTS crawl_runs_source_check");
        jdbcTemplate.execute(
                "ALTER TABLE crawl_runs ADD CONSTRAINT crawl_runs_source_check " +
                        "CHECK (source IN (" + enumInClause + "))"
        );

        jdbcTemplate.execute("ALTER TABLE crawl_runs DROP CONSTRAINT IF EXISTS crawl_runs_requested_source_check");
        jdbcTemplate.execute(
                "ALTER TABLE crawl_runs ADD CONSTRAINT crawl_runs_requested_source_check " +
                        "CHECK (requested_source IS NULL OR requested_source IN (" + enumInClause + "))"
        );
        log.info("Synced enum check constraints: crawl_runs_source_check, crawl_runs_requested_source_check");
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT to_regclass(?) IS NOT NULL",
                Boolean.class,
                "public." + tableName
        );
        return Boolean.TRUE.equals(exists);
    }

    private boolean isPostgres() {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("postgresql");
        } catch (Exception e) {
            log.warn("Unable to detect database product name, skipping enum constraint sync", e);
            return false;
        }
    }
}
