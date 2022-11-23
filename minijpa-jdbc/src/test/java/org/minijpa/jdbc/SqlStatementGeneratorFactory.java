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
        SqlStatementGenerator sqlStatementGenerator = null;
        switch (database) {
        case APACHE_DERBY:
            sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();
            sqlStatementGenerator.init();
            return sqlStatementGenerator;
        case H2:
            sqlStatementGenerator = new H2SqlStatementGenerator();
            sqlStatementGenerator.init();
            return sqlStatementGenerator;
        case MARIADB:
            sqlStatementGenerator = new MariaDBSqlStatementGenerator();
            sqlStatementGenerator.init();
            return sqlStatementGenerator;
        case MYSQL:
            sqlStatementGenerator = new MySQLSqlStatementGenerator();
            sqlStatementGenerator.init();
            return sqlStatementGenerator;
        case ORACLE:
            sqlStatementGenerator = new OracleSqlStatementGenerator();
            sqlStatementGenerator.init();
            return sqlStatementGenerator;
        case POSTGRES:
            sqlStatementGenerator = new PostgresSqlStatementGenerator();
            sqlStatementGenerator.init();
            return sqlStatementGenerator;

        default:
            break;
        }

        return null;
    }
}
