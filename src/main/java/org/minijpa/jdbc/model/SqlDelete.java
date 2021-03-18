package org.minijpa.jdbc.model;

import java.util.Optional;

import org.minijpa.jdbc.model.condition.Condition;

public class SqlDelete implements SqlStatement {

    private FromTable fromTable;
    private Optional<Condition> condition;

    public SqlDelete(FromTable fromTable, Optional<Condition> condition) {
	super();
	this.fromTable = fromTable;
	this.condition = condition;
    }

    public FromTable getFromTable() {
	return fromTable;
    }

    public Optional<Condition> getCondition() {
	return condition;
    }

}
