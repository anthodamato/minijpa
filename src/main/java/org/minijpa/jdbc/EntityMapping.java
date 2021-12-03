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

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EntityMapping implements ResultMapping {

	private final MetaEntity metaEntity;
	private final List<AttributeNameMapping> attributeNameMappings;

	public EntityMapping(MetaEntity metaEntity, List<AttributeNameMapping> attributeNameMappings) {
		this.metaEntity = metaEntity;
		this.attributeNameMappings = attributeNameMappings;
	}

	public MetaEntity getMetaEntity() {
		return metaEntity;
	}

	public List<AttributeNameMapping> getAttributeNameMappings() {
		return attributeNameMappings;
	}

	public Optional<MetaAttribute> getAttribute(String columnName) {
		List<MetaAttribute> attributes = metaEntity.expandAllAttributes();
		Optional<AttributeNameMapping> optional = attributeNameMappings.stream().
				filter(m -> m.getAlias().equalsIgnoreCase(columnName)).findFirst();
		if (optional.isPresent()) {
			Optional<MetaAttribute> oa = attributes.stream().
					filter(a -> a.getPath().equalsIgnoreCase(optional.get().getName())).findFirst();
			if (oa.isPresent())
				return oa;
		}

		return attributes.stream().filter(a -> a.getColumnName().equalsIgnoreCase(columnName)).findFirst();
	}

	public Optional<JoinColumnAttribute> getJoinColumnAttribute(String columnName) {
		List<JoinColumnAttribute> joinColumnAttributes = metaEntity.expandJoinColumnAttributes();
		Optional<AttributeNameMapping> optional = attributeNameMappings.stream().
				filter(m -> m.getAlias().equalsIgnoreCase(columnName)).findFirst();
		if (optional.isPresent()) {
			Optional<JoinColumnAttribute> o = joinColumnAttributes.stream().filter(
					j -> j.getColumnName().equalsIgnoreCase(optional.get().getName())).findFirst();
			if (o.isPresent())
				return o;
		}

		return joinColumnAttributes.stream().filter(a -> a.getColumnName().equalsIgnoreCase(columnName)).findFirst();
	}
}
