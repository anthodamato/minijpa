/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
	Optional<AttributeNameMapping> optional = attributeNameMappings.stream().
		filter(m -> m.getColumn().equalsIgnoreCase(columnName)).findFirst();
	final String cn = optional.isPresent() ? optional.get().getName() : columnName;
	List<MetaAttribute> attributes = metaEntity.expandAllAttributes();
	return attributes.stream().filter(a -> a.getColumnName().equalsIgnoreCase(cn)).findFirst();
    }

    public Optional<JoinColumnAttribute> getJoinColumnAttribute(String columnName) {
	Optional<AttributeNameMapping> optional = attributeNameMappings.stream().
		filter(m -> m.getColumn().equalsIgnoreCase(columnName)).findFirst();
	final String cn = optional.isPresent() ? optional.get().getName() : columnName;
	return metaEntity.getJoinColumnAttributes().stream().filter(a -> a.getColumnName().equalsIgnoreCase(cn)).findFirst();
    }
}
