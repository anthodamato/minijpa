package org.minijpa.sql.model;

import java.util.List;

public interface JdbcJoinColumnMapping {

    SqlPk getForeignKey();

    List<ColumnDeclaration> getJoinColumns();

    ColumnDeclaration getJoinColumn();

    default boolean isComposite() {
        return false;
    }

    boolean unique();
}
