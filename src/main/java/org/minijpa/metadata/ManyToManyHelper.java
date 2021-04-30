/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jdbc.relationship.ManyToManyRelationship;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;

/**
 *
 * @author adamato
 */
public class ManyToManyHelper extends RelationshipHelper {

    public ManyToManyRelationship createManyToMany(ManyToMany manyToMany, JoinColumn joinColumn,
	    String joinColumnName, Class<?> collectionClass, Class<?> targetEntity, JoinTable joinTable) {
	ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder();
	if (joinColumn != null)
	    builder = builder.withJoinColumn(joinColumn.name());

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

    private JoinTableAttributes createJoinTableAttributes(Relationship relationship, MetaEntity entity, MetaEntity toEntity) {
	JoinTableAttributes joinTableAttributes = relationship.getJoinTableAttributes();
	if (joinTableAttributes != null)
	    return joinTableAttributes;

	String joinTableName = createDefaultManyToManyJoinTable(entity, toEntity);
	return new JoinTableAttributes(null, joinTableName);
    }

    private JoinColumnAttribute createJoinColumnAttribute(MetaAttribute id, MetaAttribute toAttribute,
	    String joinColumn, DbConfiguration dbConfiguration) {
	String jc = joinColumn;
	if (jc == null)
	    jc = toAttribute.getName() + "_" + id.getColumnName();

	JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper().mapJdbcAttribute(id.getType(), id.getSqlType());
	return new JoinColumnAttribute.Builder().withColumnName(jc).withType(id.getType())
		.withReadWriteDbType(id.getReadWriteDbType()).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withSqlType(id.getSqlType()).withForeignKeyAttribute(id).withJdbcAttributeMapper(jdbcAttributeMapper).build();
    }

    private List<JoinColumnAttribute> createJoinColumnAttributes(MetaEntity entity,
	    MetaAttribute toAttribute, DbConfiguration dbConfiguration) {
	List<MetaAttribute> attributes = entity.getId().getAttributes();
	List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    JoinColumnAttribute joinColumnAttribute = createJoinColumnAttribute(a, toAttribute, null, dbConfiguration);
	    joinColumnAttributes.add(joinColumnAttribute);
	}

	return joinColumnAttributes;
    }

    private Optional<MetaAttribute> findBidirectionalAttribute(String owningAttributeName, MetaEntity toEntity) {
	List<MetaAttribute> attributes = toEntity.getRelationshipAttributes();
	return attributes.stream().filter(
		a -> (a.getRelationship() instanceof ManyToManyRelationship)
		&& ((ManyToManyRelationship) a.getRelationship()).getMappedBy().isPresent()
		&& ((ManyToManyRelationship) a.getRelationship()).getMappedBy().get().equals(owningAttributeName))
		.findFirst();
    }

    private RelationshipJoinTable createBidirectionalJoinTable(MetaEntity owningEntity, MetaEntity targetEntity, MetaAttribute a, MetaAttribute toAttribute, JoinTableAttributes joinTableAttributes, String joinTableAlias, DbConfiguration dbConfiguration) {
	List<JoinColumnAttribute> joinColumnOwningAttributes = createJoinColumnAttributes(
		owningEntity, toAttribute, dbConfiguration);
	List<JoinColumnAttribute> joinColumnTargetAttributes = createJoinColumnAttributes(
		targetEntity, a, dbConfiguration);
	RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(joinTableAttributes.getSchema(), joinTableAttributes.getName(),
		joinTableAlias, joinColumnOwningAttributes, joinColumnTargetAttributes, owningEntity.getId(),
		targetEntity.getId());
	return relationshipJoinTable;
    }

    public ManyToManyRelationship finalizeRelationship(ManyToManyRelationship manyToManyRelationship, MetaAttribute a, MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration, AliasGenerator aliasGenerator, Map<String, MetaEntity> entities) {
	ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder().with(manyToManyRelationship);
	builder = builder.withAttributeType(toEntity);
	if (manyToManyRelationship.isOwner()) {
	    JoinTableAttributes joinTableAttributes = createJoinTableAttributes(manyToManyRelationship, entity, toEntity);
	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
//	    if (manyToManyRelationship.getJoinColumn() == null) {
	    // different compared to One to many

	    Optional<MetaAttribute> optionalToAttribute = findBidirectionalAttribute(a.getName(), toEntity);
	    if (optionalToAttribute.isPresent()) {
		// bidirectional
		RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(entity, toEntity, a, optionalToAttribute.get(), joinTableAttributes, joinTableAlias, dbConfiguration);
		builder = builder.withJoinTable(relationshipJoinTable);
		return builder.build();
	    }

	    // unidirectional
	    List<JoinColumnAttribute> joinColumnOwningAttributes = createUnidirectionalJoinColumnAttributes(entity, dbConfiguration);
	    List<JoinColumnAttribute> joinColumnTargetAttributes = createJoinColumnAttributes(
		    toEntity, a, dbConfiguration);
	    RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(joinTableAttributes.getSchema(), joinTableAttributes.getName(),
		    joinTableAlias, joinColumnOwningAttributes, joinColumnTargetAttributes, entity.getId(),
		    toEntity.getId());
	    builder = builder.withJoinTable(relationshipJoinTable);
	    return builder.build();
//	    } else {
//		// TODO: current implemented with just one join column, more columns could be
//		// used
//		JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
//			.withColumnName(manyToManyRelationship.getJoinColumn()).withType(entity.getId().getType())
//			.withReadWriteDbType(entity.getId().getReadWriteDbType()).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
//			.withSqlType(entity.getId().getSqlType()).withForeignKeyAttribute(entity.getId())
//			.build();
//		toEntity.getJoinColumnAttributes().add(joinColumnAttribute);
//	    }
	} else {
	    JoinTableAttributes joinTableAttributes = createJoinTableAttributes(manyToManyRelationship, toEntity, entity);
	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
	    builder = builder.withOwningEntity(toEntity);
	    builder = builder.withOwningAttribute(toEntity.getAttribute(manyToManyRelationship.getMappedBy().get()));
	    MetaAttribute attribute = toEntity.getAttribute(manyToManyRelationship.getMappedBy().get());
	    builder = builder.withTargetAttribute(attribute);
	    RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(toEntity, entity, attribute, a, joinTableAttributes, joinTableAlias, dbConfiguration);
	    builder = builder.withJoinTable(relationshipJoinTable);
	    return builder.build();
	}
    }
}
