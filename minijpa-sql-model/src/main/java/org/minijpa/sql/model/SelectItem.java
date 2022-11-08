package org.minijpa.sql.model;

import java.util.Optional;

public class SelectItem implements Value {
    private Object item;
    private Optional<String> alias;

    public SelectItem(Object item, Optional<String> alias) {
        super();
        this.item = item;
        this.alias = alias;
    }

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    public Optional<String> getAlias() {
        return alias;
    }

    public void setAlias(Optional<String> alias) {
        this.alias = alias;
    }

}
