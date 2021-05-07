package org.minijpa.jpa.db;

import org.minijpa.jdbc.mapper.ApacheDerbyDbTypeMapper;
import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.model.DefaultSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlStatementGenerator;

public class ApacheDerbyConfiguration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;

    public ApacheDerbyConfiguration() {
	super();
	this.dbJdbc = new ApacheDerbyJdbc();
	this.dbTypeMapper = new ApacheDerbyDbTypeMapper();
	this.sqlStatementGenerator = new DefaultSqlStatementGenerator(dbJdbc);
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
