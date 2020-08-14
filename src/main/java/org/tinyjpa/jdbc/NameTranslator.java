package org.tinyjpa.jdbc;

public interface NameTranslator {
	public String toColumnName(String alias, String columnName);

}
