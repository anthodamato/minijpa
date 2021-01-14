package org.tinyjpa.jdbc;

import java.util.Optional;

public class DefaultNameTranslator implements NameTranslator {

	@Override
	public String toColumnName(Optional<String> tableAlias, String columnName) {
		if (tableAlias.isPresent())
			return tableAlias.get() + "." + columnName;

		return columnName;
	}

}
