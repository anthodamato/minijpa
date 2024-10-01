package org.minijpa.jpa;

import java.util.Map;

public interface NativeQuery {
    public String getSql();

    public Class<?> getResultClass();

    public String getResultSetMapping();

    public Map<String, Object> getAdditionalHints();
}
