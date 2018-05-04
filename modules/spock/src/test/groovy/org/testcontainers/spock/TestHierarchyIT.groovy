package org.testcontainers.spock

import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared

/**
 * This test verifies that integration tests can subclass each other
 */
@Testcontainers
class TestHierarchyIT extends MySqlContainerIT {

    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer()
            .withDatabaseName("foo")
            .withUsername("foo")
            .withPassword("secret")

    def "both containers are running"() {
        expect:
        postgreSQLContainer.isRunning()
        mySQLContainer.isRunning()

    }

}
