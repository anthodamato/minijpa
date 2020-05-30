package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.db.DbJdbc;
import org.tinyjpa.jdbc.db.DefaultDbJdbc;

public class DefaultDbConfiguration implements DbConfiguration {

	@Override
	public DbJdbc getDbJdbc() {
		return new DefaultDbJdbc();
	}

}
