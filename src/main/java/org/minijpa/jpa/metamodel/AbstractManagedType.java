package org.minijpa.jpa.metamodel;

import java.util.Optional;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

public class AbstractManagedType<X> implements ManagedType<X> {

    protected Set<Attribute<? super X, ?>> attributes;
    protected Set<Attribute<X, ?>> declaredAttributes;
    protected Set<SingularAttribute<? super X, ?>> singularAttributes;
    protected Set<SingularAttribute<X, ?>> declaredSingularAttributes;
    protected PersistenceType persistenceType;
    protected Class<X> javaType;

    @Override
    public Set<Attribute<? super X, ?>> getAttributes() {
	return attributes;
    }

    @Override
    public Set<Attribute<X, ?>> getDeclaredAttributes() {
	return declaredAttributes;
    }

    @Override
    public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
	Optional<SingularAttribute<? super X, ?>> optional = singularAttributes.stream()
		.filter(a -> a.getName().equals(name) && a.getJavaType() == type).findFirst();
	if (optional.isPresent())
	    return (SingularAttribute<? super X, Y>) optional.get();

	throw new IllegalArgumentException(
		"Singular Attribute '" + name + "' not found (type='" + type.getName() + "')");
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
	Optional<SingularAttribute<X, ?>> optional = declaredSingularAttributes.stream()
		.filter(a -> a.getName().equals(name) && a.getJavaType() == type).findFirst();
	if (optional.isPresent())
	    return (SingularAttribute<X, Y>) optional.get();

	throw new IllegalArgumentException(
		"Declared Singular Attribute '" + name + "' not found (type='" + type.getName() + "')");
    }

    @Override
    public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
	return singularAttributes;
    }

    @Override
    public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
	return declaredSingularAttributes;
    }

    @Override
    public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Attribute<? super X, ?> getAttribute(String name) {
	Optional<Attribute<? super X, ?>> optional = attributes.stream().filter(a -> a.getName().equals(name))
		.findFirst();
	if (optional.isPresent())
	    return optional.get();

	throw new IllegalArgumentException("Attribute '" + name + "' not found");
    }

    @Override
    public Attribute<X, ?> getDeclaredAttribute(String name) {
	Optional<Attribute<X, ?>> optional = declaredAttributes.stream().filter(a -> a.getName().equals(name))
		.findFirst();
	if (optional.isPresent())
	    return optional.get();

	throw new IllegalArgumentException("Declared Attribute '" + name + "' not found");
    }

    @Override
    public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
	Optional<SingularAttribute<? super X, ?>> optional = singularAttributes.stream()
		.filter(a -> a.getName().equals(name)).findFirst();
	if (optional.isPresent())
	    return optional.get();

	throw new IllegalArgumentException("Singular Attribute '" + name + "' not found");
    }

    @Override
    public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
	Optional<SingularAttribute<X, ?>> optional = declaredSingularAttributes.stream()
		.filter(a -> a.getName().equals(name)).findFirst();
	if (optional.isPresent())
	    return (SingularAttribute<X, ?>) optional.get();

	throw new IllegalArgumentException("Declared Singular Attribute '" + name + "' not found");
    }

    @Override
    public CollectionAttribute<? super X, ?> getCollection(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public SetAttribute<? super X, ?> getSet(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public SetAttribute<X, ?> getDeclaredSet(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ListAttribute<? super X, ?> getList(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ListAttribute<X, ?> getDeclaredList(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MapAttribute<? super X, ?, ?> getMap(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PersistenceType getPersistenceType() {
	return persistenceType;
    }

    @Override
    public Class<X> getJavaType() {
	return javaType;
    }

}
