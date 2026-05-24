package com.company.runcoach.planning.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.UUID;

class FlywayMigrationRegressionTest {

    @Test
    void v11BackfillHandlesExistingAdaptationRows() {
        String dbName = "flyway_v11_regression_" + UUID.randomUUID();
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration", "classpath:db/migration-regression")
            .placeholders(Map.of(
                "race_goal_active_unique_columns", "user_id, status",
                "race_goal_active_unique_predicate", "",
                "adaptation_reason_codes_backfill_expr", "CONCAT('[\"', reason, '\"]')"
            ))
            .load();

        flyway.migrate();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String decisionType = jdbcTemplate.queryForObject(
            "SELECT decision_type FROM adaptation_decision WHERE id = '00000000-0000-0000-0000-000000000007'",
            String.class
        );
        String reasonCodes = jdbcTemplate.queryForObject(
            "SELECT CAST(reason_codes AS VARCHAR) FROM adaptation_decision WHERE id = '00000000-0000-0000-0000-000000000007'",
            String.class
        );
        Integer completionTableCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'WORKOUT_COMPLETION'",
            Integer.class
        );

        Assertions.assertEquals("NEAR_TERM_REGENERATION", decisionType);
        Assertions.assertNotNull(reasonCodes);
        Assertions.assertTrue(reasonCodes.contains("NO_TIME"));
        Assertions.assertEquals(1, completionTableCount);

        MigrationInfo current = flyway.info().current();
        Assertions.assertNotNull(current);
        Assertions.assertEquals("13", current.getVersion().getVersion());
    }
}
