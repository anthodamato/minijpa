package org.minijpa.sql.model;

public class SimpleSqlPk implements SqlPk {
	private ColumnDeclaration columnDeclaration;
	private boolean identityColumn;

	public SimpleSqlPk(ColumnDeclaration columnDeclaration) {
		super();
		this.columnDeclaration = columnDeclaration;
	}

	public SimpleSqlPk(ColumnDeclaration columnDeclaration, boolean identityColumn) {
		super();
		this.columnDeclaration = columnDeclaration;
		this.identityColumn = identityColumn;
	}

	@Override
	public boolean isIdentityColumn() {
		return identityColumn;
	}

	@Override
	public ColumnDeclaration getColumn() {
		return columnDeclaration;
	}

}
