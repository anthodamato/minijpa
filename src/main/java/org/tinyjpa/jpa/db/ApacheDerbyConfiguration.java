package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.DbJdbc;

public class ApacheDerbyConfiguration implements DbConfiguration {
	private ApacheDerbyJdbc apacheDerbyJdbc;

	public ApacheDerbyConfiguration() {
		super();
		this.apacheDerbyJdbc = new ApacheDerbyJdbc();
	}

	@Override
	public DbJdbc getDbJdbc() {
		return apacheDerbyJdbc;
	}

}
