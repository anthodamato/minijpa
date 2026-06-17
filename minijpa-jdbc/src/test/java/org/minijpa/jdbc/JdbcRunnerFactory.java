package org.minijpa.jdbc;

public class JdbcRunnerFactory {

    public JdbcRunner getJdbcRunner(String db) {
        Database database = Database.getDatabaseById(db);

        switch (database) {
            case APACHE_DERBY:
            case H2:
            case MARIADB:
            case MYSQL:
            case POSTGRES:
                return new JdbcRunner();
            case ORACLE:
                return new OracleJdbcRunner();

            default:
                break;
        }

        return null;
    }
}
