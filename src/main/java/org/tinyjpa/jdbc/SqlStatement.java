package org.tinyjpa.jdbc;

import java.util.List;

public class SqlStatement {
	private String sql;
	private Object[] values;
	private List<Attribute> attributes;
	private List<AttrValue> attrValues;
	private int startIndex;
	private Object idValue;

	public SqlStatement(String sql, Object[] values, List<AttrValue> attrValues, int startIndex, Object idValue) {
		super();
		this.sql = sql;
		this.values = values;
		this.attrValues = attrValues;
		this.startIndex = startIndex;
		this.idValue = idValue;
	}

	public SqlStatement(String sql, Object[] values, List<Attribute> attributes, List<AttrValue> attrValues,
			int startIndex) {
		super();
		this.sql = sql;
		this.values = values;
		this.attributes = attributes;
		this.attrValues = attrValues;
		this.startIndex = startIndex;
	}

	public String getSql() {
		return sql;
	}

	public Object[] getValues() {
		return values;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public List<AttrValue> getAttrValues() {
		return attrValues;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public Object getIdValue() {
		return idValue;
	}

}
