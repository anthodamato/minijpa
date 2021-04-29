/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;
import org.minijpa.jdbc.relationship.OneToOneRelationship;

/**
 *
 * @author adamato
 */
public class OneToOneHelper {

    public OneToOneRelationship createOneToOne(OneToOne oneToOne, JoinColumn joinColumn) {
	OneToOneRelationship.Builder builder = new OneToOneRelationship.Builder();
	if (joinColumn != null)
	    builder.withJoinColumn(joinColumn.name());

	if (oneToOne.mappedBy() != null && !oneToOne.mappedBy().isEmpty())
	    builder.withMappedBy(oneToOne.mappedBy());

	if (oneToOne.fetch() != null)
	    if (oneToOne.fetch() == FetchType.EAGER)
		builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (oneToOne.fetch() == FetchType.LAZY)
		builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

	return builder.build();
    }

    private String createDefaultJoinColumn(MetaAttribute owningAttribute, MetaEntity toEntity) {
	return owningAttribute.getName() + "_" + toEntity.getId().getAttribute().getColumnName();
    }

    public OneToOneRelationship finalizeRelationship(OneToOneRelationship oneToOneRelationship, MetaAttribute a,
	    MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration) {
	OneToOneRelationship.Builder builder = new OneToOneRelationship.Builder().with(oneToOneRelationship);
	if (oneToOneRelationship.isOwner()) {
	    String joinColumnName = oneToOneRelationship.getJoinColumn();
	    if (oneToOneRelationship.getJoinColumn() == null) {
		joinColumnName = createDefaultJoinColumn(a, toEntity);
		builder.withJoinColumn(joinColumnName);
	    }

	    JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper()
		    .mapJdbcAttribute(toEntity.getId().getType(), toEntity.getId().getAttribute().getSqlType());
	    JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
		    .withColumnName(joinColumnName)
		    .withType(toEntity.getId().getType())
		    .withReadWriteDbType(toEntity.getId().getAttribute().getReadWriteDbType())
		    .withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		    .withSqlType(toEntity.getId().getAttribute().getSqlType())
		    .withForeignKeyAttribute(a)
		    .withJdbcAttributeMapper(jdbcAttributeMapper).build();
	    entity.getJoinColumnAttributes().add(joinColumnAttribute);
	    builder.withTargetAttribute(toEntity.findAttributeByMappedBy(a.getName()));
	} else {
	    builder.withOwningEntity(toEntity);
	    builder.withOwningAttribute(toEntity.getAttribute(oneToOneRelationship.getMappedBy()));
	}

	builder.withAttributeType(toEntity);
	return builder.build();
    }
}
