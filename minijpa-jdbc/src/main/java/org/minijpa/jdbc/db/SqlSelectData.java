package org.minijpa.jdbc.db;

import java.util.List;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatement;
import org.minijpa.jdbc.model.StatementType;

public class SqlSelectData implements SqlStatement {
	private SqlSelect sqlSelect;
	private List<FetchParameter> fetchParameters;

	public SqlSelectData(SqlSelect sqlSelect, List<FetchParameter> fetchParameters) {
		super();
		this.sqlSelect = sqlSelect;
		this.fetchParameters = fetchParameters;
	}

	public SqlSelect getSqlSelect() {
		return sqlSelect;
	}

	public List<FetchParameter> getFetchParameters() {
		return fetchParameters;
	}

	@Override
	public StatementType getType() {
		return sqlSelect.getType();
	}

}
