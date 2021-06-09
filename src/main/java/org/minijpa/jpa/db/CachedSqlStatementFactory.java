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
package org.minijpa.jpa.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.minijpa.jdbc.AbstractAttribute;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.model.SqlInsert;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class CachedSqlStatementFactory extends SqlStatementFactory {

    private final Map<MetaEntity, Map<List<String>, SqlInsert>> insertMap = new HashMap<>();
    private final Map<MetaEntity, Map<LockType, SqlSelect>> selectByIdMap = new HashMap<>();
    private final Map<MetaEntity, Map<MetaAttribute, Map<List<String>, SqlSelect>>> selectByForeignKey = new HashMap<>();
    private final Map<MetaEntity, Map<RelationshipJoinTable, Map<List<AbstractAttribute>, SqlSelect>>> selectByJoinTable = new HashMap<>();
    private final Map<MetaEntity, Map<RelationshipJoinTable, Map<List<AbstractAttribute>, SqlSelect>>> selectByJoinTableFromTarget = new HashMap<>();

    @Override
    public SqlInsert generateInsert(MetaEntity entity, List<String> columns,
	    boolean hasIdentityColumn, boolean identityColumnNull, Optional<MetaEntity> metaEntity) throws Exception {
	Map<List<String>, SqlInsert> map = insertMap.get(entity);
	if (map != null) {
	    SqlInsert sqlInsert = map.get(columns);
	    if (sqlInsert != null)
		return sqlInsert;
	}

	SqlInsert insert = super.generateInsert(entity, columns, hasIdentityColumn, identityColumnNull, metaEntity);

	if (map == null) {
	    map = new HashMap<>();
	    insertMap.put(entity, map);
	}

	map.put(columns, insert);
	return insert;
    }

    @Override
    public SqlSelect generateSelectById(MetaEntity entity, LockType lockType) throws Exception {
	Map<LockType, SqlSelect> map = selectByIdMap.get(entity);
	if (map != null) {
	    SqlSelect sqlSelect = map.get(lockType);
	    if (sqlSelect != null)
		return sqlSelect;
	}

	SqlSelect sqlSelect = super.generateSelectById(entity, lockType);

	if (map == null) {
	    map = new HashMap<>();
	    selectByIdMap.put(entity, map);
	}

	map.put(lockType, sqlSelect);
	return sqlSelect;
    }

    @Override
    public SqlSelect generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
	    List<String> columns) throws Exception {
	Map<MetaAttribute, Map<List<String>, SqlSelect>> map = selectByForeignKey.get(entity);
	if (map != null) {
	    Map<List<String>, SqlSelect> m = map.get(foreignKeyAttribute);
	    if (m != null) {
		SqlSelect sqlSelect = m.get(columns);
		if (sqlSelect != null)
		    return sqlSelect;
	    }
	}

	SqlSelect sqlSelect = super.generateSelectByForeignKey(entity, foreignKeyAttribute, columns); //To change body of generated methods, choose Tools | Templates.
	if (map == null) {
	    map = new HashMap<>();
	    selectByForeignKey.put(entity, map);
	    Map<List<String>, SqlSelect> m = new HashMap<>();
	    map.put(foreignKeyAttribute, m);
	    m.put(columns, sqlSelect);
	} else {
	    Map<List<String>, SqlSelect> m = map.get(foreignKeyAttribute);
	    if (m != null) {
		m.put(columns, sqlSelect);
	    } else {
		m = new HashMap<>();
		map.put(foreignKeyAttribute, m);
		m.put(columns, sqlSelect);
	    }
	}

	return sqlSelect;
    }

    private SqlSelect findJoinTableCacheValue(MetaEntity entity, RelationshipJoinTable relationshipJoinTable,
	    List<AbstractAttribute> attributes,
	    Map<MetaEntity, Map<RelationshipJoinTable, Map<List<AbstractAttribute>, SqlSelect>>> cache) {
	Map<RelationshipJoinTable, Map<List<AbstractAttribute>, SqlSelect>> map = cache.get(entity);
	if (map != null) {
	    Map<List<AbstractAttribute>, SqlSelect> m = map.get(relationshipJoinTable);
	    if (m != null) {
		SqlSelect sqlSelect = m.get(attributes);
		if (sqlSelect != null)
		    return sqlSelect;
	    }
	}

	return null;
    }

    private void saveJoinTableCacheValue(MetaEntity entity, RelationshipJoinTable relationshipJoinTable,
	    List<AbstractAttribute> attributes,
	    Map<MetaEntity, Map<RelationshipJoinTable, Map<List<AbstractAttribute>, SqlSelect>>> cache,
	    SqlSelect sqlSelect) {
	Map<RelationshipJoinTable, Map<List<AbstractAttribute>, SqlSelect>> map = cache.get(entity);
	if (map == null) {
	    map = new HashMap<>();
	    cache.put(entity, map);
	    Map<List<AbstractAttribute>, SqlSelect> m = new HashMap<>();
	    map.put(relationshipJoinTable, m);
	    m.put(attributes, sqlSelect);
	} else {
	    Map<List<AbstractAttribute>, SqlSelect> m = map.get(relationshipJoinTable);
	    if (m != null) {
		m.put(attributes, sqlSelect);
	    } else {
		m = new HashMap<>();
		map.put(relationshipJoinTable, m);
		m.put(attributes, sqlSelect);
	    }
	}
    }

    @Override
    public SqlSelect generateSelectByJoinTable(MetaEntity entity, RelationshipJoinTable relationshipJoinTable,
	    List<AbstractAttribute> attributes) throws Exception {
	SqlSelect sqlSelect = findJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTable);
	if (sqlSelect != null)
	    return sqlSelect;

	sqlSelect = super.generateSelectByJoinTable(entity, relationshipJoinTable, attributes);
	saveJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTable, sqlSelect);
	return sqlSelect;
    }

    @Override
    public SqlSelect generateSelectByJoinTableFromTarget(MetaEntity entity, RelationshipJoinTable relationshipJoinTable, List<AbstractAttribute> attributes) throws Exception {
	SqlSelect sqlSelect = findJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTableFromTarget);
	if (sqlSelect != null)
	    return sqlSelect;

	sqlSelect = super.generateSelectByJoinTableFromTarget(entity, relationshipJoinTable, attributes);
	saveJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTableFromTarget, sqlSelect);
	return sqlSelect;
    }

}
