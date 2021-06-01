package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.mapper.PostgresDbTypeMapper;
import org.minijpa.jdbc.model.PostgresSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlStatementGenerator;

public class PostgresConfiguration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;

    public PostgresConfiguration() {
	super();
	this.dbJdbc = new PostgresJdbc();
	this.dbTypeMapper = new PostgresDbTypeMapper();
	this.sqlStatementGenerator = new PostgresSqlStatementGenerator(dbJdbc);
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

}
