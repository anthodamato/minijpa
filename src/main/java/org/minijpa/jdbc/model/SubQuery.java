package org.minijpa.jdbc.model;

import java.util.Optional;

public class SubQuery {
	private SqlSelect query;
	private Optional<String> alias = Optional.empty();

	public SubQuery(SqlSelect query, String alias) {
		super();
		this.query = query;
		if (alias != null)
			this.alias = Optional.of(alias);
	}

	public Optional<String> getAlias() {
		return alias;
	}

	public SqlSelect getQuery() {
		return query;
	}

}
