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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jdbc.ConnectionProvider;
import org.minijpa.jpa.criteria.MiniCriteriaBuilder;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.JpqlModule;
import org.minijpa.jpa.db.StatementParameters;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.jpql.ParseException;
import org.minijpa.jpa.metamodel.MetamodelFactory;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniEntityManagerFactory implements EntityManagerFactory {

    private final Logger log = LoggerFactory.getLogger(MiniEntityManagerFactory.class);
    private final EntityManagerType entityManagerType;
    private final PersistenceUnitInfo persistenceUnitInfo;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map map;
    private ConnectionProvider connectionProvider;
    /**
     * The key used is the full class name.
     */
    private PersistenceUnitContext persistenceUnitContext;
    private Metamodel metamodel;
    private DbConfiguration dbConfiguration;
    private JpqlModule jpqlModule;

    public MiniEntityManagerFactory(
            EntityManagerType entityManagerType,
            PersistenceUnitInfo persistenceUnitInfo,
            @SuppressWarnings("rawtypes") Map map,
            ConnectionProvider connectionProvider) {
        super();
        this.entityManagerType = entityManagerType;
        this.persistenceUnitInfo = persistenceUnitInfo;
        this.map = map;
        this.connectionProvider = connectionProvider;
        this.dbConfiguration = DbConfigurationList.getInstance()
                .getDbConfiguration(persistenceUnitInfo.getPersistenceUnitName());
    }

    public EntityManagerType getEntityManagerType() {
        return entityManagerType;
    }

    @Override
    public EntityManager createEntityManager() {
        synchronized (persistenceUnitInfo) {
            buildPersistenceUnitContext();
        }

        return new MiniEntityManager(this, persistenceUnitInfo, persistenceUnitContext,
                connectionProvider);
    }

    private void buildPersistenceUnitContext() {
        if (persistenceUnitContext != null)
            return;


        try {
            persistenceUnitContext = PersistenceUnitContextManager.getInstance()
                    .get(persistenceUnitInfo);
        } catch (Exception e) {
            log.error("Unable to read entities: {}", e.getMessage());
            if (e instanceof InvocationTargetException) {
                InvocationTargetException targetException = (InvocationTargetException) e;
                if (targetException.getTargetException() != null) {
                    log.error(
                            "Unable to read entities: {}", targetException.getTargetException().getMessage());
                }
            }

            if (e.getStackTrace() != null) {
                log.error("stacktrace: ");
                for (StackTraceElement element : e.getStackTrace()) {
                    log.error(element.getClassName() + "." + element.getMethodName() + " - "
                            + element.getLineNumber());
                }
            }

            throw new IllegalStateException(e.getMessage());
        }

        // compiles named queries
        this.jpqlModule = new JpqlModule(dbConfiguration, persistenceUnitContext);
        if (persistenceUnitContext.getNamedQueries() != null) {
            persistenceUnitContext.getNamedQueries().forEach((k, v) -> {
                StatementParameters statementParameters = null;
                try {
                    statementParameters = jpqlModule.parse(v.getQuery(), v.getHints());
                } catch (ParseException e) {
                    throw new PersistenceException("Jpql Parser Error: " + e.getMessage());
                }
                v.setStatementParameters(statementParameters);
            });
        }
    }

    @Override
    public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map map) {
        synchronized (persistenceUnitInfo) {
            buildPersistenceUnitContext();
        }

        return new MiniEntityManager(this, persistenceUnitInfo, persistenceUnitContext,
                connectionProvider);
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType,
                                             @SuppressWarnings("rawtypes") Map map) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        synchronized (persistenceUnitInfo) {
            buildPersistenceUnitContext();
        }

        return new MiniCriteriaBuilder(getMetamodel(), persistenceUnitContext);
    }

    @Override
    public Metamodel getMetamodel() {
        log.debug("getMetamodel: metamodel={}", metamodel);
        synchronized (persistenceUnitInfo) {
            buildPersistenceUnitContext();
            if (metamodel == null) {
                try {
                    metamodel = new MetamodelFactory(persistenceUnitContext.getEntities()).build();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }

        return metamodel;
    }

    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Cache getCache() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        synchronized (persistenceUnitInfo) {
            buildPersistenceUnitContext();
        }

        String jpqlQuery = null;
        if (query instanceof MiniJpqlQuery)
            jpqlQuery = ((MiniJpqlQuery) query).getJpqlString();
        else if (query instanceof MiniJpqlTypedQuery)
            jpqlQuery = ((MiniJpqlTypedQuery) query).getJpqlString();
        else throw new IllegalArgumentException("Unknown query: " + query);

        StatementParameters statementParameters;
        try {
            statementParameters = jpqlModule.parse(jpqlQuery, null);
        } catch (ParseException e) {
            throw new PersistenceException("Jpql Parser Error: " + e.getMessage());
        }

        MiniNamedQueryMapping miniNamedQueryMapping = new MiniNamedQueryMapping(name, jpqlQuery);
        miniNamedQueryMapping.setStatementParameters(statementParameters);
        if (persistenceUnitContext.getNamedQueries() != null) {
            persistenceUnitContext.getNamedQueries().put(name, miniNamedQueryMapping);
        } else {
            persistenceUnitContext.setNamedQueries(new HashMap<>());
            persistenceUnitContext.getNamedQueries().put(name, miniNamedQueryMapping);
        }
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {
        // TODO Auto-generated method stub

    }

}
