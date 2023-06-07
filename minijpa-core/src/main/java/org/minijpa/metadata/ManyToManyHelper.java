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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.ManyToManyRelationship;
import org.minijpa.jpa.model.relationship.Relationship;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;

/**
 * @author adamato
 */
public class ManyToManyHelper extends RelationshipHelper {

  private final JoinColumnMappingFactory joinColumnMappingFactory = new OwningJoinColumnMappingFactory();
  private final JoinColumnMappingFactory oneToManyJoinColumnMappingFactory = new OneToManyTargetJoinColumnMappingFactory();
  private final JoinColumnMappingFactory owningJoinColumnMappingFactory = new OneToManyOwningJoinColumnMappingFactory();

  public ManyToManyRelationship createManyToMany(
      ManyToMany manyToMany,
      Class<?> collectionClass,
      Class<?> targetEntity,
      JoinTable joinTable,
      Optional<JoinColumnDataList> joinColumnDataList) {
    ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder();
    builder = builder.withJoinColumnDataList(joinColumnDataList);

    builder.withMappedBy(getMappedBy(manyToMany));
    builder.withCascades(getCascades(manyToMany.cascade()));

    if (manyToMany.fetch() != null) {
      if (manyToMany.fetch() == FetchType.EAGER) {
        builder = builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.EAGER);
      } else if (manyToMany.fetch() == FetchType.LAZY) {
        builder = builder.withFetchType(org.minijpa.jpa.model.relationship.FetchType.LAZY);
      }
    }

    if (joinTable != null) {
      JoinTableAttributes joinTableAttributes = new JoinTableAttributes(joinTable.schema(),
          joinTable.name());
      builder.withJoinTableAttributes(joinTableAttributes);
    }

    builder = builder.withCollectionClass(collectionClass);
    builder = builder.withTargetEntityClass(targetEntity);
    return builder.build();
  }

  private String createDefaultManyToManyJoinTable(MetaEntity owner, MetaEntity target) {
    return owner.getTableName() + "_" + target.getTableName();
  }

  private JoinTableAttributes createJoinTableAttributes(
      Relationship relationship,
      MetaEntity entity,
      MetaEntity toEntity) {
    JoinTableAttributes joinTableAttributes = relationship.getJoinTableAttributes();
    if (joinTableAttributes != null) {
      return joinTableAttributes;
    }

    String joinTableName = createDefaultManyToManyJoinTable(entity, toEntity);
    return new JoinTableAttributes(null, joinTableName);
  }

  private Optional<RelationshipMetaAttribute> findBidirectionalAttribute(String owningAttributeName,
      MetaEntity toEntity) {
    List<RelationshipMetaAttribute> attributes = toEntity.getRelationshipAttributes();
    return attributes.stream().filter(
            a -> (a.getRelationship() instanceof ManyToManyRelationship)
                && ((ManyToManyRelationship) a.getRelationship()).getMappedBy().isPresent()
                && ((ManyToManyRelationship) a.getRelationship()).getMappedBy().get()
                .equals(owningAttributeName))
        .findFirst();
  }

  private RelationshipJoinTable createBidirectionalJoinTable(
      MetaEntity owningEntity,
      MetaEntity targetEntity,
      RelationshipMetaAttribute a,
      RelationshipMetaAttribute toAttribute,
      JoinTableAttributes joinTableAttributes,
      //			String joinTableAlias,
      DbConfiguration dbConfiguration,
      ManyToManyRelationship manyToManyRelationship) {
    JoinColumnMapping owningJoinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
        dbConfiguration, toAttribute, owningEntity, manyToManyRelationship.getJoinColumnDataList());

    JoinColumnMapping targetJoinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
        dbConfiguration, a, targetEntity, manyToManyRelationship.getJoinColumnDataList());

    RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(
        joinTableAttributes.getSchema(), joinTableAttributes.getName(),
        //				joinTableAlias,
        owningJoinColumnMapping,
        targetJoinColumnMapping,
        owningEntity, targetEntity, owningEntity.getId(),
        targetEntity.getId());
    return relationshipJoinTable;
  }

  public ManyToManyRelationship finalizeRelationship(
      ManyToManyRelationship manyToManyRelationship,
      RelationshipMetaAttribute a,
      MetaEntity entity,
      MetaEntity toEntity,
      DbConfiguration dbConfiguration,
      Map<String, MetaEntity> entities) {
    ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder().with(
        manyToManyRelationship);
    builder = builder.withAttributeType(toEntity);
    if (manyToManyRelationship.isOwner()) {
      JoinTableAttributes joinTableAttributes = createJoinTableAttributes(manyToManyRelationship,
          entity, toEntity);
//	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
      String joinTableAlias = null;
      // different compared to One to many

      Optional<RelationshipMetaAttribute> optionalToAttribute = findBidirectionalAttribute(
          a.getName(), toEntity);
      if (optionalToAttribute.isPresent()) {
        // bidirectional
        RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(
            entity,
            toEntity,
            a,
            optionalToAttribute.get(),
            joinTableAttributes,
            //						joinTableAlias,
            dbConfiguration, manyToManyRelationship);
        builder = builder.withJoinTable(relationshipJoinTable);
        return builder.build();
      }

      // unidirectional
      JoinColumnMapping owningJoinColumnMapping = owningJoinColumnMappingFactory.buildJoinColumnMapping(
          dbConfiguration, a, entity, manyToManyRelationship.getJoinColumnDataList());

      JoinColumnMapping targetJoinColumnMapping = oneToManyJoinColumnMappingFactory.buildJoinColumnMapping(
          dbConfiguration, a, toEntity, manyToManyRelationship.getJoinColumnDataList());

      RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(
          joinTableAttributes.getSchema(), joinTableAttributes.getName(),
          //					joinTableAlias,
          owningJoinColumnMapping,
          targetJoinColumnMapping,
          entity, toEntity, entity.getId(),
          toEntity.getId());
      builder = builder.withJoinTable(relationshipJoinTable);
      return builder.build();
    } else {
      JoinTableAttributes joinTableAttributes = createJoinTableAttributes(manyToManyRelationship,
          toEntity, entity);
//			String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
      String joinTableAlias = null;
      builder = builder.withOwningEntity(toEntity);
      builder = builder.withOwningAttribute(
          (RelationshipMetaAttribute) toEntity.getAttribute(
              manyToManyRelationship.getMappedBy().get()));
      RelationshipMetaAttribute attribute = toEntity.getRelationshipAttribute(
          manyToManyRelationship.getMappedBy().get());
      builder = builder.withTargetAttribute(attribute);

      RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(
          toEntity, entity, attribute, a, joinTableAttributes,
          //					joinTableAlias,
          dbConfiguration, manyToManyRelationship);
      builder = builder.withJoinTable(relationshipJoinTable);
      return builder.build();
    }
  }
}
