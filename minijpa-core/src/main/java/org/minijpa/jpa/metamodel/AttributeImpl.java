package org.minijpa.jpa.metamodel;

import java.lang.reflect.Member;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

public class AttributeImpl<X, Y> implements Attribute<X, Y> {

  private String name;
  private PersistentAttributeType persistentAttributeType = PersistentAttributeType.BASIC;
  private ManagedType<X> declaringType;
  private Class<Y> javaType;
  private Member javaMember;
  private boolean isAssociation;
  private boolean isCollection;

  public AttributeImpl(String name, PersistentAttributeType persistentAttributeType,
      ManagedType<X> declaringType, Class<Y> javaType, Member javaMember,
      boolean isAssociation, boolean isCollection) {
    this.name = name;
    this.persistentAttributeType = persistentAttributeType;
    this.declaringType = declaringType;
    this.javaType = javaType;
    this.javaMember = javaMember;
    this.isAssociation = isAssociation;
    this.isCollection = isCollection;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public PersistentAttributeType getPersistentAttributeType() {
    return persistentAttributeType;
  }

  @Override
  public ManagedType<X> getDeclaringType() {
    return declaringType;
  }

  @Override
  public Class<Y> getJavaType() {
    return javaType;
  }

  @Override
  public Member getJavaMember() {
    return javaMember;
  }

  @Override
  public boolean isAssociation() {
    return isAssociation;
  }

  @Override
  public boolean isCollection() {
    return isCollection;
  }
}
