package org.tinyjpa.jdbc.db;

public class ApacheDerbyConfiguration implements DbConfiguration {

	@Override
	public DbJdbc getDbJdbc() {
		return new ApacheDerbyJdbc();
	}

}
