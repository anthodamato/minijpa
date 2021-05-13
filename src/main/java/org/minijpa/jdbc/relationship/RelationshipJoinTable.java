package org.minijpa.jdbc.relationship;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.Pk;

public class RelationshipJoinTable {

    private String schema;
    private String tableName;
    private String alias;
    private final JoinColumnMapping owningJoinColumnMapping;
    private final JoinColumnMapping targetJoinColumnMapping;
    private MetaEntity owningEntity;
    private MetaEntity targetEntity;
    private Pk owningAttribute;
    private Pk targetAttribute;

    public RelationshipJoinTable(String schema, String tableName, String alias,
	    JoinColumnMapping owningJoinColumnMapping,
	    JoinColumnMapping targetJoinColumnMapping,
	    MetaEntity owningEntity,
	    MetaEntity targetEntity, Pk owningAttribute,
	    Pk targetAttribute) {
	super();
	this.schema = schema;
	this.tableName = tableName;
	this.alias = alias;
	this.owningJoinColumnMapping = owningJoinColumnMapping;
	this.targetJoinColumnMapping = targetJoinColumnMapping;
	this.owningEntity = owningEntity;
	this.targetEntity = targetEntity;
	this.owningAttribute = owningAttribute;
	this.targetAttribute = targetAttribute;
    }

    public String getTableName() {
	return tableName;
    }

    public String getAlias() {
	return alias;
    }

    public String getSchema() {
	return schema;
    }

    public JoinColumnMapping getOwningJoinColumnMapping() {
	return owningJoinColumnMapping;
    }

    public JoinColumnMapping getTargetJoinColumnMapping() {
	return targetJoinColumnMapping;
    }

    public MetaEntity getOwningEntity() {
	return owningEntity;
    }

    public MetaEntity getTargetEntity() {
	return targetEntity;
    }

    public Pk getOwningAttribute() {
	return owningAttribute;
    }

    public Pk getTargetAttribute() {
	return targetAttribute;
    }

}
