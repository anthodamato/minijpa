package org.tinyjpa.jdbc;

import java.util.List;

public class SqlStatement {
	private String sql;
	private Object[] values;
	private List<Attribute> attributes;
	private List<AttributeValue> attrValues;
	private int startIndex = 0;
	private Object idValue;
	private List<ColumnNameValue> columnNameValues;
	private List<ColumnNameValue> fetchColumnNameValues;

	public SqlStatement() {
	}

	public String getSql() {
		return sql;
	}

	public Object[] getValues() {
		return values;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public List<AttributeValue> getAttrValues() {
		return attrValues;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public Object getIdValue() {
		return idValue;
	}

	public List<ColumnNameValue> getColumnNameValues() {
		return columnNameValues;
	}

	public List<ColumnNameValue> getFetchColumnNameValues() {
		return fetchColumnNameValues;
	}

	public static class Builder {
		private String sql;
		private Object[] values;
		private List<Attribute> attributes;
		private List<AttributeValue> attrValues;
		private Object idValue;
		private List<ColumnNameValue> columnNameValues;
		private List<ColumnNameValue> fetchColumnNameValues;

		public Builder() {
		}

		public Builder withSql(String sql) {
			this.sql = sql;
			return this;
		}

		public Builder withValues(Object[] values) {
			this.values = values;
			return this;
		}

		public Builder withAttributes(List<Attribute> attributes) {
			this.attributes = attributes;
			return this;
		}

		public Builder withAttributeValues(List<AttributeValue> attrValues) {
			this.attrValues = attrValues;
			return this;
		}

		public Builder withIdValue(Object idValue) {
			this.idValue = idValue;
			return this;
		}

		public Builder withColumnNameValues(List<ColumnNameValue> columnNameValues) {
			this.columnNameValues = columnNameValues;
			return this;
		}

		public Builder withFetchColumnNameValues(List<ColumnNameValue> fetchColumnNameValues) {
			this.fetchColumnNameValues = fetchColumnNameValues;
			return this;
		}

		public SqlStatement build() {
			SqlStatement sqlStatement = new SqlStatement();
			sqlStatement.sql = sql;
			sqlStatement.values = values;
			sqlStatement.attributes = attributes;
			sqlStatement.attrValues = attrValues;
			sqlStatement.idValue = idValue;
			sqlStatement.columnNameValues = columnNameValues;
			sqlStatement.fetchColumnNameValues = fetchColumnNameValues;
			return sqlStatement;
		}
	}
}
