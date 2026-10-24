/** This file was written by Claude Code Sonnet 5.5. */
package com.cointrail;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The throwaway PostgreSQL that database tests run against. Import it with
 * {@code @Import(TestDatabase.class)}, mark the test class
 * {@code @Testcontainers(disabledWithoutDocker = true)}, and fill the
 * database with {@link TestData#load}.
 *
 * <p>Spring reuses one application context across test classes that are set
 * up the same way, so those classes share one container instead of each
 * starting their own.</p>
 */
@TestConfiguration(proxyBeanMethods = false)
public final class TestDatabase {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16-alpine");
    }
}
