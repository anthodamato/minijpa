package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.DbTypeMapper;
import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.DbJdbc;

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
