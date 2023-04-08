package org.minijpa.sql.model;

import java.util.List;

public interface JdbcJoinColumnMapping {

  public SqlPk getForeignKey();

  public List<ColumnDeclaration> getJoinColumns();

  public ColumnDeclaration getJoinColumn();

  public default boolean isComposite() {
    return false;
  }

  public boolean unique();
}
