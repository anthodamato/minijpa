/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.List;
import java.util.Map;
import javax.persistence.spi.PersistenceUnitInfo;
import org.junit.jupiter.api.Assertions;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.ConnectionHolderImpl;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jpa.MiniPersistenceContext;
import org.minijpa.jpa.PersistenceProviderHelper;
import org.minijpa.metadata.EntityContainerContext;
import org.minijpa.metadata.EntityContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.jdbc.db.EntityInstanceBuilderImpl;
import org.minijpa.metadata.MetaEntityUtils;

/**
 *
 * @author adamato
 */
public class PersistenceUnitEnv {

    private Map<String, MetaEntity> entities;
    private JdbcEntityManager jdbcEntityManager;
    private EntityLoader entityLoader;
    private EntityContainer entityContainer;
    private ConnectionHolder connectionHolder;

    private PersistenceUnitEnv() {
    }

    public Map<String, MetaEntity> getEntities() {
	return entities;
    }

    public void setEntities(Map<String, MetaEntity> entities) {
	this.entities = entities;
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

    public static PersistenceUnitEnv build(DbConfiguration dbConfiguration, String persistenceUnitName) throws Exception {
	PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper()
		.parseXml("/META-INF/persistence.xml", persistenceUnitName);
	List<String> classNames = persistenceUnitInfo.getManagedClassNames();

	Map<String, MetaEntity> entities = MetaEntityUtils.parse(classNames);
	MiniPersistenceContext miniPersistenceContext = new MiniPersistenceContext(entities);
	SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	EntityInstanceBuilder entityInstanceBuilder = new EntityInstanceBuilderImpl();
	ConnectionHolder connectionHolder = new ConnectionHolderImpl(new ConnectionProviderImpl(persistenceUnitInfo));

	Assertions.assertNotNull(dbConfiguration);
	JdbcEntityManagerImpl jdbcEntityManagerImpl = new JdbcEntityManagerImpl(dbConfiguration, entities, miniPersistenceContext,
		entityInstanceBuilder, connectionHolder);

	SqlStatementGenerator sqlStatementGenerator = new SqlStatementGenerator(dbConfiguration.getDbJdbc());

	new PersistenceUnitPropertyActions().analyzeCreateScripts(persistenceUnitInfo);
	EntityDelegate.getInstance()
		.addEntityContext(new EntityContext(persistenceUnitInfo.getPersistenceUnitName(), entities));

	MetaEntityHelper metaEntityHelper = new MetaEntityHelper();
	JdbcRunner jdbcRunner = new JdbcRunner();
	EntityQueryLevel entityQueryLevel = new EntityQueryLevel(sqlStatementFactory,
		entityInstanceBuilder, sqlStatementGenerator, metaEntityHelper,
		jdbcRunner, connectionHolder);
	JoinTableCollectionQueryLevel joinTableCollectionQueryLevel = new JoinTableCollectionQueryLevel(
		sqlStatementFactory, sqlStatementGenerator, jdbcRunner, connectionHolder);
	ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel = new ForeignKeyCollectionQueryLevel(
		sqlStatementFactory, metaEntityHelper, sqlStatementGenerator, jdbcRunner, connectionHolder);
	EntityLoaderImpl entityLoader = new EntityLoaderImpl(entities, entityInstanceBuilder, miniPersistenceContext,
		entityQueryLevel, foreignKeyCollectionQueryLevel, joinTableCollectionQueryLevel);

	EntityDelegate.getInstance()
		.addEntityManagerContext(new EntityContainerContext(entities, miniPersistenceContext,
			entityLoader));

	PersistenceUnitEnv persistenceUnitEnv = new PersistenceUnitEnv();
	persistenceUnitEnv.setEntities(entities);
	persistenceUnitEnv.setJdbcEntityManager(jdbcEntityManagerImpl);
	persistenceUnitEnv.setEntityLoader(entityLoader);
	persistenceUnitEnv.setEntityContainer(miniPersistenceContext);
	persistenceUnitEnv.setConnectionHolder(connectionHolder);
	return persistenceUnitEnv;
    }
}
