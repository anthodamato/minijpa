package org.minijpa.jpa.metamodel;

import java.lang.reflect.Member;
import java.util.Collection;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

public class CollectionAttributeImpl<X, E> extends AttributeImpl<X, Collection<E>> implements
        CollectionAttribute<X, E> {

    private CollectionType collectionType;
    private Type<E> elementType;
    private BindableType bindableType;
    private Class<E> bindableJavaType;

    public CollectionAttributeImpl(String name, PersistentAttributeType persistentAttributeType,
                                   ManagedType<X> declaringType, Class<Collection<E>> javaType,
                                   Member javaMember, boolean isAssociation,
                                   boolean isCollection, CollectionType collectionType, Type<E> elementType,
                                   BindableType bindableType, Class<E> bindableJavaType) {
        super(name, persistentAttributeType, declaringType, javaType, javaMember,
                isAssociation, isCollection);
        this.collectionType = collectionType;
        this.elementType = elementType;
        this.bindableType = bindableType;
        this.bindableJavaType = bindableJavaType;
    }

    @Override
    public CollectionType getCollectionType() {
        return collectionType;
    }

    @Override
    public Type<E> getElementType() {
        return elementType;
    }

    @Override
    public BindableType getBindableType() {
        return bindableType;
    }

    @Override
    public Class<E> getBindableJavaType() {
        return bindableJavaType;
    }
}
