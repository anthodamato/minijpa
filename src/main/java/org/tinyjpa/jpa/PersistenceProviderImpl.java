package org.tinyjpa.jpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceProviderImpl implements PersistenceProvider {
	private Logger LOG = LoggerFactory.getLogger(PersistenceProviderImpl.class);

	public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml", emName);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return null;
		}

		return createContainerEntityManagerFactory(persistenceUnitInfo, map);
	}

	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info,
			@SuppressWarnings("rawtypes") Map map) {
		return new EntityManagerFactoryImpl(info, map);
	}

	public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
	}

	public boolean generateSchema(String persistenceUnitName, @SuppressWarnings("rawtypes") Map map) {
		return false;
	}

	public ProviderUtil getProviderUtil() {
		return null;
	}

}
