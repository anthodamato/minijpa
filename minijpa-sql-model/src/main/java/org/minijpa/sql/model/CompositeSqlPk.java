package org.minijpa.sql.model;

import java.util.List;

public class CompositeSqlPk implements SqlPk {
	private List<ColumnDeclaration> columnDeclarations;

	public CompositeSqlPk(List<ColumnDeclaration> columnDeclarations) {
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
