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

import java.util.Collections;
import java.util.List;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.jdbc.model.SqlDDLStatement;
import org.minijpa.metadata.PersistenceUnitContext;

public class MySQLJdbc extends BasicDbJdbc {

	@Override
	public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
		PkStrategy pkStrategy = super.findPkStrategy(pkGenerationType);
		if (pkStrategy == PkStrategy.SEQUENCE)
			return PkStrategy.IDENTITY;

		return pkStrategy;
	}

	@Override
	public List<SqlDDLStatement> buildDDLStatementsCreateSequences(PersistenceUnitContext persistenceUnitContext,
			List<MetaEntity> sorted) {
		return Collections.EMPTY_LIST;
	}

//	@Override
//	public String buildColumnDefinition(Class<?> type, Optional<DDLData> ddlData) {
//		if (type == Timestamp.class || type == Calendar.class || type == LocalDateTime.class || type == Instant.class
//				|| type == ZonedDateTime.class)
//			return "datetime(6)";
//
//		return super.buildColumnDefinition(type, ddlData);
//	}

//	@Override
//	public String getFunction(SqlFunction sqlFunction) {
//		if (sqlFunction == SqlFunction.CURRENT_DATE)
//			return "CURRENT_DATE()";
//
//		if (sqlFunction == SqlFunction.CURRENT_TIME)
//			return "CURRENT_TIME()";
//
//		if (sqlFunction == SqlFunction.CURRENT_TIMESTAMP)
//			return "CURRENT_TIMESTAMP()";
//
//		return super.getFunction(sqlFunction);
//	}

}
