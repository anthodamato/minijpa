/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.metamodel;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class IdSingularAttribute<X, T> implements SingularAttribute<X, T> {
    private String name;
    private ManagedType<X> declaringType;
    private Class<T> javaType;
    private Member javaMember;
    private Class<T> bindableJavaType;
    private Type<T> type;

    public IdSingularAttribute(
            String name,
            ManagedType<X> declaringType,
            Class<T> javaType,
            Member javaMember,
            Class<T> bindableJavaType,
            Type<T> type) {
        super();
        this.name = name;
        this.declaringType = declaringType;
        this.javaType = javaType;
        this.javaMember = javaMember;
        this.bindableJavaType = bindableJavaType;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PersistentAttributeType getPersistentAttributeType() {
        return PersistentAttributeType.BASIC;
    }

    @Override
    public ManagedType<X> getDeclaringType() {
        return declaringType;
    }

    void setDeclaringType(ManagedType<X> managedType) {
        this.declaringType = managedType;
    }

    @Override
    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public Member getJavaMember() {
        return javaMember;
    }

    @Override
    public boolean isAssociation() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public BindableType getBindableType() {
        return BindableType.SINGULAR_ATTRIBUTE;
    }

    @Override
    public Class<T> getBindableJavaType() {
        return bindableJavaType;
    }

    @Override
    public boolean isId() {
        return true;
    }

    @Override
    public boolean isVersion() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    public Type<T> getType() {
        return type;
    }

}
