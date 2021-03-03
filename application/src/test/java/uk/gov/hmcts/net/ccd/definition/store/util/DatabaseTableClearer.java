package uk.gov.hmcts.net.ccd.definition.store.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/*
 Util component to clear all tables manually in tests.
 Inject this bean in BaseTest etc and call clearTables as part of setUp() or TearDown().
 */
@TestComponent
public class DatabaseTableClearer {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseTableClearer.class);

    private static final List<String> EXCLUDE_TABLES = List.of("flyway_schema_history");

    @Autowired
    private DataSource dataSource;

    private Connection connection;

    public void clearTables() {
        try {
            connection = dataSource.getConnection();
            tryToClearTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void tryToClearTables() throws SQLException {
        List<String> tableNames = getTableNames();
        clear(tableNames);
    }

    private List<String> getTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(
            connection.getCatalog(), null, null, new String[]{"TABLE"});

        while (rs.next()) {
            if (EXCLUDE_TABLES.contains(rs.getString("TABLE_NAME"))) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }

        return tableNames;
    }

    private void clear(List<String> tableNames) throws SQLException {
        Statement statement = buildSqlStatement(tableNames);

        LOG.debug("Executing SQL");
        statement.executeBatch();
    }

    private Statement buildSqlStatement(List<String> tableNames) throws SQLException {
        Statement statement = connection.createStatement();

        statement.addBatch(sql("SET FOREIGN_KEY_CHECKS = 0"));
        addDeleteSatements(tableNames, statement);
        statement.addBatch(sql("SET FOREIGN_KEY_CHECKS = 1"));

        return statement;
    }

    private void addDeleteSatements(List<String> tableNames, Statement statement) {
        tableNames.forEach(tableName -> {
            try {
                statement.addBatch(sql("DELETE FROM " + tableName));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String sql(String sql) {
        LOG.debug("Adding SQL: {}", sql);
        return sql;
    }
}
