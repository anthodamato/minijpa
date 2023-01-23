package org.minijpa.jpa.metamodel.generator;

public class AttributeElement {
    private String name;
    private AttributeType attributeType;
    private Class<?> type;
    private boolean relationship = false;

    public AttributeElement(String name, AttributeType attributeType, Class<?> type) {
        super();
        this.name = name;
        this.attributeType = attributeType;
        this.type = type;
    }

    public AttributeElement(String name, AttributeType attributeType, Class<?> type, boolean relationship) {
        super();
        this.name = name;
        this.attributeType = attributeType;
        this.type = type;
        this.relationship = relationship;
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

    public boolean isRelationship() {
        return relationship;
    }

}
