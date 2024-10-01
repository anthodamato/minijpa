package org.minijpa.jpa;

import javax.persistence.Parameter;

public class QueryParameterData {
    private final int index;
    private final Parameter<?> parameter;
    private final String placeholder;

    public QueryParameterData(int index, Parameter<?> parameter, String placeholder) {
        this.index = index;
        this.parameter = parameter;
        this.placeholder = placeholder;
    }

    public int getIndex() {
        return index;
    }

    public Parameter<?> getParameter() {
        return parameter;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
