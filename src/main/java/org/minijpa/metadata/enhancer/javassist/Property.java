package org.minijpa.metadata.enhancer.javassist;

import java.util.List;
import java.util.Optional;

import javassist.CtField;

public class Property {

    boolean id;
    PropertyMethod getPropertyMethod;
    PropertyMethod setPropertyMethod;
    CtField ctField;
    boolean embedded;
    List<Property> embeddedProperties;
    private final Optional<RelationshipProperties> relationshipProperties;

    public Property(boolean id,
	    PropertyMethod getPropertyMethod,
	    PropertyMethod setPropertyMethod,
	    CtField ctField,
	    boolean embedded,
	    List<Property> embeddedProperties,
	    Optional<RelationshipProperties> relationshipProperties) {
	super();
	this.id = id;
	this.getPropertyMethod = getPropertyMethod;
	this.setPropertyMethod = setPropertyMethod;
	this.ctField = ctField;
	this.embedded = embedded;
	this.embeddedProperties = embeddedProperties;
	this.relationshipProperties = relationshipProperties;
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

    public Optional<RelationshipProperties> getRelationshipProperties() {
	return relationshipProperties;
    }

}
