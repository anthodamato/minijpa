package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.AttributeMapper;

public interface FetchParameter {
    String getColumnName();

    Integer getSqlType();

    @SuppressWarnings("rawtypes")
    AttributeMapper getAttributeMapper();

}
