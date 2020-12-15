package org.tinyjpa.jdbc.model;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.tinyjpa.jdbc.ColumnNameValue;

public class SqlSelect {
	private String tableName;
	private String tableAlias;
	private List<ColumnNameValue> columnNameValues;
	private List<ColumnNameValue> fetchColumnNameValues;
	private List<ColumnNameValue> joinColumnNameValues;
	private CriteriaQuery<?> criteriaQuery;

	public SqlSelect(String tableName, String tableAlias, List<ColumnNameValue> columnNameValues,
			List<ColumnNameValue> fetchColumnNameValues, List<ColumnNameValue> joinColumnNameValues,
			CriteriaQuery<?> criteriaQuery) {
		super();
		this.tableName = tableName;
		this.tableAlias = tableAlias;
		this.columnNameValues = columnNameValues;
		this.fetchColumnNameValues = fetchColumnNameValues;
		this.joinColumnNameValues = joinColumnNameValues;
		this.criteriaQuery = criteriaQuery;
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

	public List<ColumnNameValue> getJoinColumnNameValues() {
		return joinColumnNameValues;
	}

	public CriteriaQuery<?> getCriteriaQuery() {
		return criteriaQuery;
	}

}
