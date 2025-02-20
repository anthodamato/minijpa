package org.minijpa.sql.model;

public class SelectItem implements Value {
    private Object item;
    private String alias;

    public SelectItem(Object item, String alias) {
        super();
        this.item = item;
        this.alias = alias;
    }

    public Object getItem() {
        return item;
    }

    public String getAlias() {
        return alias;
    }

}
