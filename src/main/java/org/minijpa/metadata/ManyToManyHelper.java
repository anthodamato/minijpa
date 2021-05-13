/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jdbc.relationship.ManyToManyRelationship;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;

/**
 *
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

	builder.withMappedBy(RelationshipHelper.getMappedBy(manyToMany));

	if (manyToMany.fetch() != null)
	    if (manyToMany.fetch() == FetchType.EAGER)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (manyToMany.fetch() == FetchType.LAZY)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

	if (joinTable != null) {
	    JoinTableAttributes joinTableAttributes = new JoinTableAttributes(joinTable.schema(), joinTable.name());
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
	if (joinTableAttributes != null)
	    return joinTableAttributes;

	String joinTableName = createDefaultManyToManyJoinTable(entity, toEntity);
	return new JoinTableAttributes(null, joinTableName);
    }

    private Optional<MetaAttribute> findBidirectionalAttribute(String owningAttributeName, MetaEntity toEntity) {
	List<MetaAttribute> attributes = toEntity.getRelationshipAttributes();
	return attributes.stream().filter(
		a -> (a.getRelationship() instanceof ManyToManyRelationship)
		&& ((ManyToManyRelationship) a.getRelationship()).getMappedBy().isPresent()
		&& ((ManyToManyRelationship) a.getRelationship()).getMappedBy().get().equals(owningAttributeName))
		.findFirst();
    }

    private RelationshipJoinTable createBidirectionalJoinTable(
	    MetaEntity owningEntity,
	    MetaEntity targetEntity,
	    MetaAttribute a,
	    MetaAttribute toAttribute,
	    JoinTableAttributes joinTableAttributes,
	    String joinTableAlias,
	    DbConfiguration dbConfiguration,
	    ManyToManyRelationship manyToManyRelationship) {
	JoinColumnMapping owningJoinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
		dbConfiguration, toAttribute, owningEntity, manyToManyRelationship.getJoinColumnDataList());

	JoinColumnMapping targetJoinColumnMapping = joinColumnMappingFactory.buildJoinColumnMapping(
		dbConfiguration, a, targetEntity, manyToManyRelationship.getJoinColumnDataList());

	RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(
		joinTableAttributes.getSchema(), joinTableAttributes.getName(),
		joinTableAlias,
		owningJoinColumnMapping,
		targetJoinColumnMapping,
		owningEntity, targetEntity, owningEntity.getId(),
		targetEntity.getId());
	return relationshipJoinTable;
    }

    public ManyToManyRelationship finalizeRelationship(ManyToManyRelationship manyToManyRelationship, MetaAttribute a, MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration, AliasGenerator aliasGenerator, Map<String, MetaEntity> entities) {
	ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder().with(manyToManyRelationship);
	builder = builder.withAttributeType(toEntity);
	if (manyToManyRelationship.isOwner()) {
	    JoinTableAttributes joinTableAttributes = createJoinTableAttributes(manyToManyRelationship, entity, toEntity);
	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
	    // different compared to One to many

	    Optional<MetaAttribute> optionalToAttribute = findBidirectionalAttribute(a.getName(), toEntity);
	    if (optionalToAttribute.isPresent()) {
		// bidirectional
		RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(
			entity,
			toEntity,
			a,
			optionalToAttribute.get(),
			joinTableAttributes, joinTableAlias, dbConfiguration, manyToManyRelationship);
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
		    joinTableAlias,
		    owningJoinColumnMapping,
		    targetJoinColumnMapping,
		    entity, toEntity, entity.getId(),
		    toEntity.getId());
	    builder = builder.withJoinTable(relationshipJoinTable);
	    return builder.build();
	} else {
	    JoinTableAttributes joinTableAttributes = createJoinTableAttributes(manyToManyRelationship, toEntity, entity);
	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
	    builder = builder.withOwningEntity(toEntity);
	    builder = builder.withOwningAttribute(toEntity.getAttribute(manyToManyRelationship.getMappedBy().get()));
	    MetaAttribute attribute = toEntity.getAttribute(manyToManyRelationship.getMappedBy().get());
	    builder = builder.withTargetAttribute(attribute);

	    RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(
		    toEntity, entity, attribute, a, joinTableAttributes, joinTableAlias, dbConfiguration, manyToManyRelationship);
	    builder = builder.withJoinTable(relationshipJoinTable);
	    return builder.build();
	}
    }
}
