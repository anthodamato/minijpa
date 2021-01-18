package org.minijpa.jdbc.relationship;

import java.util.List;

import org.minijpa.jdbc.JoinColumnAttribute;
import org.minijpa.jdbc.MetaAttribute;

public class RelationshipJoinTable {
	private String tableName;
	private String alias;
	private List<JoinColumnAttribute> joinColumnOwningAttributes;
	private List<JoinColumnAttribute> joinColumnTargetAttributes;
	private MetaAttribute owningAttribute;
	private MetaAttribute targetAttribute;

	public RelationshipJoinTable(String tableName, String alias, List<JoinColumnAttribute> joinColumnOwningAttributes,
			List<JoinColumnAttribute> joinColumnTargetAttributes, MetaAttribute owningAttribute,
			MetaAttribute targetAttribute) {
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

	public MetaAttribute getOwningAttribute() {
		return owningAttribute;
	}

	public MetaAttribute getTargetAttribute() {
		return targetAttribute;
	}

}
