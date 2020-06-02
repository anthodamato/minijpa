package org.tinyjpa.metadata;

public class EnhAttribute {
	private String attributeName;
	private String attributeClassName;
	private String getMethod;
	private String setMethod;

	public EnhAttribute(String attribute, String attributeClassName, String getMethod, String setMethod) {
		super();
		this.attributeName = attribute;
		this.attributeClassName = attributeClassName;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attribute) {
		this.attributeName = attribute;
	}

	public String getAttributeClassName() {
		return attributeClassName;
	}

	public void setAttributeClassName(String attributeClassName) {
		this.attributeClassName = attributeClassName;
	}

	public String getGetMethod() {
		return getMethod;
	}

	public void setGetMethod(String getMethod) {
		this.getMethod = getMethod;
	}

	public String getSetMethod() {
		return setMethod;
	}

	public void setSetMethod(String setMethod) {
		this.setMethod = setMethod;
	}

}
