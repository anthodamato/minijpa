package org.tinyjpa.jdbc;

public class DefaultNameTranslator implements NameTranslator {

	@Override
	public String toColumnName(String alias, String columnName) {
		if (alias == null)
			return columnName;

		return alias + "." + columnName;
	}

}
