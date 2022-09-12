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

import java.util.Collection;
import java.util.List;

import org.minijpa.jdbc.AbstractAttribute;
import org.minijpa.jdbc.CollectionUtils;
import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.metadata.AliasGenerator;

/**
 *
 * @author adamato
 */
public class JoinTableCollectionQueryLevel implements QueryLevel {

	private final SqlStatementFactory sqlStatementFactory;
	private final DbConfiguration dbConfiguration;
	private final ConnectionHolder connectionHolder;
	private final AliasGenerator tableAliasGenerator;

	public JoinTableCollectionQueryLevel(SqlStatementFactory sqlStatementFactory, DbConfiguration dbConfiguration,
			ConnectionHolder connectionHolder, AliasGenerator tableAliasGenerator) {
		this.sqlStatementFactory = sqlStatementFactory;
		this.dbConfiguration = dbConfiguration;
		this.connectionHolder = connectionHolder;
		this.tableAliasGenerator = tableAliasGenerator;
	}

	public Object run(Object primaryKey, Pk id, Relationship relationship, MetaAttribute metaAttribute,
			EntityLoader entityLoader) throws Exception {
		ModelValueArray<AbstractAttribute> modelValueArray = null;
		SqlSelectData sqlSelectData = null;
		if (relationship.isOwner()) {
			modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
					relationship.getJoinTable().getOwningJoinColumnMapping().getJoinColumnAttributes());
			List<AbstractAttribute> attributes = modelValueArray.getModels();
			sqlSelectData = sqlStatementFactory.generateSelectByJoinTable(relationship.getAttributeType(),
					relationship.getJoinTable(), attributes, tableAliasGenerator);
		} else {
			modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(id, primaryKey,
					relationship.getJoinTable().getTargetJoinColumnMapping().getJoinColumnAttributes());
			List<AbstractAttribute> attributes = modelValueArray.getModels();
			sqlSelectData = sqlStatementFactory.generateSelectByJoinTableFromTarget(relationship.getAttributeType(),
					relationship.getJoinTable(), attributes, tableAliasGenerator);
		}

		List<QueryParameter> parameters = MetaEntityHelper.convertAbstractAVToQP(modelValueArray);
		String sql = dbConfiguration.getSqlStatementGenerator().export(sqlSelectData.getSqlSelect());
		Collection<Object> collectionResult = (Collection<Object>) CollectionUtils.createInstance(null,
				metaAttribute.getCollectionImplementationClass());
		dbConfiguration.getJdbcRunner().findCollection(connectionHolder.getConnection(), sql,
				sqlSelectData.getSqlSelect(), sqlSelectData.getFetchParameters(), LockType.NONE, collectionResult,
				entityLoader, parameters);
		return collectionResult;
	}

}
