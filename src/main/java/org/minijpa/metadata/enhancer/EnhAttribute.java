package org.minijpa.metadata.enhancer;

import java.util.List;
import java.util.Optional;

public class EnhAttribute {

    private String name;
    private String className;
    private boolean primitiveType;
    private String getMethod;
    private String setMethod;
    private boolean embedded;
    private EnhEntity embeddedEnhEntity;
    private Optional<String> joinColumnSetMethod = Optional.empty();
    private Optional<String> joinColumnGetMethod = Optional.empty();

    public EnhAttribute(String name, String className, boolean primitiveType, String getMethod, String setMethod,
	    boolean embedded, List<EnhAttribute> embeddedAttributes, EnhEntity embeddedEnhEntity) {
	super();
	this.name = name;
	this.className = className;
	this.primitiveType = primitiveType;
	this.getMethod = getMethod;
	this.setMethod = setMethod;
	this.embedded = embedded;
	this.embeddedEnhEntity = embeddedEnhEntity;
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

    public EnhEntity getEmbeddedEnhEntity() {
	return embeddedEnhEntity;
    }

    public Optional<String> getJoinColumnSetMethod() {
	return joinColumnSetMethod;
    }

    public void setJoinColumnSetMethod(Optional<String> joinColumnSetMethod) {
	this.joinColumnSetMethod = joinColumnSetMethod;
    }

    public Optional<String> getJoinColumnGetMethod() {
	return joinColumnGetMethod;
    }

    public void setJoinColumnGetMethod(Optional<String> joinColumnGetMethod) {
	this.joinColumnGetMethod = joinColumnGetMethod;
    }

}
