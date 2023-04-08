package org.minijpa.sql.model;

import java.util.List;

public class CompositeJdbcJoinColumnMapping implements JdbcJoinColumnMapping {

  private final List<ColumnDeclaration> joinColumns;
  private final SqlPk jdbcPk;
  private boolean unique = false;

  public CompositeJdbcJoinColumnMapping(List<ColumnDeclaration> joinColumns, SqlPk jdbcPk) {
    super();
    this.joinColumns = joinColumns;
    this.jdbcPk = jdbcPk;
  }

  public CompositeJdbcJoinColumnMapping(List<ColumnDeclaration> joinColumns, SqlPk jdbcPk,
      boolean unique) {
    super();
    this.joinColumns = joinColumns;
    this.jdbcPk = jdbcPk;
    this.unique = unique;
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

  @Override
  public boolean unique() {
    return unique;
  }

}
