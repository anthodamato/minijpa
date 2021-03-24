package org.minijpa.metadata.enhancer.javassist;

import java.util.List;

import javassist.CtField;

public class Property {

    boolean id;
    PropertyMethod getPropertyMethod;
    PropertyMethod setPropertyMethod;
    CtField ctField;
    boolean embedded;
    List<Property> embeddedProperties;

    public Property(boolean id, PropertyMethod getPropertyMethod, PropertyMethod setPropertyMethod, CtField ctField,
	    boolean embedded, List<Property> embeddedProperties) {
	super();
	this.id = id;
	this.getPropertyMethod = getPropertyMethod;
	this.setPropertyMethod = setPropertyMethod;
	this.ctField = ctField;
	this.embedded = embedded;
	this.embeddedProperties = embeddedProperties;
    }

    public boolean isId() {
	return id;
    }

    public PropertyMethod getGetPropertyMethod() {
	return getPropertyMethod;
    }

    public PropertyMethod getSetPropertyMethod() {
	return setPropertyMethod;
    }

    public CtField getCtField() {
	return ctField;
    }

    public boolean isEmbedded() {
	return embedded;
    }

    public List<Property> getEmbeddedProperties() {
	return embeddedProperties;
    }

}
