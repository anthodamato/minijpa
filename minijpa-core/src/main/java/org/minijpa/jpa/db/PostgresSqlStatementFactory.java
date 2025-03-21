package org.minijpa.jpa.db;

import org.minijpa.jpa.criteria.expression.PostgresCriteriaExpressionHelper;

public class PostgresSqlStatementFactory extends SqlStatementFactory {

    @Override
    public void init() {
        this.criteriaExpressionHelper = new PostgresCriteriaExpressionHelper();
    }

}
