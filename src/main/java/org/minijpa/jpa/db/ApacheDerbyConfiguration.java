package org.minijpa.jpa.db;

import org.minijpa.jdbc.mapper.ApacheDerbyDbTypeMapper;
import org.minijpa.jdbc.DbTypeMapper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.DbJdbc;

public class ApacheDerbyConfiguration implements DbConfiguration {
	private DbJdbc dbJdbc;
	private DbTypeMapper dbTypeMapper;

	public ApacheDerbyConfiguration() {
		super();
		this.dbJdbc = new ApacheDerbyJdbc();
		this.dbTypeMapper = new ApacheDerbyDbTypeMapper();
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
