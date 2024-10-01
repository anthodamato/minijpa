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
package org.minijpa.metadata;

import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.MetaEntity;

import java.util.Map;
import java.util.Optional;

public class PersistenceUnitContext {

    private final String persistenceUnitName;
    private final Map<String, MetaEntity> entities;
    private final Map<String, QueryResultMapping> queryResultMappings;
    private Map<String, MiniNamedQueryMapping> namedQueries;
    private Map<String, MiniNamedNativeQueryMapping> namedNativeQueries;
    private AliasGenerator aliasGenerator;

    public PersistenceUnitContext(
            String persistenceUnitName,
            Map<String, MetaEntity> entities,
            Map<String, QueryResultMapping> queryResultMappings,
            Map<String, MiniNamedQueryMapping> namedQueries,
            Map<String, MiniNamedNativeQueryMapping> namedNativeQueries) {
        super();
        this.persistenceUnitName = persistenceUnitName;
        this.entities = entities;
        this.queryResultMappings = queryResultMappings;
        this.namedQueries = namedQueries;
        this.namedNativeQueries = namedNativeQueries;
    }

    public MetaEntity getEntity(String entityClassName) {
        return entities.get(entityClassName);
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public Map<String, MetaEntity> getEntities() {
        return entities;
    }

    public Map<String, QueryResultMapping> getQueryResultMappings() {
        return queryResultMappings;
    }

    public Map<String, MiniNamedQueryMapping> getNamedQueries() {
        return namedQueries;
    }

    public void setNamedQueries(Map<String, MiniNamedQueryMapping> namedQueries) {
        this.namedQueries = namedQueries;
    }

    public Map<String, MiniNamedNativeQueryMapping> getNamedNativeQueries() {
        return namedNativeQueries;
    }

    public void setNamedNativeQueries(Map<String, MiniNamedNativeQueryMapping> namedNativeQueries) {
        this.namedNativeQueries = namedNativeQueries;
    }

    public Optional<MetaEntity> findMetaEntityByName(String name) {
        for (Map.Entry<String, MetaEntity> e : entities.entrySet()) {
            if (e.getValue().getName().equals(name))
                return Optional.of(e.getValue());
        }

        return Optional.empty();
    }

    public Optional<MetaEntity> findMetaEntityByTableName(String tableName) {
        for (Map.Entry<String, MetaEntity> e : entities.entrySet()) {
            if (e.getValue().getTableName().equals(tableName))
                return Optional.of(e.getValue());
        }

        return Optional.empty();
    }

    //	public Optional<MetaEntity> findMetaEntityByAlias(String alias) {
//		for (Map.Entry<String, MetaEntity> e : entities.entrySet()) {
//			if (e.getValue().getAlias().equals(alias))
//				return Optional.of(e.getValue());
//		}
//
//		return Optional.empty();
//	}
    public AliasGenerator getAliasGenerator() {
        if (aliasGenerator == null)
            this.aliasGenerator = new TableAliasGeneratorImpl();

        return aliasGenerator;

    }

    public AliasGenerator createTableAliasGenerator() {
        return new TableAliasGeneratorImpl();
    }
}
