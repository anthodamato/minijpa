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
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.CompositeJoinColumnMapping;
import org.minijpa.jpa.model.relationship.JoinColumnAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.SingleJoinColumnMapping;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class OwningJoinColumnMappingFactory implements JoinColumnMappingFactory {

    protected JoinColumnAttribute buildJoinColumnAttribute(
            String joinColumnName,
            DbConfiguration dbConfiguration,
            RelationshipMetaAttribute foreignKeyAttribute,
            MetaAttribute id) {
        return new JoinColumnAttribute.Builder()
                .withColumnName(joinColumnName)
                .withType(id.getType())
                .withDatabaseType(id.getDatabaseType())
                .withSqlType(id.getSqlType())
                .withAttribute(foreignKeyAttribute)
                .withForeignKeyAttribute(id).build();
    }

    @Override
    public JoinColumnMapping buildSingleJoinColumnMapping(
            DbConfiguration dbConfiguration,
            RelationshipMetaAttribute a,
            MetaEntity toEntity,
            JoinColumnDataList joinColumnDataList) {
        String joinColumnName = null;
        if (joinColumnDataList != null) {
            if (joinColumnDataList.getJoinColumnDataList().get(0).getName().isPresent()) {
                joinColumnName = joinColumnDataList.getJoinColumnDataList().get(0).getName().get();
            }
        } else {
            joinColumnName = createDefaultJoinColumnName(toEntity, a, toEntity.getId().getAttribute());
        }

        JoinColumnAttribute joinColumnAttribute = buildJoinColumnAttribute(
                joinColumnName, dbConfiguration, a, (MetaAttribute) toEntity.getId().getAttribute());
        return new SingleJoinColumnMapping(joinColumnAttribute, a, toEntity.getId());
    }

    @Override
    public JoinColumnMapping buildCompositeJoinColumnMapping(
            DbConfiguration dbConfiguration,
            RelationshipMetaAttribute a,
            MetaEntity toEntity,
            JoinColumnDataList joinColumnDataList) {
        List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
        for (MetaAttribute metaAttribute : toEntity.getId().getAttributes()) {
            Optional<String> joinColumnName = joinColumnDataList != null
                    ? joinColumnDataList.getNameByReferenced(metaAttribute.getColumnName())
                    : Optional.empty();
            if (joinColumnName.isEmpty()) {
                joinColumnName = Optional.of(createDefaultJoinColumnName(toEntity, a, metaAttribute));
            }

            JoinColumnAttribute joinColumnAttribute = buildJoinColumnAttribute(
                    joinColumnName.get(), dbConfiguration, a, metaAttribute);
            joinColumnAttributes.add(joinColumnAttribute);
        }

        return new CompositeJoinColumnMapping(Collections.unmodifiableList(joinColumnAttributes), a,
                toEntity.getId());
    }

    @Override
    public JoinColumnMapping buildJoinColumnMapping(
            DbConfiguration dbConfiguration,
            RelationshipMetaAttribute a,
            MetaEntity toEntity,
            JoinColumnDataList joinColumnDataList) {
        if (toEntity.getId().isComposite()) {
            return buildCompositeJoinColumnMapping(dbConfiguration, a, toEntity, joinColumnDataList);
        }

        return buildSingleJoinColumnMapping(dbConfiguration, a, toEntity, joinColumnDataList);
    }

}
