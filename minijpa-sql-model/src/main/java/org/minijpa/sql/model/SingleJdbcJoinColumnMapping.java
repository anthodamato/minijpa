package org.minijpa.sql.model;

import java.util.List;

public class SingleJdbcJoinColumnMapping implements JdbcJoinColumnMapping {

  private final ColumnDeclaration joinColumn;
  private final SqlPk jdbcPk;
  private boolean unique = false;

  public SingleJdbcJoinColumnMapping(ColumnDeclaration joinColumn, SqlPk jdbcPk) {
    super();
    this.joinColumn = joinColumn;
    this.jdbcPk = jdbcPk;
  }

  public SingleJdbcJoinColumnMapping(ColumnDeclaration joinColumn, SqlPk jdbcPk, boolean unique) {
    super();
    this.joinColumn = joinColumn;
    this.jdbcPk = jdbcPk;
    this.unique = unique;
  }

  @Override
  public SqlPk getForeignKey() {
    return jdbcPk;
  }

  @Override
  public ColumnDeclaration getJoinColumn() {
    return joinColumn;
  }

  @Override
  public List<ColumnDeclaration> getJoinColumns() {
    return List.of(joinColumn);
  }

  @Override
  public boolean unique() {
    return unique;
  }
}
