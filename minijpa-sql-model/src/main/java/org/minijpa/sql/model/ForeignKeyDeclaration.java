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
package org.minijpa.sql.model;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ForeignKeyDeclaration {

	private final JdbcJoinColumnMapping jdbcJoinColumnMapping;
	private final String referenceTable;

	public ForeignKeyDeclaration(JdbcJoinColumnMapping jdbcJoinColumnMapping, String referenceTable) {
		this.jdbcJoinColumnMapping = jdbcJoinColumnMapping;
		this.referenceTable = referenceTable;
	}

	public JdbcJoinColumnMapping getJdbcJoinColumnMapping() {
		return jdbcJoinColumnMapping;
	}

	public String getReferenceTable() {
		return referenceTable;
	}

}
