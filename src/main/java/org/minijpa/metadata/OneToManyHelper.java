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
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import org.minijpa.jdbc.AttributeUtil;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jdbc.relationship.OneToManyRelationship;
import org.minijpa.jdbc.relationship.Relationship;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;

/**
 *
 * @author adamato
 */
public class OneToManyHelper extends RelationshipHelper {

    public OneToManyRelationship createOneToMany(
	    OneToMany oneToMany,
	    Class<?> collectionClass,
	    Class<?> targetEntity,
	    JoinTable joinTable,
	    Optional<JoinColumnDataList> joinColumnDataList) {
	OneToManyRelationship.Builder builder = new OneToManyRelationship.Builder();
	builder = builder.withJoinColumnDataList(joinColumnDataList);

	builder.withMappedBy(RelationshipHelper.getMappedBy(oneToMany));

	if (oneToMany.fetch() != null)
	    if (oneToMany.fetch() == FetchType.EAGER)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (oneToMany.fetch() == FetchType.LAZY)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

	if (joinTable != null) {
	    JoinTableAttributes joinTableAttributes = new JoinTableAttributes(joinTable.schema(), joinTable.name());
	    builder.withJoinTableAttributes(joinTableAttributes);
	}

	builder = builder.withCollectionClass(collectionClass);
	builder = builder.withTargetEntityClass(targetEntity);
	return builder.build();
    }

    private String createDefaultOneToManyJoinColumn(MetaAttribute relationshipAttribute, MetaAttribute id) {
	return relationshipAttribute.getName() + "_" + id.getColumnName();
    }

    private JoinColumnAttribute createJoinColumnTargetAttribute(
	    MetaAttribute id,
	    MetaAttribute relationshipAttribute,
	    String joinColumn,
	    DbConfiguration dbConfiguration) {
	String jc = joinColumn;
	if (jc == null)
	    jc = createDefaultOneToManyJoinColumn(relationshipAttribute, id);

	JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper().mapJdbcAttribute(id.getType(), id.getSqlType());
	return new JoinColumnAttribute.Builder()
		.withColumnName(jc)
		.withType(id.getType())
		.withReadWriteDbType(id.getReadWriteDbType())
		.withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withSqlType(id.getSqlType())
		.withAttribute(id)
		.withJdbcAttributeMapper(jdbcAttributeMapper)
		.build();
    }

    private List<JoinColumnAttribute> createJoinColumnTargetAttributes(MetaEntity entity,
	    MetaAttribute relationshipAttribute, DbConfiguration dbConfiguration) {
	List<MetaAttribute> attributes = entity.getId().getAttributes();
	List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    JoinColumnAttribute joinColumnAttribute = createJoinColumnTargetAttribute(a, relationshipAttribute, null, dbConfiguration);
	    joinColumnAttributes.add(joinColumnAttribute);
	}

	return joinColumnAttributes;
    }

    private String createDefaultOneToManyJoinTable(MetaEntity owner, MetaEntity target) {
	return owner.getTableName() + "_" + target.getTableName();
    }

    private JoinTableAttributes createJoinTableAttributes(Relationship relationship, MetaEntity entity, MetaEntity toEntity) {
	JoinTableAttributes joinTableAttributes = relationship.getJoinTableAttributes();
	if (joinTableAttributes != null)
	    return joinTableAttributes;

	String joinTableName = createDefaultOneToManyJoinTable(entity, toEntity);
	return new JoinTableAttributes(null, joinTableName);
    }

    public OneToManyRelationship finalizeRelationship(OneToManyRelationship oneToManyRelationship,
	    MetaAttribute a, MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration,
	    AliasGenerator aliasGenerator, Map<String, MetaEntity> entities) {
	OneToManyRelationship.Builder builder = new OneToManyRelationship.Builder().with(oneToManyRelationship);
	builder = builder.withAttributeType(toEntity);
	if (oneToManyRelationship.isOwner()) {
	    if (oneToManyRelationship.getJoinColumnDataList().isPresent()) {
		// Unidirectional One-to-Many association using a foreign key mapping
		JoinColumnMapping joinColumnMapping = buildJoinColumnMapping(dbConfiguration, a, entity, oneToManyRelationship.getJoinColumnDataList());
		toEntity.getJoinColumnMappings().add(joinColumnMapping);
	    } //	    if (oneToManyRelationship.getJoinColumn() == null) 
	    else {
		JoinTableAttributes joinTableAttributes = createJoinTableAttributes(oneToManyRelationship, entity, toEntity);
		String joinTableAlias = aliasGenerator.calculateAlias(joinTableAttributes.getName(), entities.values());
		List<JoinColumnAttribute> joinColumnOwningAttributes = createUnidirectionalJoinColumnAttributes(entity, dbConfiguration);
		List<JoinColumnAttribute> joinColumnTargetAttributes = createJoinColumnTargetAttributes(
			toEntity, a, dbConfiguration);
		RelationshipJoinTable relationshipJoinTable = new RelationshipJoinTable(joinTableAttributes.getSchema(), joinTableAttributes.getName(),
			joinTableAlias, joinColumnOwningAttributes, joinColumnTargetAttributes, entity.getId(),
			toEntity.getId());
		builder = builder.withJoinTable(relationshipJoinTable);
	    }
	} else {
	    builder = builder.withOwningEntity(toEntity);
	    MetaAttribute owningAttribute = AttributeUtil.findAttributeFromPath(oneToManyRelationship.getMappedBy().get(), toEntity);
	    builder = builder.withOwningAttribute(owningAttribute);
	    builder = builder.withTargetAttribute(owningAttribute);
	}

	return builder.build();
    }
}
