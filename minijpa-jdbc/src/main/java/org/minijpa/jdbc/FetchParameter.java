package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.ObjectConverter;

public interface FetchParameter {
    String getColumnName();

    Integer getSqlType();

    @SuppressWarnings("rawtypes")
    ObjectConverter getObjectConverter();

}
