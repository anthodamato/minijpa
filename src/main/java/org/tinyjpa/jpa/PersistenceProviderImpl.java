package org.tinyjpa.jpa;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProvider;
import org.tinyjpa.jdbc.DbMetaData;
import org.tinyjpa.jpa.db.DbConfiguration;
import org.tinyjpa.jpa.db.DbConfigurationList;
import org.tinyjpa.jpa.db.PersistenceUnitPropertyActions;

public class PersistenceProviderImpl implements PersistenceProvider {
	private Logger LOG = LoggerFactory.getLogger(PersistenceProviderImpl.class);

	private void processConfiguration(PersistenceUnitInfo persistenceUnitInfo) throws SQLException, URISyntaxException,
			IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		LOG.info("processConfiguration: 0");

		new ConnectionProvider().initDriver(persistenceUnitInfo);

		Connection connection = null;
		try {
			connection = new ConnectionProvider().getConnection(persistenceUnitInfo);
			LOG.info("processConfiguration: 1");
			DbMetaData dbMetaData = new DbMetaData();
			dbMetaData.find(connection);
			LOG.info("processConfiguration: 2");
			DbConfiguration dbConfiguration = new DbMetaData().createDbConfiguration(connection);
			DbConfigurationList.getInstance().setDbConfiguration(persistenceUnitInfo, dbConfiguration);
		} catch (Exception e) {
			LOG.info("processConfiguration: Exception " + e.getClass());
			if (connection != null)
				connection.rollback();
		} finally {
			if (connection != null)
				connection.close();
		}

		LOG.info("processConfiguration: ...");
		new PersistenceUnitPropertyActions().analyze(persistenceUnitInfo);
	}

	public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
		return createEntityManagerFactory("/META-INF/persistence.xml", emName, map);
	}

	public EntityManagerFactory createEntityManagerFactory(String path, String emName,
			@SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo = null;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml(path, emName);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.info("createEntityManagerFactory(String emName");
			return null;
		}

		try {
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.info("createEntityManagerFactory(String emName: processConfiguration: " + e.getClass().getName());
		}

		return new EntityManagerFactoryImpl(persistenceUnitInfo, map);
	}

	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
			@SuppressWarnings("rawtypes") Map map) {
		if (persistenceUnitInfo == null)
			return null;

		try {
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.info("createEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo: processConfiguration: "
					+ e.getClass().getName());
		}

		return new EntityManagerFactoryImpl(persistenceUnitInfo, map);
	}

	public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
	}

	public boolean generateSchema(String persistenceUnitName, @SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml",
					persistenceUnitName);
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error("generateSchema: e.getClass()=" + e.getClass());
			return false;
		}

		return true;
	}

	public ProviderUtil getProviderUtil() {
		return null;
	}

}
