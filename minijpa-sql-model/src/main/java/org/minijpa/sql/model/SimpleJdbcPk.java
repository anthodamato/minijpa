package org.minijpa.sql.model;

public class SimpleJdbcPk implements JdbcPk {
	private ColumnDeclaration columnDeclaration;
	private boolean identityColumn;

	public SimpleJdbcPk(ColumnDeclaration columnDeclaration) {
		super();
		this.columnDeclaration = columnDeclaration;
	}

	public SimpleJdbcPk(ColumnDeclaration columnDeclaration, boolean identityColumn) {
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
