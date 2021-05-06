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
import org.minijpa.jdbc.relationship.CompositeJoinColumnMapping;
import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinColumnMapping;
import org.minijpa.jdbc.relationship.SingleJoinColumnMapping;

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
		.withSqlType(attribute.getSqlType()).withAttribute(attribute).withJdbcAttributeMapper(jdbcAttributeMapper).build();
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

    private String createDefaultJoinColumn(MetaAttribute owningAttribute, MetaAttribute targetAttribute) {
	return owningAttribute.getName() + "_" + targetAttribute.getColumnName();
    }

    private JoinColumnMapping buildCompositeJoinColumnMapping(DbConfiguration dbConfiguration, MetaAttribute a,
	    MetaEntity toEntity, Optional<JoinColumnDataList> joinColumnDataList) {
	List<JoinColumnAttribute> joinColumnAttributes = new ArrayList<>();
	for (MetaAttribute metaAttribute : toEntity.getId().getAttributes()) {
	    Optional<String> joinColumnName = joinColumnDataList.isPresent()
		    ? joinColumnDataList.get().getNameByReferenced(metaAttribute.getColumnName())
		    : Optional.empty();
	    if (joinColumnName.isEmpty())
		joinColumnName = Optional.of(createDefaultJoinColumn(a, metaAttribute));

	    JoinColumnAttribute joinColumnAttribute = buildJoinColumnAttribute(
		    joinColumnName.get(), dbConfiguration, a, metaAttribute);
	    joinColumnAttributes.add(joinColumnAttribute);
	}

	return new CompositeJoinColumnMapping(joinColumnAttributes, a, toEntity.getId());
    }

    private JoinColumnAttribute buildJoinColumnAttribute(
	    String joinColumnName,
	    DbConfiguration dbConfiguration,
	    MetaAttribute foreignKeyAttribute,
	    MetaAttribute id) {
	JdbcAttributeMapper jdbcAttributeMapper = dbConfiguration.getDbTypeMapper()
		.mapJdbcAttribute(id.getType(), id.getSqlType());
	return new JoinColumnAttribute.Builder()
		.withColumnName(joinColumnName)
		.withType(id.getType())
		.withReadWriteDbType(id.getReadWriteDbType())
		.withDbTypeMapper(dbConfiguration.getDbTypeMapper())
		.withSqlType(id.getSqlType())
		.withAttribute(foreignKeyAttribute)
		.withForeignKeyAttribute(id)
		.withJdbcAttributeMapper(jdbcAttributeMapper).build();
    }

    private JoinColumnMapping buildSingleJoinColumnMapping(DbConfiguration dbConfiguration, MetaAttribute a,
	    MetaEntity toEntity, Optional<JoinColumnDataList> joinColumnDataList) {
	String joinColumnName = null;
	if (joinColumnDataList.isPresent()) {
	    if (joinColumnDataList.get().getJoinColumnDataList().get(0).getName().isPresent())
		joinColumnName = joinColumnDataList.get().getJoinColumnDataList().get(0).getName().get();
	} else
	    joinColumnName = createDefaultJoinColumn(a, toEntity.getId().getAttribute());

	JoinColumnAttribute joinColumnAttribute = buildJoinColumnAttribute(
		joinColumnName, dbConfiguration, a, toEntity.getId().getAttribute());
	return new SingleJoinColumnMapping(joinColumnAttribute, a, toEntity.getId());
    }

    protected JoinColumnMapping buildJoinColumnMapping(
	    DbConfiguration dbConfiguration,
	    MetaAttribute a,
	    MetaEntity toEntity,
	    Optional<JoinColumnDataList> joinColumnDataList) {
	if (toEntity.getId().isComposite())
	    return buildCompositeJoinColumnMapping(dbConfiguration, a, toEntity, joinColumnDataList);

	return buildSingleJoinColumnMapping(dbConfiguration, a, toEntity, joinColumnDataList);
    }

}
