package org.tinyjpa.jdbc;

import java.util.Optional;

public interface NameTranslator {
	public String toColumnName(Optional<String> tableAlias, String columnName);
}
