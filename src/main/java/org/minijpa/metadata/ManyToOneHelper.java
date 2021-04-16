/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;
import org.minijpa.jdbc.relationship.ManyToOneRelationship;

/**
 *
 * @author adamato
 */
public class ManyToOneHelper {

    public ManyToOneRelationship createManyToOne(javax.persistence.ManyToOne manyToOne, JoinColumn joinColumn) {
	ManyToOneRelationship.Builder builder = new ManyToOneRelationship.Builder();
	if (joinColumn != null)
	    builder = builder.withJoinColumn(joinColumn.name());

	if (manyToOne.fetch() != null)
	    if (manyToOne.fetch() == FetchType.EAGER)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.EAGER);
	    else if (manyToOne.fetch() == FetchType.LAZY)
		builder = builder.withFetchType(org.minijpa.jdbc.relationship.FetchType.LAZY);

	return builder.build();
    }

    private String createDefaultJoinColumn(MetaAttribute owningAttribute, MetaEntity toEntity) {
	return owningAttribute.getName() + "_" + toEntity.getId().getColumnName();
    }

    public ManyToOneRelationship finalizeRelationship(ManyToOneRelationship manyToOneRelationship, MetaAttribute a, MetaEntity entity, MetaEntity toEntity, DbConfiguration dbConfiguration) {
	ManyToOneRelationship.Builder builder = new ManyToOneRelationship.Builder().with(manyToOneRelationship);
	if (manyToOneRelationship.isOwner() && manyToOneRelationship.getJoinColumn() == null) {
	    String joinColumnName = createDefaultJoinColumn(a, toEntity);
	    builder = builder.withJoinColumn(joinColumnName);

	    JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper().mapJdbcAttribute(toEntity.getId().getType(), toEntity.getId().getSqlType());
	    JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute.Builder()
		    .withColumnName(joinColumnName).withType(toEntity.getId().getType())
		    .withReadWriteDbType(toEntity.getId().getReadWriteDbType()).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		    .withSqlType(toEntity.getId().getSqlType()).withForeignKeyAttribute(a).withJdbcAttributeMapper(jdbcAttributeMapper).build();
	    entity.getJoinColumnAttributes().add(joinColumnAttribute);
	}

	builder = builder.withAttributeType(toEntity);
	return builder.build();
    }
}
