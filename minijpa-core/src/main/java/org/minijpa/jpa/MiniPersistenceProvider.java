/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;

import org.minijpa.jpa.db.ConnectionProviderImpl;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jdbc.DbMetaData;
import org.minijpa.jpa.db.DbConfigurationFactory;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.PersistenceUnitPropertyActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniPersistenceProvider implements PersistenceProvider {

	private Logger LOG = LoggerFactory.getLogger(MiniPersistenceProvider.class);

	private void processConfiguration(PersistenceUnitInfo persistenceUnitInfo) {
		try {
			LOG.info("Processing Db Configuration...");
			LOG.debug("processConfiguration: persistenceUnitInfo={}", persistenceUnitInfo);
			Connection connection = null;
//	    try {
			new ConnectionProviderImpl(persistenceUnitInfo).init();

			connection = new ConnectionProviderImpl(persistenceUnitInfo).getConnection();
			DbMetaData dbMetaData = new DbMetaData();
//	    dbMetaData.showDatabaseMetadata(connection);
			DbConfiguration dbConfiguration = DbConfigurationFactory.create(connection);
			DbConfigurationList.getInstance().setDbConfiguration(persistenceUnitInfo.getPersistenceUnitName(),
					dbConfiguration);
//	    } catch (Exception e) {
//		LOG.error("processConfiguration: Exception " + e.getClass());
//		if (connection != null)
//		    connection.rollback();
//	    } finally {
//		if (connection != null)
//		    connection.close();
//	    }

			new PersistenceUnitPropertyActions().analyzeCreateScripts(persistenceUnitInfo);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
			throw new IllegalStateException(ex.getMessage());
//	    java.util.logging.Logger.getLogger(MiniPersistenceProvider.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public EntityManagerFactory createEntityManagerFactory(String emName, @SuppressWarnings("rawtypes") Map map) {
		return createEntityManagerFactory("/META-INF/persistence.xml", emName, map);
	}

	private EntityManagerFactory createEntityManagerFactory(String path, String emName,
			@SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo = null;
		LOG.debug("createEntityManagerFactory: emName={}", emName);
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml(path, emName, map);
			if (persistenceUnitInfo == null) {
				LOG.error("Persistence Unit '{}' not found", emName);
				return null;
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return null;
		}

		processConfiguration(persistenceUnitInfo);

		LOG.debug("createEntityManagerFactory: EntityManagerType.APPLICATION_MANAGED");
		return new MiniEntityManagerFactory(EntityManagerType.APPLICATION_MANAGED, persistenceUnitInfo, map);
	}

	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo,
			@SuppressWarnings("rawtypes") Map map) {
		if (persistenceUnitInfo == null)
			return null;

//	try {
		processConfiguration(persistenceUnitInfo);
//	} catch (Exception e) {
//	    LOG.error(e.getMessage());
//	    LOG.debug("createEntityManagerFactory(PersistenceUnitInfo persistenceUnitInfo: processConfiguration: "
//		    + e.getClass().getName());
//	}

		LOG.debug("createEntityManagerFactory: EntityManagerType.CONTAINER_MANAGED");
		return new MiniEntityManagerFactory(EntityManagerType.CONTAINER_MANAGED, persistenceUnitInfo, map);
	}

	@Override
	public void generateSchema(PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
		try {
			PersistenceUnitPropertyActions persistenceUnitPropertyActions = new PersistenceUnitPropertyActions();
			List<String> script = persistenceUnitPropertyActions.generateScriptFromMetadata(info);
			persistenceUnitPropertyActions.runScript(script, info);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error("generateSchema: e.getClass()={}", e.getClass());
		}
	}

	@Override
	public boolean generateSchema(String persistenceUnitName, @SuppressWarnings("rawtypes") Map map) {
		PersistenceUnitInfo persistenceUnitInfo;
		try {
			persistenceUnitInfo = new PersistenceProviderHelper().parseXml("/META-INF/persistence.xml",
					persistenceUnitName, map);
			processConfiguration(persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error("generateSchema: e.getClass()={}", e.getClass());
			return false;
		}

		try {
			PersistenceUnitPropertyActions persistenceUnitPropertyActions = new PersistenceUnitPropertyActions();
			List<String> script = persistenceUnitPropertyActions.generateScriptFromMetadata(persistenceUnitInfo);
			persistenceUnitPropertyActions.runScript(script, persistenceUnitInfo);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error("generateSchema: e.getClass()={}", e.getClass());
			return false;
		}

		return true;
	}

	@Override
	public ProviderUtil getProviderUtil() {
		return null;
	}

}
