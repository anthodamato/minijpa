package org.minijpa.jdbc.relationship;

import java.util.List;

import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.Pk;

public class RelationshipJoinTable {

    private String schema;
    private String tableName;
    private String alias;
    private List<JoinColumnAttribute> joinColumnOwningAttributes;
    private List<JoinColumnAttribute> joinColumnTargetAttributes;
    private Pk owningAttribute;
    private Pk targetAttribute;

    public RelationshipJoinTable(String schema, String tableName, String alias, List<JoinColumnAttribute> joinColumnOwningAttributes,
	    List<JoinColumnAttribute> joinColumnTargetAttributes, Pk owningAttribute,
	    Pk targetAttribute) {
	super();
	this.schema = schema;
	this.tableName = tableName;
	this.alias = alias;
	this.joinColumnOwningAttributes = joinColumnOwningAttributes;
	this.joinColumnTargetAttributes = joinColumnTargetAttributes;
	this.owningAttribute = owningAttribute;
	this.targetAttribute = targetAttribute;
    }

    public String getTableName() {
	return tableName;
    }

    public String getAlias() {
	return alias;
    }

    public List<JoinColumnAttribute> getJoinColumnOwningAttributes() {
	return joinColumnOwningAttributes;
    }

    public List<JoinColumnAttribute> getJoinColumnTargetAttributes() {
	return joinColumnTargetAttributes;
    }

    public Pk getOwningAttribute() {
	return owningAttribute;
    }

    public Pk getTargetAttribute() {
	return targetAttribute;
    }

}
