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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.Parser;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PersistenceUnitContextManager {

	private static final Logger LOG = LoggerFactory.getLogger(PersistenceUnitContextManager.class);
	private static final PersistenceUnitContextManager persistenceUnitContextManager = new PersistenceUnitContextManager();

	private PersistenceUnitContextManager() {

	}

	public static PersistenceUnitContextManager getInstance() {
		return persistenceUnitContextManager;
	}

	public synchronized PersistenceUnitContext get(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		// if the entities have been already parsed they are saved in the EntityContext.
		// It must reuse them. Just one MetaEntity instance for each class name must
		// exists.
		Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance()
				.getEntityContext(persistenceUnitInfo.getPersistenceUnitName());
		if (optional.isPresent()) {
			LOG.debug("Persistence Unit Entities already parsed");
			return optional.get();
		}

		// collects existing meta entities
		Map<String, MetaEntity> existingMetaEntities = new HashMap<>();
		for (String className : persistenceUnitInfo.getManagedClassNames()) {
			Optional<MetaEntity> optionalMetaEntity = EntityDelegate.getInstance().getMetaEntity(className);
			if (optionalMetaEntity.isPresent())
				existingMetaEntities.put(className, optionalMetaEntity.get());
		}

		LOG.info("Parsing entities...");
		Map<String, MetaEntity> entityMap = new HashMap<>();
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance()
				.getDbConfiguration(persistenceUnitInfo.getPersistenceUnitName());
		Parser parser = new Parser(dbConfiguration);
		for (String className : persistenceUnitInfo.getManagedClassNames()) {
			EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);
			MetaEntity metaEntity = parser.parse(enhEntity, entityMap.values());
			entityMap.put(enhEntity.getClassName(), metaEntity);
		}

		// replaces the existing meta entities
		entityMap.putAll(existingMetaEntities);

		parser.fillRelationships(entityMap);
		Optional<Map<String, QueryResultMapping>> queryResultMappings = parser.parseSqlResultSetMappings(entityMap);

		PersistenceUnitContext puc = new PersistenceUnitContext(persistenceUnitInfo.getPersistenceUnitName(), entityMap,
				queryResultMappings);

		entityMap.forEach((k, v) -> {
			LOG.debug("get: v.getName()={}", v.getName());
			v.getBasicAttributes().forEach(a -> LOG.debug("get: ba a.getName()={}", a.getName()));
		});

		EntityDelegate.getInstance().addPersistenceUnitContext(puc);
		return puc;
	}
}
