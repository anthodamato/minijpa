package org.minijpa.jdbc.model;

import java.util.List;

public class CompositeJdbcPk implements JdbcPk {
	private List<ColumnDeclaration> columnDeclarations;

	public CompositeJdbcPk(List<ColumnDeclaration> columnDeclarations) {
		super();
		this.columnDeclarations = columnDeclarations;
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	@Override
	public List<ColumnDeclaration> getColumns() {
		return columnDeclarations;
	}

	@Override
	public ColumnDeclaration getColumn() {
		return null;
	}

}
