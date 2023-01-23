package org.minijpa.jpa.metamodel.generator;

import java.util.Optional;

public class JpaAttribute {
    private String name;
    private Class<?> type;
    private Optional<JpaRelationship> relationship = Optional.empty();

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

    public Optional<JpaRelationship> getRelationship() {
        return relationship;
    }

    public void setRelationship(Optional<JpaRelationship> jpaRelationship) {
        this.relationship = jpaRelationship;
    }

}
