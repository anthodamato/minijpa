package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.mapper.H2DbTypeMapper;
import org.minijpa.jdbc.model.DefaultSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlStatementGenerator;

public class H2Configuration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;

    public H2Configuration() {
	super();
	this.dbJdbc = new H2Jdbc();
	this.dbTypeMapper = new H2DbTypeMapper();
	this.sqlStatementGenerator = new DefaultSqlStatementGenerator(dbJdbc);
	this.jdbcRunner = new JpaJdbcRunner(dbTypeMapper);
    }

    @Override
    public DbJdbc getDbJdbc() {
	return dbJdbc;
    }

    @Override
    public DbTypeMapper getDbTypeMapper() {
	return dbTypeMapper;
    }

    @Override
    public SqlStatementGenerator getSqlStatementGenerator() {
	return sqlStatementGenerator;
    }

    @Override
    public JdbcRunner getJdbcRunner() {
	return jdbcRunner;
    }

}
