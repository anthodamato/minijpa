package org.tinyjpa.jdbc;

import java.util.ArrayList;
import java.util.List;

public class SqlStatement {
	private String sql;
	private Object idValue;
	private List<ColumnNameValue> columnNameValues;
	private List<ColumnNameValue> fetchColumnNameValues;

	public SqlStatement() {
	}

	public String getSql() {
		return sql;
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
		private Object idValue;
		private List<ColumnNameValue> columnNameValues = new ArrayList<>();
		private List<ColumnNameValue> fetchColumnNameValues;

		public Builder() {
		}

		public Builder withSql(String sql) {
			this.sql = sql;
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
			sqlStatement.idValue = idValue;
			sqlStatement.columnNameValues = columnNameValues;
			sqlStatement.fetchColumnNameValues = fetchColumnNameValues;
			return sqlStatement;
		}
	}
}
