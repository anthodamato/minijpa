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
import org.minijpa.metadata.AliasGenerator;

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

	public CachedSqlStatementFactory() {
		super();
	}

	@Override
	public SqlInsert generateInsert(MetaEntity entity, List<String> columns,
			boolean hasIdentityColumn, boolean identityColumnNull, Optional<MetaEntity> metaEntity,
			AliasGenerator tableAliasGenerator) throws Exception {
		Map<List<String>, SqlInsert> map = insertMap.get(entity);
		if (map != null) {
			SqlInsert sqlInsert = map.get(columns);
			if (sqlInsert != null)
				return sqlInsert;
		}

		SqlInsert insert = super.generateInsert(entity, columns, hasIdentityColumn, identityColumnNull, metaEntity, tableAliasGenerator);

		if (map == null) {
			map = new HashMap<>();
			insertMap.put(entity, map);
		}

		map.put(columns, insert);
		return insert;
	}

	@Override
	public SqlSelect generateSelectById(MetaEntity entity, LockType lockType, AliasGenerator tableAliasGenerator) throws Exception {
		Map<LockType, SqlSelect> map = selectByIdMap.get(entity);
		if (map != null) {
			SqlSelect sqlSelect = map.get(lockType);
			if (sqlSelect != null)
				return sqlSelect;
		}

		SqlSelect sqlSelect = super.generateSelectById(entity, lockType, tableAliasGenerator);

		if (map == null) {
			map = new HashMap<>();
			selectByIdMap.put(entity, map);
		}

		map.put(lockType, sqlSelect);
		return sqlSelect;
	}

	@Override
	public SqlSelect generateSelectByForeignKey(MetaEntity entity, MetaAttribute foreignKeyAttribute,
			List<String> columns, AliasGenerator tableAliasGenerator) throws Exception {
		Map<MetaAttribute, Map<List<String>, SqlSelect>> map = selectByForeignKey.get(entity);
		if (map != null) {
			Map<List<String>, SqlSelect> m = map.get(foreignKeyAttribute);
			if (m != null) {
				SqlSelect sqlSelect = m.get(columns);
				if (sqlSelect != null)
					return sqlSelect;
			}
		}

		SqlSelect sqlSelect = super.generateSelectByForeignKey(entity, foreignKeyAttribute, columns, tableAliasGenerator);
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
	public SqlSelect generateSelectByJoinTable(
			MetaEntity entity,
			RelationshipJoinTable relationshipJoinTable,
			List<AbstractAttribute> attributes,
			AliasGenerator tableAliasGenerator) throws Exception {
		SqlSelect sqlSelect = findJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTable);
		if (sqlSelect != null)
			return sqlSelect;

		sqlSelect = super.generateSelectByJoinTable(entity, relationshipJoinTable, attributes, tableAliasGenerator);
		saveJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTable, sqlSelect);
		return sqlSelect;
	}

	@Override
	public SqlSelect generateSelectByJoinTableFromTarget(
			MetaEntity entity,
			RelationshipJoinTable relationshipJoinTable,
			List<AbstractAttribute> attributes,
			AliasGenerator tableAliasGenerator) throws Exception {
		SqlSelect sqlSelect = findJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTableFromTarget);
		if (sqlSelect != null)
			return sqlSelect;

		sqlSelect = super.generateSelectByJoinTableFromTarget(entity, relationshipJoinTable, attributes, tableAliasGenerator);
		saveJoinTableCacheValue(entity, relationshipJoinTable, attributes, selectByJoinTableFromTarget, sqlSelect);
		return sqlSelect;
	}

}
