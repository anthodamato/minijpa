package org.minijpa.jpa.jpql;

import javax.persistence.Parameter;
import java.util.Map;

public class JpqlParserInputData {
    private final Map<String, Object> hints;

    public JpqlParserInputData(Map<String, Object> hints) {
        this.hints = hints;
    }


    public Map<String, Object> getHints() {
        return hints;
    }
}
