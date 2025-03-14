package org.minijpa.jpa.model;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.model.relationship.JoinColumnMapping;
import org.minijpa.jpa.model.relationship.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RelationshipMetaAttribute extends AbstractMetaAttribute {
    private static final Logger log = LoggerFactory.getLogger(RelationshipMetaAttribute.class);

    private Relationship relationship;
    private boolean collection = false;
    /**
     * If an attribute type is a collection this is the chosen implementation.
     */
    private Class<?> collectionImplementationClass;
    private Method joinColumnReadMethod;
    private Method joinColumnWriteMethod;
    private boolean id;

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

    public Method getJoinColumnReadMethod() {
        return joinColumnReadMethod;
    }

    public Method getJoinColumnWriteMethod() {
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

    public boolean isId() {
        return id;
    }

    public List<QueryParameter> queryParameters(
            Object value) throws Exception {
        List<QueryParameter> list = new ArrayList<>();
        if (getRelationship().getJoinColumnMapping() == null)
            return list;

        JoinColumnMapping joinColumnMapping = getRelationship().getJoinColumnMapping();
        Object v = joinColumnMapping.isComposite()
                ? joinColumnMapping.getForeignKey().readValue(value)
                : value;
        list.addAll(getRelationship().getJoinColumnMapping().queryParameters(v));
        return list;
    }


    public void setForeignKeyValue(
            Object entityInstance,
            Object value) throws IllegalAccessException, InvocationTargetException {
        getJoinColumnWriteMethod().invoke(entityInstance, value);
    }


    public Object getForeignKeyValue(
            Object entityInstance) throws IllegalAccessException, InvocationTargetException {
        return getJoinColumnReadMethod().invoke(entityInstance);
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
                ", collectionImplementationClass=" + collectionImplementationClass +
                ", relationship=" + relationship +
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
        private Method joinColumnReadMethod;
        private Method joinColumnWriteMethod;
        private boolean id;
        protected Field javaMember;

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

        public Builder withJoinColumnReadMethod(Method joinColumnReadMethod) {
            this.joinColumnReadMethod = joinColumnReadMethod;
            return this;
        }

        public Builder withJoinColumnWriteMethod(Method joinColumnWriteMethod) {
            this.joinColumnWriteMethod = joinColumnWriteMethod;
            return this;
        }

        public Builder isId(boolean id) {
            this.id = id;
            return this;
        }

        public Builder withJavaMember(Field field) {
            this.javaMember = field;
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
            attribute.id = id;
            attribute.javaMember = javaMember;
            return attribute;
        }

    }
}
