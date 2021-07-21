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
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.relationship.CompositeJoinColumnMapping;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.SingleJoinColumnMapping;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class OwningJoinColumnMappingFactory implements JoinColumnMappingFactory {

    protected JoinColumnAttribute buildJoinColumnAttribute(
	    String joinColumnName,
	    DbConfiguration dbConfiguration,
	    MetaAttribute foreignKeyAttribute,
	    MetaAttribute id) {
	return new JoinColumnAttribute.Builder()
		.withColumnName(joinColumnName)
		.withType(id.getType())
		.withReadWriteDbType(id.getReadWriteDbType())
		.withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withSqlType(id.getSqlType())
		.withAttribute(foreignKeyAttribute)
		.withForeignKeyAttribute(id).build();
    }

    @Override
    public JoinColumnMapping buildSingleJoinColumnMapping(DbConfiguration dbConfiguration, MetaAttribute a,
	    MetaEntity toEntity, Optional<JoinColumnDataList> joinColumnDataList) {
	String joinColumnName = null;
	if (joinColumnDataList.isPresent()) {
	    if (joinColumnDataList.get().getJoinColumnDataList().get(0).getName().isPresent())
		joinColumnName = joinColumnDataList.get().getJoinColumnDataList().get(0).getName().get();
	} else
	    joinColumnName = createDefaultJoinColumnName(toEntity, a, toEntity.getId().getAttribute());

	JoinColumnAttribute joinColumnAttribute = buildJoinColumnAttribute(
		joinColumnName, dbConfiguration, a, toEntity.getId().getAttribute());
	return new SingleJoinColumnMapping(joinColumnAttribute, a, toEntity.getId());
    }

    @Override
    public JoinColumnMapping buildCompositeJoinColumnMapping(DbConfiguration dbConfiguration, MetaAttribute a,
	    MetaEntity toEntity, Optional<JoinColumnDataList> joinColumnDataList) {
	List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	for (MetaAttribute metaAttribute : toEntity.getId().getAttributes()) {
	    Optional<String> joinColumnName = joinColumnDataList.isPresent()
		    ? joinColumnDataList.get().getNameByReferenced(metaAttribute.getColumnName())
		    : Optional.empty();
	    if (joinColumnName.isEmpty())
		joinColumnName = Optional.of(createDefaultJoinColumnName(toEntity, a, metaAttribute));

	    JoinColumnAttribute joinColumnAttribute = buildJoinColumnAttribute(
		    joinColumnName.get(), dbConfiguration, a, metaAttribute);
	    joinColumnAttributes.add(joinColumnAttribute);
	}

	return new CompositeJoinColumnMapping(Collections.unmodifiableList(joinColumnAttributes), a, toEntity.getId());
    }

    @Override
    public JoinColumnMapping buildJoinColumnMapping(
	    DbConfiguration dbConfiguration,
	    MetaAttribute a,
	    MetaEntity toEntity,
	    Optional<JoinColumnDataList> joinColumnDataList) {
	if (toEntity.getId().isComposite())
	    return buildCompositeJoinColumnMapping(dbConfiguration, a, toEntity, joinColumnDataList);

	return buildSingleJoinColumnMapping(dbConfiguration, a, toEntity, joinColumnDataList);
    }

}
