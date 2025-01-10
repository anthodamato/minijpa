package org.minijpa.sql.model;

import java.util.List;

public interface SqlPk {
    default boolean isComposite() {
        return false;
    }

    default boolean isIdentityColumn() {
        return false;
    }

    ColumnDeclaration getColumn();

    default List<ColumnDeclaration> getColumns() {
        return null;
    }

    List<ColumnDeclaration> getConstraintColumns();

}
