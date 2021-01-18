package org.minijpa.jdbc;

import java.util.Optional;

public interface NameTranslator {
	public String toColumnName(Optional<String> tableAlias, String columnName);

	public String toTableName(Optional<String> tableAlias, String tableName);
}
