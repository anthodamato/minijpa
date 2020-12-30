package org.tinyjpa.jpa.db;

import org.tinyjpa.jdbc.AbstractJdbcRunner;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;

public class JdbcRunner extends AbstractJdbcRunner {
	private JdbcEntityManager jdbcEntityManager;

	public JdbcRunner(JdbcEntityManager jdbcEntityManager) {
		super();
		this.jdbcEntityManager = jdbcEntityManager;
	}

	@Override
	public Object createEntityInstance(AttributeValues attributeValues, MetaEntity entity, MetaAttribute childAttribute,
			Object childAttributeValue) throws Exception {
		return jdbcEntityManager.createAndSaveEntityInstance(attributeValues, entity, childAttribute,
				childAttributeValue);
	}

}
