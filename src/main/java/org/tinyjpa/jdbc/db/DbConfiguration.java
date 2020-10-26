package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.DbTypeMapper;

public interface DbConfiguration {
	public DbJdbc getDbJdbc();

	public DbTypeMapper getDbTypeMapper();
}
