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
package org.minijpa.jdbc.db;

import java.util.Collections;
import java.util.List;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.PkGenerationType;
import org.minijpa.jdbc.PkStrategy;
import org.minijpa.sql.model.SqlDDLStatement;

public class MySQLJdbc extends BasicDbJdbc {

	@Override
	public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
		PkStrategy pkStrategy = super.findPkStrategy(pkGenerationType);
		if (pkStrategy == PkStrategy.SEQUENCE)
			return PkStrategy.IDENTITY;

		return pkStrategy;
	}

	@Override
	public List<SqlDDLStatement> buildDDLStatementsCreateSequences(List<MetaEntity> sorted) {
		return Collections.EMPTY_LIST;
	}

}
