package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.db.ApacheDerbyJdbc;
import org.tinyjpa.jdbc.db.DbJdbc;

public class ApacheDerbyConfiguration implements DbConfiguration {

	@Override
	public DbJdbc getDbJdbc() {
		return new ApacheDerbyJdbc();
	}

}
