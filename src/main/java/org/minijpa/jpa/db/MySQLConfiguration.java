package org.minijpa.jpa.db;

import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;
import org.minijpa.jdbc.mapper.MySQLDbTypeMapper;
import org.minijpa.jdbc.model.MySQLSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlStatementGenerator;

public class MySQLConfiguration implements DbConfiguration {

    private final DbJdbc dbJdbc;
    private final DbTypeMapper dbTypeMapper;
    private final SqlStatementGenerator sqlStatementGenerator;

    public MySQLConfiguration() {
	super();
	this.dbJdbc = new MySQLJdbc();
	this.dbTypeMapper = new MySQLDbTypeMapper();
	this.sqlStatementGenerator = new MySQLSqlStatementGenerator(dbJdbc);
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
