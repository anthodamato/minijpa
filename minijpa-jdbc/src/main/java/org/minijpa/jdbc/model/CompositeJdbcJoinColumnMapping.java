package org.minijpa.jdbc.model;

import java.util.List;

public class CompositeJdbcJoinColumnMapping implements JdbcJoinColumnMapping {
	private List<ColumnDeclaration> joinColumns;
	private JdbcPk jdbcPk;

	public CompositeJdbcJoinColumnMapping(List<ColumnDeclaration> joinColumns, JdbcPk jdbcPk) {
		super();
		this.joinColumns = joinColumns;
		this.jdbcPk = jdbcPk;
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	@Override
	public JdbcPk getForeignKey() {
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
