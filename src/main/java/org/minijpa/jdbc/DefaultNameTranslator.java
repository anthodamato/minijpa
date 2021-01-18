package org.minijpa.jdbc;

import java.util.Optional;

public class DefaultNameTranslator implements NameTranslator {

	@Override
	public String toColumnName(Optional<String> tableAlias, String columnName) {
		if (tableAlias.isPresent())
			return tableAlias.get() + "." + columnName;

		return columnName;
	}

	@Override
	public String toTableName(Optional<String> tableAlias, String tableName) {
		if (tableAlias.isPresent())
			return tableName + " AS " + tableAlias.get();

		return tableName;
	}

}
