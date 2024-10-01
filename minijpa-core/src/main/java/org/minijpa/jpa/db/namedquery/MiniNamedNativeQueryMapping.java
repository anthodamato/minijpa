package org.minijpa.jpa.db.namedquery;

import java.util.Map;

public class MiniNamedNativeQueryMapping {
    private final String name;
    private final String query;
    private Map<String, Object> hints;
    private Class resultClass;
    private String resultSetMapping;

    public MiniNamedNativeQueryMapping(String name, String query) {
        this.name = name;
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }


    public Map<String, Object> getHints() {
        return hints;
    }

    public void setHints(Map<String, Object> hints) {
        this.hints = hints;
    }

    public Class getResultClass() {
        return resultClass;
    }

    public void setResultClass(Class resultClass) {
        this.resultClass = resultClass;
    }

    public String getResultSetMapping() {
        return resultSetMapping;
    }

    public void setResultSetMapping(String resultSetMapping) {
        this.resultSetMapping = resultSetMapping;
    }
}
