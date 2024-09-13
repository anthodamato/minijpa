package org.minijpa.jdbc;

import java.util.Optional;

import org.minijpa.jdbc.mapper.AttributeMapper;

public class BasicFetchParameter implements FetchParameter {
    private final String columnName;
    private final Integer sqlType;
    private AttributeMapper attributeMapper;

    public BasicFetchParameter(String columnName, Integer sqlType, AttributeMapper attributeMapper) {
        super();
        this.columnName = columnName;
        this.sqlType = sqlType;
        this.attributeMapper = attributeMapper;
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
    public AttributeMapper getAttributeMapper() {
        return attributeMapper;
    }

}
