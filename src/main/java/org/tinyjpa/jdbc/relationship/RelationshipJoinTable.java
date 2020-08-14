package org.tinyjpa.jdbc.relationship;

import java.util.List;

import org.tinyjpa.jdbc.Attribute;
import org.tinyjpa.jdbc.JoinColumnAttribute;

public class RelationshipJoinTable {
	private String tableName;
	private String alias;
	private List<JoinColumnAttribute> joinColumnOwningAttributes;
	private List<JoinColumnAttribute> joinColumnTargetAttributes;
	private Attribute owningAttribute;
	private Attribute targetAttribute;

	public RelationshipJoinTable(String tableName, String alias, List<JoinColumnAttribute> joinColumnOwningAttributes,
			List<JoinColumnAttribute> joinColumnTargetAttributes, Attribute owningAttribute,
			Attribute targetAttribute) {
		super();
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

	public Attribute getOwningAttribute() {
		return owningAttribute;
	}

	public Attribute getTargetAttribute() {
		return targetAttribute;
	}

}
