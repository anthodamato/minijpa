package org.minijpa.jdbc.model.join;

import java.util.List;

import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;

public class FromJoinImpl implements FromJoin {

    private FromTable toTable;
//	private String name;
//	private String alias;
    private List<Column> fromColumns;
//	private List<String> columnAlias;
    private List<Column> toColumns;
//	private List<String> joinColumnAlias;
    private JoinType joinType = JoinType.InnerJoin;

    public FromJoinImpl(FromTable toTable, List<Column> fromColumns, List<String> columnAlias, List<Column> toColumns,
	    List<String> joinColumnAlias) {
	super();
	this.toTable = toTable;
	this.fromColumns = fromColumns;
//		this.columnAlias = columnAlias;
	this.toColumns = toColumns;
//		this.joinColumnAlias = joinColumnAlias;
    }

    public FromJoinImpl(FromTable toTable, List<Column> fromColumns, List<Column> toColumns) {
	super();
	this.toTable = toTable;
	this.fromColumns = fromColumns;
	this.toColumns = toColumns;
    }

    public FromJoinImpl(FromTable toTable, List<Column> columns, List<String> columnAlias, List<Column> toColumns,
	    List<String> joinColumnAlias, JoinType joinType) {
	super();
	this.toTable = toTable;
	this.fromColumns = columns;
//		this.columnAlias = columnAlias;
	this.toColumns = toColumns;
//		this.joinColumnAlias = joinColumnAlias;
	this.joinType = joinType;
    }

//	@Override
//	public String getName() {
//		return name;
//	}
//
//	@Override
//	public String getAlias() {
//		return alias;
//	}
    public FromTable getToTable() {
	return toTable;
    }

    @Override
    public List<Column> getFromColumns() {
	return fromColumns;
    }

    @Override
    public List<Column> getToColumns() {
	return toColumns;
    }

//	@Override
//	public List<String> getJoinColumnAlias() {
//		return joinColumnAlias;
//	}
    @Override
    public JoinType getType() {
	return joinType;
    }

}
