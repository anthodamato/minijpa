package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.mapper.MariaDBDbTypeMapper;
import org.minijpa.jdbc.model.MariaDBSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlStatementGenerator;

public class MariaDBConfiguration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;

    public MariaDBConfiguration() {
	super();
	this.dbJdbc = new MariaDBJdbc();
	this.dbTypeMapper = new MariaDBDbTypeMapper();
	this.sqlStatementGenerator = new MariaDBSqlStatementGenerator(dbJdbc);
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
