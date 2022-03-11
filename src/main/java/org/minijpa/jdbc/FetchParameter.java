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
package org.minijpa.jdbc;

public class FetchParameter {

	private final String columnName;
	private final Class<?> type;
	private final Class<?> readWriteDbType;
	private final int sqlType;
	private final MetaAttribute attribute;
	private final MetaEntity metaEntity;
	private final boolean joinColumn;

	public FetchParameter(String columnName, Class<?> type, Class<?> readWriteDbType, int sqlType,
			MetaAttribute attribute, MetaEntity metaEntity, boolean joinColumn) {
		super();
		this.columnName = columnName;
		this.type = JdbcTypes.getWrapperClass(type);
		this.readWriteDbType = readWriteDbType;
		this.sqlType = sqlType;
		this.attribute = attribute;
		this.metaEntity = metaEntity;
		this.joinColumn = joinColumn;
	}

	public static FetchParameter build(MetaAttribute attribute) {
		return new FetchParameter(attribute.getColumnName(), attribute.getType(), attribute.getDatabaseType(),
				attribute.sqlType, attribute, null, false); // , attribute.attributeMapper);
	}

	public String getColumnName() {
		return columnName;
	}

	public Class<?> getType() {
		return type;
	}

	public Class<?> getReadWriteDbType() {
		return readWriteDbType;
	}

	public int getSqlType() {
		return sqlType;
	}

	public MetaAttribute getAttribute() {
		return attribute;
	}

	public MetaEntity getMetaEntity() {
		return metaEntity;
	}

	public boolean isJoinColumn() {
		return joinColumn;
	}

}
