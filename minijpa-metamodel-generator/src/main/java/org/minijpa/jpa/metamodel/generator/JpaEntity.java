package org.minijpa.jpa.metamodel.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JpaEntity {
    private String name;
    private Class<?> type;
    private String className;
    private List<JpaAttribute> attributes = new ArrayList<>();
    private Optional<JpaEntity> mappedSuperclass = Optional.empty();
    private List<JpaEntity> embeddables = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Optional<JpaEntity> getMappedSuperclass() {
        return mappedSuperclass;
    }

    public void setMappedSuperclass(Optional<JpaEntity> mappedSuperclass) {
        this.mappedSuperclass = mappedSuperclass;
    }

    public List<JpaAttribute> getAttributes() {
        return attributes;
    }

    public void addAttribute(JpaAttribute attribute) {
        attributes.add(attribute);
    }

    public List<JpaEntity> getEmbeddables() {
        return embeddables;
    }

    public void addEmbeddable(JpaEntity entity) {
        embeddables.add(entity);
    }
}
