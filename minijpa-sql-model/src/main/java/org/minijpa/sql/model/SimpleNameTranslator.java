package org.minijpa.sql.model;

public class SimpleNameTranslator implements NameTranslator {
    @Override
    public String toColumnName(String tableAlias, String columnName, String columnAlias) {
        return columnName;
    }

    @Override
    public String toTableName(String tableAlias, String tableName) {
        return tableName;
    }
}
