package org.minijpa.sql.model;

import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SqlSelectBuilder {

    private List<From> fromTables = new ArrayList<>();
    //  private List<FromJoin> fromJoins;
    private List<Value> values;
    private List<Condition> conditions;
    private GroupBy groupBy;
    private FromTable result;
    private List<OrderBy> orderByList;
    private boolean distinct = false;
    private Optional<ForUpdate> optionalForUpdate = Optional.empty();

    public SqlSelectBuilder() {
        super();
    }

    public SqlSelectBuilder withFromTable(From fromTable) {
        this.fromTables.add(fromTable);
        return this;
    }


    public SqlSelectBuilder withValues(List<Value> values) {
        this.values = Collections.unmodifiableList(values);
        return this;
    }

    public SqlSelectBuilder withConditions(List<Condition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public SqlSelectBuilder withGroupBy(GroupBy groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public SqlSelectBuilder withOrderBy(List<OrderBy> orderByList) {
        this.orderByList = orderByList;
        return this;
    }

    public SqlSelectBuilder withResult(FromTable result) {
        this.result = result;
        return this;
    }

    public SqlSelectBuilder distinct() {
        this.distinct = true;
        return this;
    }

    public SqlSelectBuilder withForUpdate(Optional<ForUpdate> optionalForUpdate) {
        this.optionalForUpdate = optionalForUpdate;
        return this;
    }

    protected void build(SqlSelect sqlSelect) {
        sqlSelect.setFrom(fromTables);

        sqlSelect.setValues(values);
        if (conditions != null && !conditions.isEmpty()) {
            sqlSelect.setConditions(Optional.of(conditions));
        }

        sqlSelect.setGroupBy(Optional.ofNullable(groupBy));
        if (orderByList != null && !orderByList.isEmpty()) {
            sqlSelect.setOrderByList(Optional.of(orderByList));
        }

        sqlSelect.setResult(result);
        sqlSelect.setDistinct(distinct);
        sqlSelect.setOptionalForUpdate(optionalForUpdate);
    }

    public SqlSelect build() {
        SqlSelect sqlSelect = new SqlSelect();
        build(sqlSelect);
        return sqlSelect;
    }

}
