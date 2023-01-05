package org.minijpa.jpa.metamodel.generator;

public class AttributeElement {
    private String name;
    private AttributeType attributeType;
    private Class<?> type;

    public AttributeElement(String name, AttributeType attributeType, Class<?> type) {
        super();
        this.name = name;
        this.attributeType = attributeType;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public Class<?> getType() {
        return type;
    }

}
