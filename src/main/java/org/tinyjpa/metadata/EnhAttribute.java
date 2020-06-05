package org.tinyjpa.metadata;

public class EnhAttribute {
	private String name;
	private String className;
	private boolean primitiveType;
	private String getMethod;
	private String setMethod;

	public EnhAttribute(String name, String className, boolean primitiveType, String getMethod, String setMethod) {
		super();
		this.name = name;
		this.className = className;
		this.primitiveType = primitiveType;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}

	public boolean isPrimitiveType() {
		return primitiveType;
	}

	public String getGetMethod() {
		return getMethod;
	}

	public String getSetMethod() {
		return setMethod;
	}

}
