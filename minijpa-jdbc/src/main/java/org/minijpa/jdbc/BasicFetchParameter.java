package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.ObjectConverter;

public class BasicFetchParameter implements FetchParameter {
    private final String columnName;
    private final Integer sqlType;
    private ObjectConverter objectConverter;

    public BasicFetchParameter(String columnName, Integer sqlType, ObjectConverter objectConverter) {
        super();
        this.columnName = columnName;
        this.sqlType = sqlType;
        this.objectConverter = objectConverter;
    }

    public BasicFetchParameter(String columnName, Integer sqlType) {
        super();
        this.columnName = columnName;
        this.sqlType = sqlType;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Integer getSqlType() {
        return sqlType;
    }

    @Override
    public ObjectConverter getObjectConverter() {
        return objectConverter;
    }

}
