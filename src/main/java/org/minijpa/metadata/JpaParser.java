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
import org.minijpa.jdbc.AttributeNameMapping;
import org.minijpa.jdbc.ConstructorMapping;
import org.minijpa.jdbc.EntityMapping;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryResultMapping;
import org.minijpa.jdbc.SingleColumnMapping;

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
