package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.DbJdbc;

public class DefaultDbConfiguration implements DbConfiguration {

	@Override
	public DbJdbc getDbJdbc() {
		return new DefaultDbJdbc();
	}

}
