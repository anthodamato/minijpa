package org.minijpa.jdbc.model;

import java.util.List;

public interface JdbcJoinColumnMapping {
	public JdbcPk getForeignKey();

	public List<ColumnDeclaration> getJoinColumns();

	public ColumnDeclaration getJoinColumn();

	public default boolean isComposite() {
		return false;
	}

}
