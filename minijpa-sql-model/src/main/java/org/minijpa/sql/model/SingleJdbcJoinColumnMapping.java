package org.minijpa.sql.model;

import java.util.List;

public class SingleJdbcJoinColumnMapping implements JdbcJoinColumnMapping {
	private ColumnDeclaration joinColumn;
	private SqlPk jdbcPk;

	public SingleJdbcJoinColumnMapping(ColumnDeclaration joinColumn, SqlPk jdbcPk) {
		super();
		this.joinColumn = joinColumn;
		this.jdbcPk = jdbcPk;
	}

	@Override
	public SqlPk getForeignKey() {
		return jdbcPk;
	}

	@Override
	public ColumnDeclaration getJoinColumn() {
		return joinColumn;
	}

	@Override
	public List<ColumnDeclaration> getJoinColumns() {
		return List.of(joinColumn);
	}

}
