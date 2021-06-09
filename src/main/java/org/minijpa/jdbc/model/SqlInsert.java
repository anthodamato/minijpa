package org.minijpa.jdbc.model;

import java.util.List;
import java.util.Optional;
import org.minijpa.jdbc.MetaEntity;

public class SqlInsert implements SqlStatement {

    private final FromTable fromTable;
    private final List<Column> columns;
    private final boolean hasIdentityColumn;
    private final boolean identityColumnNull;
    private final Optional<MetaEntity> metaEntity;

    public SqlInsert(FromTable fromTable, List<Column> columns, boolean hasIdentityColumn,
	    boolean identityColumnNull, Optional<MetaEntity> metaEntity) {
	super();
	this.fromTable = fromTable;
	this.columns = columns;
	this.hasIdentityColumn = hasIdentityColumn;
	this.identityColumnNull = identityColumnNull;
	this.metaEntity = metaEntity;
    }

    public FromTable getFromTable() {
	return fromTable;
    }

    public List<Column> getColumns() {
	return columns;
    }

    public boolean hasIdentityColumn() {
	return hasIdentityColumn;
    }

    public boolean isIdentityColumnNull() {
	return identityColumnNull;
    }

    public Optional<MetaEntity> getMetaEntity() {
	return metaEntity;
    }

}
