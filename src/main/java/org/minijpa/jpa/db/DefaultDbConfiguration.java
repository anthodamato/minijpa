package org.minijpa.jpa.db;

import org.minijpa.jdbc.mapper.DefaultDbTypeMapper;
import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;

public class DefaultDbConfiguration implements DbConfiguration {
	private DbJdbc dbJdbc;
	private DbTypeMapper dbTypeMapper;

	public DefaultDbConfiguration() {
		super();
		this.dbJdbc = new DefaultDbJdbc();
		this.dbTypeMapper = new DefaultDbTypeMapper();
	}

	@Override
	public DbJdbc getDbJdbc() {
		return dbJdbc;
	}

	@Override
	public DbTypeMapper getDbTypeMapper() {
		return dbTypeMapper;
	}

}
