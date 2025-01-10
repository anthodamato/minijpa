package org.minijpa.sql.model;

import java.util.List;

public class CompositeSqlPk implements SqlPk {
    private List<ColumnDeclaration> columnDeclarations;
    private List<ColumnDeclaration> constraintColumnDeclarations;

    public CompositeSqlPk(
            List<ColumnDeclaration> columnDeclarations,
            List<ColumnDeclaration> constraintColumnDeclarations) {
        this.columnDeclarations = columnDeclarations;
        this.constraintColumnDeclarations = constraintColumnDeclarations;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public List<ColumnDeclaration> getColumns() {
        return columnDeclarations;
    }

    @Override
    public ColumnDeclaration getColumn() {
        return null;
    }

    @Override
    public List<ColumnDeclaration> getConstraintColumns() {
        return constraintColumnDeclarations;
    }
}
