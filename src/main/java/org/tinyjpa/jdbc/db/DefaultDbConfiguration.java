package org.tinyjpa.jdbc.db;

public class DefaultDbConfiguration implements DbConfiguration {

	@Override
	public DbJdbc getDbJdbc() {
		return new DefaultDbJdbc();
	}

}
