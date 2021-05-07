/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.persistence.spi.PersistenceUnitInfo;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryResultMapping;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
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
	DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitInfo.getPersistenceUnitName());
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

	PersistenceUnitContext puc = new PersistenceUnitContext(persistenceUnitInfo.getPersistenceUnitName(),
		entityMap, queryResultMappings);
	EntityDelegate.getInstance().addPersistenceUnitContext(puc);
	return puc;
    }
}
