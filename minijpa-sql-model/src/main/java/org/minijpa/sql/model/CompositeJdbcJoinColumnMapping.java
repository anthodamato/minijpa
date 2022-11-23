package org.minijpa.sql.model;

import java.util.List;

public class CompositeJdbcJoinColumnMapping implements JdbcJoinColumnMapping {
	private List<ColumnDeclaration> joinColumns;
	private SqlPk jdbcPk;

	public CompositeJdbcJoinColumnMapping(List<ColumnDeclaration> joinColumns, SqlPk jdbcPk) {
		super();
		this.joinColumns = joinColumns;
		this.jdbcPk = jdbcPk;
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	@Override
	public SqlPk getForeignKey() {
		return jdbcPk;
	}

	@Override
	public List<ColumnDeclaration> getJoinColumns() {
		return joinColumns;
	}

	@Override
	public ColumnDeclaration getJoinColumn() {
		return null;
	}

}
