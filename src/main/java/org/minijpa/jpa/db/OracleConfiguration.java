package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.mapper.OracleDbTypeMapper;
import org.minijpa.jdbc.model.OracleSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlStatementGenerator;

public class OracleConfiguration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;
    private final JdbcRunner jdbcRunner;

    public OracleConfiguration() {
	super();
	this.dbJdbc = new OracleJdbc();
	this.dbTypeMapper = new OracleDbTypeMapper();
	this.sqlStatementGenerator = new OracleSqlStatementGenerator(dbJdbc);
	this.jdbcRunner = new OracleJdbcRunner(dbTypeMapper);
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
