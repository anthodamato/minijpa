package org.minijpa.sql.model;

import java.util.List;

public class SimpleSqlPk implements SqlPk {
    private ColumnDeclaration columnDeclaration;
    private boolean identityColumn;
    private List<ColumnDeclaration> constraintColumnDeclarations;

    public SimpleSqlPk(ColumnDeclaration columnDeclaration) {
        super();
        this.columnDeclaration = columnDeclaration;
        this.constraintColumnDeclarations = List.of(columnDeclaration);
    }

    public SimpleSqlPk(ColumnDeclaration columnDeclaration, boolean identityColumn) {
        super();
        this.columnDeclaration = columnDeclaration;
        this.identityColumn = identityColumn;
        this.constraintColumnDeclarations = List.of(columnDeclaration);
    }

    @Override
    public boolean isIdentityColumn() {
        return identityColumn;
    }

    @Override
    public ColumnDeclaration getColumn() {
        return columnDeclaration;
    }

    @Override
    public List<ColumnDeclaration> getConstraintColumns() {
        return constraintColumnDeclarations;
    }
}
