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

import java.util.List;

import org.minijpa.jdbc.AttributeFetchParameter;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.metadata.AliasGenerator;

/**
 *
 * @author adamato
 */
public class EntityQueryLevel implements QueryLevel {

	private final SqlStatementFactory sqlStatementFactory;
	private final DbConfiguration dbConfiguration;
	private final ConnectionHolder connectionHolder;
	private final AliasGenerator tableAliasGenerator;

	public EntityQueryLevel(SqlStatementFactory sqlStatementFactory, DbConfiguration dbConfiguration,
			ConnectionHolder connectionHolder, AliasGenerator tableAliasGenerator) {
		this.sqlStatementFactory = sqlStatementFactory;
		this.dbConfiguration = dbConfiguration;
		this.connectionHolder = connectionHolder;
		this.tableAliasGenerator = tableAliasGenerator;
	}

	public ModelValueArray<FetchParameter> run(MetaEntity entity, Object primaryKey, LockType lockType)
			throws Exception {
		SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectById(entity, lockType, tableAliasGenerator);
		List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
		String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
		return dbConfiguration.getJdbcRunner().findById(sql, connectionHolder.getConnection(),
				sqlSelectData.getFetchParameters(), parameters);
	}

	public ModelValueArray<FetchParameter> runVersionQuery(MetaEntity entity, Object primaryKey,
			LockType lockType) throws Exception {
		SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectVersion(entity, lockType, tableAliasGenerator);
		List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(entity.getId(), primaryKey);
		String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData);
		return dbConfiguration.getJdbcRunner().findById(sql, connectionHolder.getConnection(),
				sqlSelectData.getFetchParameters(), parameters);
	}

}
