package org.minijpa.jdbc.db;

import org.minijpa.jdbc.DbTypeMapper;

public interface DbConfiguration {
	public DbJdbc getDbJdbc();

	public DbTypeMapper getDbTypeMapper();
}
