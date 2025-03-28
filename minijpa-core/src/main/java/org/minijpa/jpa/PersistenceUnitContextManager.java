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

import java.util.*;

import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.JpaParser;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class PersistenceUnitContextManager {

    private static final Logger log = LoggerFactory.getLogger(PersistenceUnitContextManager.class);
    private static final PersistenceUnitContextManager persistenceUnitContextManager = new PersistenceUnitContextManager();

    private PersistenceUnitContextManager() {

    }

    public static PersistenceUnitContextManager getInstance() {
        return persistenceUnitContextManager;
    }

    public synchronized PersistenceUnitContext get(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
        // if the entities have been already parsed they are saved in the EntityContext.
        // It must reuse them. Just one MetaEntity instance for each class name must
        // exist.
        Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance()
                .getEntityContext(persistenceUnitInfo.getPersistenceUnitName());
        if (optional.isPresent()) {
            log.debug("Persistence Unit Entities already parsed");
            return optional.get();
        }

        // collects existing meta entities
        Map<String, MetaEntity> existingMetaEntities = new HashMap<>();
        for (String className : persistenceUnitInfo.getManagedClassNames()) {
            Optional<MetaEntity> optionalMetaEntity = EntityDelegate.getInstance().getMetaEntity(className);
            optionalMetaEntity.ifPresent(metaEntity -> existingMetaEntities.put(className, metaEntity));
        }

        log.info("Parsing entities...");
        Map<String, MetaEntity> entityMap = new HashMap<>();
        DbConfiguration dbConfiguration = DbConfigurationList.getInstance()
                .getDbConfiguration(persistenceUnitInfo.getPersistenceUnitName());
        Set<EnhEntity> enhEntities = new HashSet<>();
        for (String className : persistenceUnitInfo.getManagedClassNames()) {
            EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);
            enhEntities.add(enhEntity);
        }

        // before parsing, it has to add missing methods in the IdClass value classes as
        // they can be nested in case of reference to entities with composite primary key
        BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().finalizeEnhancement();

        JpaParser jpaParser = new JpaParser(dbConfiguration);
        for (EnhEntity enhEntity : enhEntities) {
            MetaEntity metaEntity = jpaParser.parse(enhEntity, entityMap.values());
            entityMap.put(enhEntity.getClassName(), metaEntity);
        }

        // adds the existing meta entities
        entityMap.putAll(existingMetaEntities);

        jpaParser.fillRelationships(entityMap);
        Optional<Map<String, QueryResultMapping>> queryResultMappings = jpaParser.parseSqlResultSetMappings(entityMap);
        Optional<Map<String, MiniNamedQueryMapping>> optionalNamedQueries = jpaParser.parseNamedQueries(entityMap);
        Optional<Map<String, MiniNamedNativeQueryMapping>> optionalNamedNativeQueries = jpaParser.parseNamedNativeQueries(entityMap);

        PersistenceUnitContext puc = new PersistenceUnitContext(
                persistenceUnitInfo.getPersistenceUnitName(),
                entityMap,
                queryResultMappings.orElse(null),
                optionalNamedQueries.orElse(null),
                optionalNamedNativeQueries.orElse(null));

        entityMap.forEach((k, v) -> {
            log.debug("Building Persistence Unit Context -> Entity Name = {}", v.getName());
            v.getBasicAttributes().forEach(a -> log.debug("Building Persistence Unit Context -> Attribute Name = {}", a.getName()));
        });

        EntityDelegate.getInstance().addPersistenceUnitContext(puc);
        return puc;
    }
}
