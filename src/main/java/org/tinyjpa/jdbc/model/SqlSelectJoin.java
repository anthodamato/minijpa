package org.tinyjpa.jdbc.model;

import java.util.List;

import org.tinyjpa.jdbc.AttributeValue;
import org.tinyjpa.jdbc.ColumnNameValue;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.relationship.RelationshipJoinTable;

public class SqlSelectJoin {
	private String tableName;
	private String tableAlias;
	private List<ColumnNameValue> columnNameValues;
	private List<ColumnNameValue> fetchColumnNameValues;
	private List<MetaAttribute> idAttributes;
	private RelationshipJoinTable joinTable;
	private List<AttributeValue> owningIdAttributeValues;

	public SqlSelectJoin(String tableName, String tableAlias, List<ColumnNameValue> columnNameValues,
			List<ColumnNameValue> fetchColumnNameValues, List<MetaAttribute> idAttributes,
			RelationshipJoinTable joinTable, List<AttributeValue> owningIdAttributeValues) {
		super();
		this.tableName = tableName;
		this.tableAlias = tableAlias;
		this.columnNameValues = columnNameValues;
		this.fetchColumnNameValues = fetchColumnNameValues;
		this.idAttributes = idAttributes;
		this.joinTable = joinTable;
		this.owningIdAttributeValues = owningIdAttributeValues;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public List<ColumnNameValue> getColumnNameValues() {
		return columnNameValues;
	}

	public List<ColumnNameValue> getFetchColumnNameValues() {
		return fetchColumnNameValues;
	}

	public List<MetaAttribute> getIdAttributes() {
		return idAttributes;
	}

	public RelationshipJoinTable getJoinTable() {
		return joinTable;
	}

	public List<AttributeValue> getOwningIdAttributeValues() {
		return owningIdAttributeValues;
	}

}
