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

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.SqlDDLStatement;

public interface DbJdbc {

	public PkStrategy findPkStrategy(PkGenerationType pkGenerationType);

	public List<SqlDDLStatement> buildDDLStatements(PersistenceUnitContext persistenceUnitContext);

	public List<SqlDDLStatement> buildDDLStatementsCreateTables(PersistenceUnitContext persistenceUnitContext,
			List<MetaEntity> sorted);

	public List<SqlDDLStatement> buildDDLStatementsCreateSequences(PersistenceUnitContext persistenceUnitContext,
			List<MetaEntity> sorted);

	public List<SqlDDLStatement> buildDDLStatementsCreateJoinTables(PersistenceUnitContext persistenceUnitContext,
			List<MetaEntity> sorted);

//	public String buildIdentityColumnDefinition(MetaAttribute metaAttribute);

//	public default int getDefaultPrecision() {
//		return 19;
//	}
//
//	public default int getDefaultScale() {
//		return 2;
//	}
//
//	public default String notEqualOperator() {
//		return "<>";
//	}
//
//	public default String equalOperator() {
//		return "=";
//	}
//
//	public default String orOperator() {
//		return "or";
//	}
//
//	public default String andOperator() {
//		return "and";
//	}
//
//	public default String notOperator() {
//		return "not";
//	}
//
//	public default String isNullOperator() {
//		return "is null";
//	}
//
//	public default String notNullOperator() {
//		return "is not null";
//	}
//
//	public default String booleanValue(Boolean value) {
//		if (value == false)
//			return falseValue();
//
//		return trueValue();
//	}
//
//	public default String trueValue() {
//		return "TRUE";
//	}
//
//	public default String falseValue() {
//		return "FALSE";
//	}
//
//	public default String trueOperator() {
//		return "= " + trueValue();
//	}
//
//	public default String falseOperator() {
//		return "= " + falseValue();
//	}
//
//	public default String emptyConjunctionOperator() {
//		return "1=1";
//	}
//
//	public default String emptyDisjunctionOperator() {
//		return "1=2";
//	}
//
//	public default String greaterThanOperator() {
//		return ">";
//	}
//
//	public default String greaterThanOrEqualToOperator() {
//		return ">=";
//	}
//
//	public default String lessThanOperator() {
//		return "<";
//	}
//
//	public default String lessThanOrEqualToOperator() {
//		return "<=";
//	}
//
//	public default String betweenOperator() {
//		return "between";
//	}
//
//	public default String likeOperator() {
//		return "like";
//	}
//
//	public default String inOperator() {
//		return "in";
//	}

}
