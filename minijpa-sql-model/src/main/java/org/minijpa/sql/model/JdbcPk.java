package org.minijpa.sql.model;

import java.util.List;

public interface JdbcPk {
	public default boolean isComposite() {
		return false;
	}

	public default boolean isIdentityColumn() {
		return false;
	}

	public ColumnDeclaration getColumn();

	public default List<ColumnDeclaration> getColumns() {
		return null;
	}

}
