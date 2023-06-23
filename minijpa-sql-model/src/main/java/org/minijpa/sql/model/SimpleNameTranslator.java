package org.minijpa.sql.model;

import java.util.Optional;

public class SimpleNameTranslator implements NameTranslator {
    @Override
    public String toColumnName(Optional<String> tableAlias, String columnName, Optional<String> columnAlias) {
        return columnName;
    }

    @Override
    public String toTableName(Optional<String> tableAlias, String tableName) {
        return tableName;
    }
}
