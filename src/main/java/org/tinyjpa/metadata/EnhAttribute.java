package org.tinyjpa.metadata;

import java.util.List;

public class EnhAttribute {
	private String name;
	private String className;
	private boolean primitiveType;
	private String getMethod;
	private String setMethod;
	private boolean embedded;
	private List<EnhAttribute> embeddedAttributes;

	public EnhAttribute(String name, String className, boolean primitiveType, String getMethod, String setMethod,
			boolean embedded, List<EnhAttribute> embeddedAttributes) {
		super();
		this.name = name;
		this.className = className;
		this.primitiveType = primitiveType;
		this.getMethod = getMethod;
		this.setMethod = setMethod;
		this.embedded = embedded;
		this.embeddedAttributes = embeddedAttributes;
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

	public boolean isEmbedded() {
		return embedded;
	}

	public List<EnhAttribute> getEmbeddedAttributes() {
		return embeddedAttributes;
	}

}
