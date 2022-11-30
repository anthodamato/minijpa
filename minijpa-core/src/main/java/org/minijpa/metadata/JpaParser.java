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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.SqlResultSetMapping;

import org.minijpa.jpa.db.AttributeNameMapping;
import org.minijpa.jpa.db.ConstructorMapping;
import org.minijpa.jpa.db.EntityMapping;
import org.minijpa.jpa.db.QueryResultMapping;
import org.minijpa.jpa.db.SingleColumnMapping;
import org.minijpa.jpa.model.MetaEntity;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpaParser {

    public Optional<Map<String, QueryResultMapping>> parseQueryResultMappings(Class<?> c,
	    Map<String, MetaEntity> entities) {
	SqlResultSetMapping[] mapping = c.getAnnotationsByType(SqlResultSetMapping.class);
	if (mapping.length == 0)
	    return Optional.empty();

	Map<String, QueryResultMapping> map = new HashMap<>();
	for (SqlResultSetMapping sqlResultSetMapping : mapping) {
	    if (map.containsKey(sqlResultSetMapping.name()))
		throw new IllegalStateException("@SqlResultSetMapping '" + sqlResultSetMapping.name() + "' already declared");

	    QueryResultMapping queryResultMapping = parseQueryResultMapping(sqlResultSetMapping, entities);
	    map.put(sqlResultSetMapping.name(), queryResultMapping);
	}

	return Optional.of(map);
    }

    private QueryResultMapping parseQueryResultMapping(SqlResultSetMapping sqlResultSetMapping,
	    Map<String, MetaEntity> entities) {
	if (sqlResultSetMapping.name() == null || "".equals(sqlResultSetMapping.name()))
	    throw new IllegalArgumentException("@SqlResultSetMapping 'name' is null");

	List<EntityMapping> entityMappings = new ArrayList<>();
	for (EntityResult entityResult : sqlResultSetMapping.entities()) {
	    entityMappings.add(parseEntityResult(entityResult, entities));
	}

	List<ConstructorMapping> constructorMappings = new ArrayList<>();
	for (ConstructorResult constructorResult : sqlResultSetMapping.classes()) {
	    constructorMappings.add(parseConstructorResult(constructorResult));
	}

	List<SingleColumnMapping> singleColumnMappings = new ArrayList<>();
	for (ColumnResult columnResult : sqlResultSetMapping.columns()) {
	    singleColumnMappings.add(new SingleColumnMapping(columnResult.name(), columnResult.type()));
	}

	return new QueryResultMapping(sqlResultSetMapping.name(), Collections.unmodifiableList(entityMappings),
		Collections.unmodifiableList(constructorMappings), Collections.unmodifiableList(singleColumnMappings));
    }

    private EntityMapping parseEntityResult(EntityResult entityResult, Map<String, MetaEntity> entities) {
	Class<?> c = entityResult.entityClass();
	if (c == null)
	    throw new IllegalArgumentException("@EntityResult entity class is null");

	MetaEntity metaEntity = entities.get(c.getName());
	if (metaEntity == null)
	    throw new IllegalArgumentException("@EntityResult @Entity class '" + c.getName() + "' not found");

	List<AttributeNameMapping> attributeNameMappings = new ArrayList<>();
	FieldResult[] fieldResults = entityResult.fields();
	for (FieldResult fieldResult : fieldResults) {
	    attributeNameMappings.add(new AttributeNameMapping(fieldResult.name(), fieldResult.column()));
	}

	return new EntityMapping(metaEntity, Collections.unmodifiableList(attributeNameMappings));
    }

    private ConstructorMapping parseConstructorResult(ConstructorResult constructorResult) {
	if (constructorResult.targetClass() == null)
	    throw new IllegalArgumentException("@ConstructorResult 'targetClass' is null");

	List<SingleColumnMapping> singleColumnMappings = new ArrayList<>();
	for (ColumnResult columnResult : constructorResult.columns()) {
	    singleColumnMappings.add(new SingleColumnMapping(columnResult.name(), columnResult.type()));
	}

	return new ConstructorMapping(constructorResult.targetClass(), singleColumnMappings);
    }
}
