package org.tinyjpa.jdbc.db.derby;

import org.tinyjpa.jdbc.db.DbConfiguration;
import org.tinyjpa.jdbc.db.DbJdbc;

public class ApacheDerbyConfiguration implements DbConfiguration {

	@Override
	public DbJdbc getDbJdbc() {
		return new ApacheDerbyJdbc();
	}

}
