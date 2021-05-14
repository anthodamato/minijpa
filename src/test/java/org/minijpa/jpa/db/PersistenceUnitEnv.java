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
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jpa.MiniPersistenceContext;
import org.minijpa.jpa.PersistenceProviderHelper;
import org.minijpa.metadata.EntityContainerContext;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.jdbc.db.EntityInstanceBuilderImpl;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.metadata.MetaEntityUtils;

/**
 *
 * @author adamato
 */
public class PersistenceUnitEnv {
    
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
	PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper()
		.parseXml("/META-INF/persistence.xml", persistenceUnitName, PersistenceUnitProperties.getProperties());
	List<String> classNames = persistenceUnitInfo.getManagedClassNames();
	return MetaEntityUtils.parsePersistenceUnitContext(
		persistenceUnitName, classNames);
    }
    
    public static PersistenceUnitEnv build(DbConfiguration dbConfiguration, String persistenceUnitName) throws Exception {
	DbConfigurationList.getInstance().setDbConfiguration(persistenceUnitName, dbConfiguration);
	PersistenceUnitInfo persistenceUnitInfo = new PersistenceProviderHelper()
		.parseXml("/META-INF/persistence.xml", persistenceUnitName, PersistenceUnitProperties.getProperties());
	PersistenceUnitContext persistenceUnitContext = build(persistenceUnitName);
	MiniPersistenceContext miniPersistenceContext = new MiniPersistenceContext(persistenceUnitContext.getEntities());
	SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	EntityInstanceBuilder entityInstanceBuilder = new EntityInstanceBuilderImpl();
	ConnectionHolder connectionHolder = new ConnectionHolderImpl(new ConnectionProviderImpl(persistenceUnitInfo));
	
	Assertions.assertNotNull(dbConfiguration);
	JdbcEntityManagerImpl jdbcEntityManagerImpl = new JdbcEntityManagerImpl(dbConfiguration, persistenceUnitContext, miniPersistenceContext,
		entityInstanceBuilder, connectionHolder);
	
	new PersistenceUnitPropertyActions().analyzeCreateScripts(persistenceUnitInfo);
	EntityDelegate.getInstance().addPersistenceUnitContext(persistenceUnitContext);
	
	MetaEntityHelper metaEntityHelper = new MetaEntityHelper();
	JpaJdbcRunner jdbcRunner = new JpaJdbcRunner();
	SqlStatementGenerator sqlStatementGenerator = dbConfiguration.getSqlStatementGenerator();
	Assertions.assertNotNull(sqlStatementGenerator);
	EntityQueryLevel entityQueryLevel = new EntityQueryLevel(sqlStatementFactory,
		entityInstanceBuilder, sqlStatementGenerator, metaEntityHelper,
		jdbcRunner, connectionHolder);
	JoinTableCollectionQueryLevel joinTableCollectionQueryLevel = new JoinTableCollectionQueryLevel(
		sqlStatementFactory, sqlStatementGenerator, jdbcRunner, connectionHolder);
	ForeignKeyCollectionQueryLevel foreignKeyCollectionQueryLevel = new ForeignKeyCollectionQueryLevel(
		sqlStatementFactory, metaEntityHelper, sqlStatementGenerator, jdbcRunner, connectionHolder);
	EntityLoaderImpl entityLoader = new EntityLoaderImpl(persistenceUnitContext, entityInstanceBuilder, miniPersistenceContext,
		entityQueryLevel, foreignKeyCollectionQueryLevel, joinTableCollectionQueryLevel);
	
	EntityDelegate.getInstance()
		.addEntityManagerContext(new EntityContainerContext(persistenceUnitContext, miniPersistenceContext,
			entityLoader));
	
	PersistenceUnitEnv persistenceUnitEnv = new PersistenceUnitEnv();
	persistenceUnitEnv.setPersistenceUnitContext(persistenceUnitContext);
	persistenceUnitEnv.setJdbcEntityManager(jdbcEntityManagerImpl);
	persistenceUnitEnv.setEntityLoader(entityLoader);
	persistenceUnitEnv.setEntityContainer(miniPersistenceContext);
	persistenceUnitEnv.setConnectionHolder(connectionHolder);
	return persistenceUnitEnv;
    }
}
