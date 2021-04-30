/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

/**
 *
 * @author adamato
 */
public abstract class RelationshipHelper {

    private static Optional<String> evalMappedBy(String mappedBy) {
	if (mappedBy == null || mappedBy.isEmpty())
	    return Optional.empty();

	return Optional.of(mappedBy);
    }

    public static Optional<String> getMappedBy(OneToOne oneToOne) {
	return evalMappedBy(oneToOne.mappedBy());
    }

    public static Optional<String> getMappedBy(OneToMany oneToMany) {
	return evalMappedBy(oneToMany.mappedBy());
    }

    public static Optional<String> getMappedBy(ManyToMany manyToMany) {
	return evalMappedBy(manyToMany.mappedBy());
    }

    public static Optional<String> getMappedBy(ManyToOne manyToOne) {
	return Optional.empty();
    }

    protected JoinColumnAttribute createUnidirectionalJoinColumnAttribute(MetaEntity entity, MetaAttribute attribute,
	    String joinColumn, DbConfiguration dbConfiguration) {
	String jc = joinColumn;
	if (jc == null)
	    jc = entity.getName() + "_" + attribute.getColumnName();

	JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper().mapJdbcAttribute(attribute.getType(), attribute.getSqlType());
	return new JoinColumnAttribute.Builder().withColumnName(jc).withType(attribute.getType())
		.withReadWriteDbType(attribute.getReadWriteDbType()).withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withSqlType(attribute.getSqlType()).withForeignKeyAttribute(attribute).withJdbcAttributeMapper(jdbcAttributeMapper).build();
    }

    protected List<JoinColumnAttribute> createUnidirectionalJoinColumnAttributes(MetaEntity entity, DbConfiguration dbConfiguration) {
	List<MetaAttribute> attributes = entity.getId().getAttributes();
	List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	for (MetaAttribute a : attributes) {
	    JoinColumnAttribute joinColumnAttribute = createUnidirectionalJoinColumnAttribute(entity, a, null, dbConfiguration);
	    joinColumnAttributes.add(joinColumnAttribute);
	}

	return joinColumnAttributes;
    }

}
