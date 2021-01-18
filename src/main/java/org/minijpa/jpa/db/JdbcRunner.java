package org.minijpa.jpa.db;

import org.minijpa.jdbc.AbstractJdbcRunner;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

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
