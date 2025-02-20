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

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.OneToOneRelationship;

import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.util.Optional;

/**
 * @author adamato
 */
public class OneToOneHelper extends RelationshipHelper {

    private final JoinColumnMappingFactory joinColumnMappingFactory = new OwningJoinColumnMappingFactory();

    public OneToOneRelationship createOneToOne(
            OneToOne oneToOne,
            JoinColumnDataList joinColumnDataList,
            boolean id) {
        OneToOneRelationship.Builder builder = new OneToOneRelationship.Builder();
        builder.withJoinColumnDataList(joinColumnDataList);

        Optional<String> mappedBy = getMappedBy(oneToOne);
        mappedBy.ifPresent(builder::withMappedBy);
        builder.withCascades(getCascades(oneToOne.cascade()));

        if (oneToOne.fetch() != null) {
            if (oneToOne.fetch() == FetchType.EAGER) {
                builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.EAGER);
            } else if (oneToOne.fetch() == FetchType.LAZY) {
                builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.LAZY);
            }
        }

        builder.withId(id);
        return builder.build();
    }

    public OneToOneRelationship finalizeRelationship(
            OneToOneRelationship oneToOneRelationship,
            RelationshipMetaAttribute a,
            MetaEntity entity,
            MetaEntity toEntity,
            DbConfiguration dbConfiguration) {
        OneToOneRelationship.Builder builder = new OneToOneRelationship.Builder().with(
                oneToOneRelationship);
        if (oneToOneRelationship.isOwner()) {
            JoinColumnMapping joinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
                    dbConfiguration, a, toEntity, oneToOneRelationship.getJoinColumnDataList());
            entity.getJoinColumnMappings().add(joinColumnMapping);
            builder.withTargetAttribute(toEntity.findAttributeByMappedBy(a.getName()));
            builder.withJoinColumnMapping(joinColumnMapping);
        } else {
            builder.withOwningEntity(toEntity);
            builder.withOwningAttribute((RelationshipMetaAttribute) toEntity.getAttribute(
                    oneToOneRelationship.getMappedBy()));
        }

        builder.withAttributeType(toEntity);
        return builder.build();
    }
}
