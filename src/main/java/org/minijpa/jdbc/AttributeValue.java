package org.minijpa.jdbc;

public class AttributeValue {

    private final MetaAttribute attribute;
    private final Object value;

    public AttributeValue(MetaAttribute attribute, Object value) {
	super();
	this.attribute = attribute;
	this.value = value;
    }

    public MetaAttribute getAttribute() {
	return attribute;
    }

    public Object getValue() {
	return value;
    }

}
