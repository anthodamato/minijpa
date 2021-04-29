package org.minijpa.metadata.enhancer.javassist;

public class AttributeData {

    Property property;
    boolean parentEmbeddedId = false;
    // only for embedded attributes
    ManagedData embeddedData;

    public AttributeData(Property property, boolean parentIsEmbeddedId, ManagedData embeddedData) {
	super();
	this.property = property;
	this.parentEmbeddedId = parentIsEmbeddedId;
	this.embeddedData = embeddedData;
    }

    public ManagedData getEmbeddedData() {
	return embeddedData;
    }

    public Property getProperty() {
	return property;
    }

    public boolean isParentEmbeddedId() {
	return parentEmbeddedId;
    }

}
