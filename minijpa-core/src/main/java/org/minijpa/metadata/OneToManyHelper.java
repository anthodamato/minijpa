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
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.OneToManyRelationship;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;

import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import java.util.Optional;

/**
 * @author adamato
 */
public class OneToManyHelper extends RelationshipHelper {

    private final JoinColumnMappingFactory joinColumnMappingFactory = new OwningJoinColumnMappingFactory();
    private final JoinColumnMappingFactory owningJoinColumnMappingFactory = new OneToManyOwningJoinColumnMappingFactory();
    private final JoinColumnMappingFactory targetJoinColumnMappingFactory = new OneToManyTargetJoinColumnMappingFactory();

    public OneToManyRelationship createOneToMany(
            OneToMany oneToMany,
            Class<?> collectionClass,
            Class<?> targetEntity,
            JoinTable joinTable,
            Optional<JoinColumnDataList> joinColumnDataList,
            boolean id) {
        OneToManyRelationship.Builder builder = new OneToManyRelationship.Builder();
        builder = builder.withJoinColumnDataList(joinColumnDataList);

        builder.withMappedBy(getMappedBy(oneToMany));
        builder.withCascades(getCascades(oneToMany.cascade()));

        if (oneToMany.fetch() != null) {
            if (oneToMany.fetch() == FetchType.EAGER) {
                builder = builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.EAGER);
            } else if (oneToMany.fetch() == FetchType.LAZY) {
                builder = builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.LAZY);
            }
        }

        if (joinTable != null) {
            JoinTableAttributes joinTableAttributes = new JoinTableAttributes(joinTable.schema(),
                    joinTable.name());
            builder.withJoinTableAttributes(joinTableAttributes);
        }

        builder.withCollectionClass(collectionClass);
        builder.withTargetEntityClass(targetEntity);
        builder.withId(id);
        return builder.build();
    }

    private String createDefaultOneToManyJoinTable(MetaEntity owner, MetaEntity target) {
        return owner.getTableName() + "_" + target.getTableName();
    }

    private JoinTableAttributes createJoinTableAttributes(Relationship relationship,
                                                          MetaEntity entity,
                                                          MetaEntity toEntity) {
        JoinTableAttributes joinTableAttributes = relationship.getJoinTableAttributes();
        if (joinTableAttributes != null) {
            return joinTableAttributes;
        }

        String joinTableName = createDefaultOneToManyJoinTable(entity, toEntity);
        return new JoinTableAttributes(null, joinTableName);
    }

    public OneToManyRelationship finalizeRelationship(
            OneToManyRelationship oneToManyRelationship,
            RelationshipMetaAttribute a,
            MetaEntity entity,
            MetaEntity toEntity,
            DbConfiguration dbConfiguration) {
        OneToManyRelationship.Builder builder = new OneToManyRelationship.Builder().with(
                oneToManyRelationship);
        builder = builder.withAttributeType(toEntity);
        if (oneToManyRelationship.isOwner()) {
            if (oneToManyRelationship.getJoinColumnDataList().isPresent()) {
                // Unidirectional One-to-Many association using a foreign key mapping
                JoinColumnMapping joinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
                        dbConfiguration,
                        a, entity, oneToManyRelationship.getJoinColumnDataList());
                toEntity.getJoinColumnMappings().add(joinColumnMapping);
            } // if (oneToManyRelationship.getJoinColumn() == null)
            else {
                JoinTableAttributes joinTableAttributes = createJoinTableAttributes(oneToManyRelationship,
                        entity,
                        toEntity);

                JoinColumnMapping owningJoinColumnMapping = owningJoinColumnMappingFactory.buildJoinColumnMapping(
                        dbConfiguration, a, entity, oneToManyRelationship.getJoinColumnDataList());

                JoinColumnMapping targetJoinColumnMapping = targetJoinColumnMappingFactory.buildJoinColumnMapping(
                        dbConfiguration, a, toEntity, oneToManyRelationship.getJoinColumnDataList());

                RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(
                        joinTableAttributes.getSchema(),
                        joinTableAttributes.getName(), owningJoinColumnMapping, targetJoinColumnMapping, entity,
                        toEntity, entity.getId(), toEntity.getId());
                builder = builder.withJoinTable(relationshipJoinTable);
            }
        } else {
            builder = builder.withOwningEntity(toEntity);
            AbstractMetaAttribute owningAttribute = toEntity
                    .findAttributeFromPath(oneToManyRelationship.getMappedBy().get());
            builder = builder.withOwningAttribute((RelationshipMetaAttribute) owningAttribute);
            builder = builder.withTargetAttribute((RelationshipMetaAttribute) owningAttribute);
        }

        return builder.build();
    }
}
