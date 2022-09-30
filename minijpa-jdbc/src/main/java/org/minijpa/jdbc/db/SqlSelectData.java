package org.minijpa.jdbc.db;

import java.util.List;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.sql.model.SqlSelect;

public class SqlSelectData extends SqlSelect {
	private List<FetchParameter> fetchParameters;

	public SqlSelectData() {
		super();
	}

	public List<FetchParameter> getFetchParameters() {
		return fetchParameters;
	}

	public void setFetchParameters(List<FetchParameter> fetchParameters) {
		this.fetchParameters = fetchParameters;
	}

}
