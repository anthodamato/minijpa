package org.minijpa.jpa.model;

import org.minijpa.jpa.model.relationship.Relationship;

import java.lang.reflect.Method;
import java.util.Optional;

public class RelationshipMetaAttribute extends AbstractMetaAttribute {

    private Relationship relationship;
    private boolean collection = false;
    /**
     * If an attribute type is a collection this is the chosen implementation.
     */
    private Class<?> collectionImplementationClass;
    private Optional<Method> joinColumnReadMethod = Optional.empty();
    private Optional<Method> joinColumnWriteMethod = Optional.empty();

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public boolean isCollection() {
        return collection;
    }

    public Class<?> getCollectionImplementationClass() {
        return collectionImplementationClass;
    }

    public Optional<Method> getJoinColumnReadMethod() {
        return joinColumnReadMethod;
    }

    public Optional<Method> getJoinColumnWriteMethod() {
        return joinColumnWriteMethod;
    }

    @Override
    public boolean isEager() {
        return !relationship.isLazy();
    }

    @Override
    public boolean isLazy() {
        return relationship.isLazy();
    }

    @Override
    public String toString() {
        return "RelationshipMetaAttribute{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", readMethod=" + readMethod +
                ", writeMethod=" + writeMethod +
                ", nullable=" + nullable +
                ", columnName='" + columnName + '\'' +
                ", type=" + type +
                ", sqlType=" + sqlType +
                ", databaseType=" + databaseType +
                '}';
    }

    public static class Builder {

        private String name;
        private Class<?> type;
        private Method readMethod;
        private Method writeMethod;
        private Integer sqlType;
        private Relationship relationship;
        private boolean collection = false;
        private Class<?> collectionImplementationClass;
        private boolean nullable = true;
        private Optional<Method> joinColumnReadMethod;
        private Optional<Method> joinColumnWriteMethod;

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withType(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder withReadMethod(Method readMethod) {
            this.readMethod = readMethod;
            return this;
        }

        public Builder withWriteMethod(Method writeMethod) {
            this.writeMethod = writeMethod;
            return this;
        }

        public Builder withRelationship(Relationship relationship) {
            this.relationship = relationship;
            return this;
        }

        public Builder isCollection(boolean collection) {
            this.collection = collection;
            return this;
        }

        public Builder withCollectionImplementationClass(Class<?> collectionImplementationClass) {
            this.collectionImplementationClass = collectionImplementationClass;
            return this;
        }

        public Builder isNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public Builder withJoinColumnReadMethod(Optional<Method> joinColumnReadMethod) {
            this.joinColumnReadMethod = joinColumnReadMethod;
            return this;
        }

        public Builder withJoinColumnWriteMethod(Optional<Method> joinColumnWriteMethod) {
            this.joinColumnWriteMethod = joinColumnWriteMethod;
            return this;
        }

        public RelationshipMetaAttribute build() {
            RelationshipMetaAttribute attribute = new RelationshipMetaAttribute();
            attribute.name = name;
            attribute.type = type;
            attribute.readMethod = readMethod;
            attribute.writeMethod = writeMethod;
            attribute.sqlType = sqlType;
            attribute.relationship = relationship;
            attribute.collection = collection;
            attribute.collectionImplementationClass = collectionImplementationClass;
            attribute.nullable = nullable;
            attribute.joinColumnReadMethod = joinColumnReadMethod;
            attribute.joinColumnWriteMethod = joinColumnWriteMethod;
            return attribute;
        }

    }
}
