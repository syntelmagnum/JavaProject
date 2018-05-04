package org.testcontainers.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.delegate.AbstractDatabaseDelegate;
import org.testcontainers.exception.ConnectionCreationException;
import org.testcontainers.ext.ScriptUtils;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC database delegate
 *
 * @author Eugeny Karpov
 */
@Slf4j
public class JdbcDatabaseDelegate extends AbstractDatabaseDelegate<Statement> {

    private JdbcDatabaseContainer container;

    public JdbcDatabaseDelegate(JdbcDatabaseContainer container) {
        this.container = container;
    }

    @Override
    protected Statement createNewConnection() {
        try {
            return container.createConnection("").createStatement();
        } catch (SQLException e) {
            log.error("Could not obtain JDBC connection");
            throw new ConnectionCreationException("Could not obtain JDBC connection", e);
        }
    }


    @Override
    public void execute(String statement, String scriptPath, int lineNumber, boolean continueOnError, boolean ignoreFailedDrops) {
        try {
            boolean rowsAffected = getConnection().execute(statement);
            log.debug("{} returned as updateCount for SQL: {}", rowsAffected, statement);
        } catch (SQLException ex) {
            boolean dropStatement = statement.trim().toLowerCase().startsWith("drop");
            if (continueOnError || (dropStatement && ignoreFailedDrops)) {
                log.debug("Failed to execute SQL script statement at line {} of resource {}: {}", lineNumber, scriptPath, statement, ex);
            } else {
                throw new ScriptUtils.ScriptStatementFailedException(statement, lineNumber, scriptPath, ex);
            }
        }
    }

    @Override
    protected void closeConnectionQuietly(Statement statement) {
        try {
            statement.close();
        } catch (Exception e) {
            log.error("Could not close JDBC connection", e);
        }
    }
}
