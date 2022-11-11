package org.minijpa.jdbc;

import org.minijpa.sql.model.ApacheDerbySqlStatementGenerator;
import org.minijpa.sql.model.H2SqlStatementGenerator;
import org.minijpa.sql.model.MariaDBSqlStatementGenerator;
import org.minijpa.sql.model.MySQLSqlStatementGenerator;
import org.minijpa.sql.model.OracleSqlStatementGenerator;
import org.minijpa.sql.model.PostgresSqlStatementGenerator;
import org.minijpa.sql.model.SqlStatementGenerator;

public class SqlStatementGeneratorFactory {
    public static SqlStatementGenerator getSqlStatementGenerator(Database database) {
        switch (database) {
        case APACHE_DERBY:
            return new ApacheDerbySqlStatementGenerator();
        case H2:
            return new H2SqlStatementGenerator();
        case MARIADB:
            return new MariaDBSqlStatementGenerator();
        case MYSQL:
            return new MySQLSqlStatementGenerator();
        case ORACLE:
            return new OracleSqlStatementGenerator();
        case POSTGRES:
            return new PostgresSqlStatementGenerator();

        default:
            break;
        }

        return null;
    }
}
