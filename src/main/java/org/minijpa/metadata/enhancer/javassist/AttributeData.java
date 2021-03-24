package org.minijpa.metadata.enhancer.javassist;

public class AttributeData {

    Property property;
    boolean parentIsEmbeddedId = false;
    // only for embedded attributes
    ManagedData embeddedData;

    public AttributeData(Property property, boolean parentIsEmbeddedId, ManagedData embeddedData) {
	super();
	this.property = property;
	this.parentIsEmbeddedId = parentIsEmbeddedId;
	this.embeddedData = embeddedData;
    }

    public ManagedData getEmbeddedData() {
	return embeddedData;
    }

    public Property getProperty() {
	return property;
    }

    public boolean isParentIsEmbeddedId() {
	return parentIsEmbeddedId;
    }

}
