package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.DbTypeMapper;
import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.DbJdbc;

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
