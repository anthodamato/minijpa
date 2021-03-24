package org.minijpa.metadata.enhancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EnhEntity {

    private String className;
    private List<EnhAttribute> enhAttributes = new ArrayList<>();
    private EnhEntity mappedSuperclass;
    private String modificationAttributeGetMethod;
    private Optional<String> lazyLoadedAttributeGetMethod = Optional.empty();

    public String getClassName() {
	return className;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    public List<EnhAttribute> getEnhAttributes() {
	return enhAttributes;
    }

    public void setEnhAttributes(List<EnhAttribute> enhAttributes) {
	this.enhAttributes = enhAttributes;
    }

    public EnhEntity getMappedSuperclass() {
	return mappedSuperclass;
    }

    public void setMappedSuperclass(EnhEntity mappedSuperclass) {
	this.mappedSuperclass = mappedSuperclass;
    }

    public String getModificationAttributeGetMethod() {
	return modificationAttributeGetMethod;
    }

    public void setModificationAttributeGetMethod(String modificationAttributeGetMethod) {
	this.modificationAttributeGetMethod = modificationAttributeGetMethod;
    }

    public Optional<String> getLazyLoadedAttributeGetMethod() {
	return lazyLoadedAttributeGetMethod;
    }

    public void setLazyLoadedAttributeGetMethod(Optional<String> lazyLoadedAttributeGetMethod) {
	this.lazyLoadedAttributeGetMethod = lazyLoadedAttributeGetMethod;
    }

    public void findEmbeddables(Set<EnhEntity> embeddables) {
	for (EnhAttribute enhAttribute : enhAttributes) {
	    if (enhAttribute.isEmbedded()) {
		EnhEntity enhEntity = enhAttribute.getEmbeddedEnhEntity();
		embeddables.add(enhEntity);

		enhEntity.findEmbeddables(embeddables);
	    }
	}
    }
}
