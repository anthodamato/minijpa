package org.minijpa.jpa.metamodel.generator;

import java.util.ArrayList;
import java.util.List;

public class EntityMetadata {
    private String path;
    private String packagePath;
    private String className;
    private String entityClassName;
    private List<AttributeElement> attributeElements = new ArrayList<>();

    public void addAttribute(AttributeElement attributeElement) {
        attributeElements.add(attributeElement);
    }

    public List<AttributeElement> getAttributeElements() {
        return attributeElements;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

}
