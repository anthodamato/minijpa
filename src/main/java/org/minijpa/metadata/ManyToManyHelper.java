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
import javax.persistence.ManyToMany;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;
import org.minijpa.jdbc.relationship.ManyToManyRelationship;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;

/**
 *
 * @author adamato
 */
public class ManyToManyHelper extends RelationshipHelper {

    public ManyToManyRelationship createManyToMany(ManyToMany manyToMany, JoinColumn joinColumn,
	    String joinColumnName, Class<?> collectionClass, Class<?> targetEntity) {
	ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder();
	if (joinColumn != null)
	    builder = builder.withJoinColumn(joinColumn.name());

	if (manyToMany.mappedBy() != null && !manyToMany.mappedBy().isEmpty())
	    builder = builder.withMappedBy(manyToMany.mappedBy());

	if (manyToMany.fetch() != null)
	    if (manyToMany.fetch() == FetchType.EAGER)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (manyToMany.fetch() == FetchType.LAZY)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

	builder = builder.withCollectionClass(collectionClass);
	builder = builder.withTargetEntityClass(targetEntity);
	return builder.build();
    }

    private String createDefaultManyToManyJoinTable(MetaEntity owner, MetaEntity target) {
	return owner.getTableName() + "_" + target.getTableName();
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
	List<MetaAttribute> attributes = entity.getId().expand();
	List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    JoinColumnAttribute joinColumnAttribute = createJoinColumnAttribute(a, toAttribute, null, dbConfiguration);
	    joinColumnAttributes.add(joinColumnAttribute);
	}

	return joinColumnAttributes;
    }

    private Optional<MetaAttribute> findBidirectionalAttribute(String owningAttributeName, MetaEntity toEntity) {
	List<MetaAttribute> attributes = toEntity.getAttributes();
	return attributes.stream().filter(a -> a.getRelationship() != null && (a.getRelationship() instanceof ManyToManyRelationship) && ((ManyToManyRelationship) a.getRelationship()).getMappedBy() != null && ((ManyToManyRelationship) a.getRelationship()).getMappedBy().equals(owningAttributeName)).findFirst();
    }

    private RelationshipJoinTable createBidirectionalJoinTable(MetaEntity owningEntity, MetaEntity targetEntity, MetaAttribute a, MetaAttribute toAttribute, String joinTableName, String joinTableAlias, DbConfiguration dbConfiguration) {
	List<JoinColumnAttribute> joinColumnOwningAttributes = createJoinColumnAttributes(
		owningEntity, toAttribute, dbConfiguration);
	List<JoinColumnAttribute> joinColumnTargetAttributes = createJoinColumnAttributes(
		targetEntity, a, dbConfiguration);
	RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(joinTableName,
		joinTableAlias, joinColumnOwningAttributes, joinColumnTargetAttributes, owningEntity.getId(),
		targetEntity.getId());
	return relationshipJoinTable;
    }

    public ManyToManyRelationship finalizeRelationship(ManyToManyRelationship manyToManyRelationship, MetaAttribute a, MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration, AliasGenerator aliasGenerator, Map<String, MetaEntity> entities) {
	ManyToManyRelationship.Builder builder = new ManyToManyRelationship.Builder().with(manyToManyRelationship);
	builder = builder.withAttributeType(toEntity);
	if (manyToManyRelationship.isOwner()) {
	    String joinTableName = createDefaultManyToManyJoinTable(entity, toEntity);
	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableName, entities.values());
//	    if (manyToManyRelationship.getJoinColumn() == null) {
	    // different compared to One to many

	    Optional<MetaAttribute> optionalToAttribute = findBidirectionalAttribute(a.getName(), toEntity);
	    if (optionalToAttribute.isPresent()) {
		// bidirectional
		RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(entity, toEntity, a, optionalToAttribute.get(), joinTableName, joinTableAlias, dbConfiguration);
		builder = builder.withJoinTable(relationshipJoinTable);
		return builder.build();
	    }

	    // unidirectional
	    List<JoinColumnAttribute> joinColumnOwningAttributes = createUnidirectionalJoinColumnAttributes(entity, dbConfiguration);
	    List<JoinColumnAttribute> joinColumnTargetAttributes = createJoinColumnAttributes(
		    toEntity, a, dbConfiguration);
	    RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(joinTableName,
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
	    String joinTableName = createDefaultManyToManyJoinTable(toEntity, entity);
	    String joinTableAlias = aliasGenerator.calculateAlias(joinTableName, entities.values());
	    builder = builder.withOwningEntity(toEntity);
	    builder = builder.withOwningAttribute(toEntity.getAttribute(manyToManyRelationship.getMappedBy()));
	    MetaAttribute attribute = toEntity.getAttribute(manyToManyRelationship.getMappedBy());
	    builder = builder.withTargetAttribute(attribute);
	    RelationshipJoinTable relationshipJoinTable = createBidirectionalJoinTable(toEntity, entity, attribute, a, joinTableName, joinTableAlias, dbConfiguration);
	    builder = builder.withJoinTable(relationshipJoinTable);
	    return builder.build();
	}
    }
}
