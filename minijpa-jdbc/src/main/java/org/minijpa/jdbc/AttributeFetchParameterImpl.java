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

import java.util.Optional;

import org.minijpa.jdbc.mapper.AttributeMapper;

public class AttributeFetchParameterImpl implements AttributeFetchParameter {

	private final String columnName;
	private final Integer sqlType;
	private final MetaAttribute attribute;

	public AttributeFetchParameterImpl(String columnName, Integer sqlType, MetaAttribute attribute) {
		super();
		this.columnName = columnName;
		this.sqlType = sqlType;
		this.attribute = attribute;
	}

	public static AttributeFetchParameter build(MetaAttribute attribute) {
		return new AttributeFetchParameterImpl(attribute.getColumnName(), attribute.sqlType, attribute);

	}

	public String getColumnName() {
		return columnName;
	}

	public Integer getSqlType() {
		return sqlType;
	}

	public MetaAttribute getAttribute() {
		return attribute;
	}

	@Override
	public Optional<AttributeMapper> getAttributeMapper() {
		return attribute.getAttributeMapper();
	}

}
