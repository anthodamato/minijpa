package org.tinyjpa.jdbc.model.join;

import java.util.List;

import org.tinyjpa.jdbc.model.Column;
import org.tinyjpa.jdbc.model.FromTable;

public interface FromJoin {
	public FromTable getToTable();

//	public String getName();
//
//	public String getAlias();

	public List<Column> getFromColumns();

	public List<Column> getToColumns();

//	public List<String> getJoinColumnAlias();

	public JoinType getType();
}
