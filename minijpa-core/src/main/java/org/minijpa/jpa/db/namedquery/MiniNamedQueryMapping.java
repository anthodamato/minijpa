package org.minijpa.jpa.db.namedquery;

import org.minijpa.jpa.db.LockType;
import org.minijpa.jpa.db.StatementParameters;

import java.util.Map;

public class MiniNamedQueryMapping {
    private final String name;
    private final String query;
    private LockType lockType = LockType.NONE;
    private Map<String, Object> hints;
    private StatementParameters statementParameters;

    public MiniNamedQueryMapping(String name, String query) {
        this.name = name;
        this.query = query;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public Map<String, Object> getHints() {
        return hints;
    }

    public void setHints(Map<String, Object> hints) {
        this.hints = hints;
    }

    public StatementParameters getStatementParameters() {
        return statementParameters;
    }

    public void setStatementParameters(StatementParameters statementParameters) {
        this.statementParameters = statementParameters;
    }
}
