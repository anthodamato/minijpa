/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.junit.jupiter.api.Assertions;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.ConnectionHolderImpl;
import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jpa.MiniPersistenceContext;
import org.minijpa.jpa.PersistenceProviderHelper;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.metadata.EntityContainerContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.MetaEntityUtils;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class PersistenceUnitEnv {

	private static Logger LOG = LoggerFactory.getLogger(PersistenceUnitEnv.class);
	private PersistenceUnitContext persistenceUnitContext;
	private JdbcEntityManager jdbcEntityManager;
	private EntityLoader entityLoader;
	private EntityContainer entityContainer;
	private ConnectionHolder connectionHolder;

	private PersistenceUnitEnv() {
	}

	public PersistenceUnitContext getPersistenceUnitContext() {
		return persistenceUnitContext;
	}

	public void setPersistenceUnitContext(PersistenceUnitContext persistenceUnitContext) {
		this.persistenceUnitContext = persistenceUnitContext;
	}

	public JdbcEntityManager getJdbcEntityManager() {
		return jdbcEntityManager;
	}

	public void setJdbcEntityManager(JdbcEntityManager jdbcEntityManager) {
		this.jdbcEntityManager = jdbcEntityManager;
	}

	public EntityLoader getEntityLoader() {
		return entityLoader;
	}

	public void setEntityLoader(EntityLoader entityLoader) {
		this.entityLoader = entityLoader;
	}

	public EntityContainer getEntityContainer() {
		return entityContainer;
	}

	public void setEntityContainer(EntityContainer entityContainer) {
		this.entityContainer = entityContainer;
	}

	public ConnectionHolder getConnectionHolder() {
		return connectionHolder;
	}

	public void setConnectionHolder(ConnectionHolder connectionHolder) {
		this.connectionHolder = connectionHolder;
	}

	public static PersistenceUnitContext build(String persistenceUnitName) throws Exception {
		PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml",
				persistenceUnitName, PersistenceUnitProperties.getProperties());
		LOG.debug("build: persistenceUnitInfo=" + persistenceUnitInfo);
		List<String> classNames = persistenceUnitInfo.getManagedClassNames();
		LOG.debug("build: classNames=" + classNames);
		return MetaEntityUtils.parsePersistenceUnitContext(persistenceUnitName, classNames);
	}

	public static PersistenceUnitEnv build(DbConfiguration dbConfiguration, String persistenceUnitName)
			throws Exception {
		DbConfigurationList.getInstance().setDbConfiguration(persistenceUnitName, dbConfiguration);
		PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml",
				persistenceUnitName, PersistenceUnitProperties.getProperties());
		LOG.debug("build: persistenceUnitInfo=" + persistenceUnitInfo);
		PersistenceUnitContext persistenceUnitContext = build(persistenceUnitName);
		LOG.debug("build: persistenceUnitContext=" + persistenceUnitContext);
		MiniPersistenceContext miniPersistenceContext = new MiniPersistenceContext(
				persistenceUnitContext.getEntities());
		SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();

		ConnectionProvider connectionProvider = null;
		try {
			connectionProvider = ConnectionProviderFactory.getConnectionProvider(persistenceUnitInfo);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			throw new IllegalStateException(ex.getMessage());
		}

		ConnectionHolder connectionHolder = new ConnectionHolderImpl(connectionProvider);

		Assertions.assertNotNull(dbConfiguration);
		JdbcEntityManagerImpl jdbcEntityManagerImpl = new JdbcEntityManagerImpl(dbConfiguration, persistenceUnitContext,
				miniPersistenceContext, connectionHolder);

		LOG.debug("build: jdbcEntityManagerImpl=" + jdbcEntityManagerImpl);

		new PersistenceUnitPropertyActions().analyzeCreateScripts(persistenceUnitInfo, connectionProvider);
		EntityDelegate.getInstance().addPersistenceUnitContext(persistenceUnitContext);

		EntityQueryLevel entityQueryLevel = new EntityQueryLevel(sqlStatementFactory, dbConfiguration, connectionHolder,
				persistenceUnitContext.getTableAliasGenerator());
		JoinTableCollectionQueryLevel joinTableCollectionQueryLevel = new JoinTableCollectionQueryLevel(
				sqlStatementFactory, dbConfiguration, connectionHolder,
				persistenceUnitContext.getTableAliasGenerator());
		ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel = new ForeignKeyCollectionQueryLevel(
				sqlStatementFactory, dbConfiguration, connectionHolder,
				persistenceUnitContext.getTableAliasGenerator());
		EntityLoaderImpl entityLoader = new EntityLoaderImpl(persistenceUnitContext, miniPersistenceContext,
				entityQueryLevel, foreignKeyCollectionQueryLevel, joinTableCollectionQueryLevel);

		EntityDelegate.getInstance().addEntityManagerContext(
				new EntityContainerContext(persistenceUnitContext, miniPersistenceContext, entityLoader));

		PersistenceUnitEnv persistenceUnitEnv = new PersistenceUnitEnv();
		persistenceUnitEnv.setPersistenceUnitContext(persistenceUnitContext);
		persistenceUnitEnv.setJdbcEntityManager(jdbcEntityManagerImpl);
		persistenceUnitEnv.setEntityLoader(entityLoader);
		persistenceUnitEnv.setEntityContainer(miniPersistenceContext);
		persistenceUnitEnv.setConnectionHolder(connectionHolder);
		return persistenceUnitEnv;
	}
}
