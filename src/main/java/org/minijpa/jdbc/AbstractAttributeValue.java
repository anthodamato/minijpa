package org.minijpa.jdbc;

public class AbstractAttributeValue {
	private AbstractAttribute attribute;
	private Object value;

	public AbstractAttributeValue(AbstractAttribute attribute, Object value) {
		super();
		this.attribute = attribute;
		this.value = value;
	}

	public AbstractAttribute getAttribute() {
		return attribute;
	}

	public Object getValue() {
		return value;
	}

}
