package org.minijpa.jpa.model.relationship;

public class ToManyRelationship extends Relationship {
    protected Class<?> collectionClass;

    public ToManyRelationship() {
        super();
    }

    @Override
    public boolean toMany() {
        return true;
    }

    public Class<?> getCollectionClass() {
        return collectionClass;
    }

    public void setCollectionClass(Class<?> collectionClass) {
        this.collectionClass = collectionClass;
    }
}
