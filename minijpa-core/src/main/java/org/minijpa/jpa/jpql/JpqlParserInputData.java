package org.minijpa.jpa.jpql;

import javax.persistence.Parameter;
import java.util.Map;

public class JpqlParserInputData {
    private Map<Parameter<?>, Object> parameterMap;
    private Map<String, Object> hints;

    public JpqlParserInputData(Map<Parameter<?>, Object> parameterMap, Map<String, Object> hints) {
        this.parameterMap = parameterMap;
        this.hints = hints;
    }

    public Map<Parameter<?>, Object> getParameterMap() {
        return parameterMap;
    }

    public Map<String, Object> getHints() {
        return hints;
    }
}
