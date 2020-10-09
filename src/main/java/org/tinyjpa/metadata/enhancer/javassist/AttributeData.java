package org.tinyjpa.metadata.enhancer.javassist;

public class AttributeData {
	Property property;
//	private List<AttributeData> embeddedAttributes;
	boolean parentIsEmbeddedId = false;
	// only for embedded attributes
//	private CtClass embeddedCtClass;
	ManagedData embeddedData;

	public AttributeData(Property property, boolean parentIsEmbeddedId, ManagedData embeddedData) {
		super();
		this.property = property;
//		this.embeddedAttributes = embeddedProperties;
		this.parentIsEmbeddedId = parentIsEmbeddedId;
//		this.embeddedCtClass = embeddedCtClass;
		this.embeddedData = embeddedData;
	}

	public ManagedData getEmbeddedData() {
		return embeddedData;
	}

}
