package org.minijpa.jpa.model;

public class IdClassDataAttribute {
    private String name;
    private String type;
    private String getMethod;
    private String setMethod;

    public IdClassDataAttribute(String name, String type, String getMethod, String setMethod) {
        this.name = name;
        this.type = type;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getGetMethod() {
        return getMethod;
    }

    public String getSetMethod() {
        return setMethod;
    }
}
