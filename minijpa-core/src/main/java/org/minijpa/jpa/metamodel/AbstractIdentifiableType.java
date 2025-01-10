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

import java.util.Set;

import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public abstract class AbstractIdentifiableType<X> extends AbstractManagedType<X> implements
        IdentifiableType<X> {

    protected SingularAttribute id;
    protected SingularAttribute declaredId;
    protected SingularAttribute version;
    protected SingularAttribute declaredVersion;
    protected IdentifiableType<? super X> superType;
    protected boolean singleIdAttribute = true;
    protected boolean versionAttribute = false;
    protected Set<SingularAttribute<? super X, ?>> idClassAttributes;

    @Override
    public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
        if (type != id.getJavaType()) {
            throw new IllegalArgumentException("Expected type id: " + id.getJavaType().getName());
        }

        return id;
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
        if (type != declaredId.getJavaType()) {
            throw new IllegalArgumentException(
                    "Expected declared type id: " + declaredId.getJavaType().getName());
        }

        return declaredId;
    }

    @Override
    public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
        if (version == null) {
            return null;
        }

        if (type != version.getJavaType()) {
            throw new IllegalArgumentException(
                    "Expected type version: " + version.getJavaType().getName());
        }

        return version;
    }

    @Override
    public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
        if (type != declaredVersion.getJavaType()) {
            throw new IllegalArgumentException(
                    "Expected declared type version: " + declaredVersion.getJavaType().getName());
        }

        return declaredVersion;
    }

    @Override
    public IdentifiableType<? super X> getSupertype() {
        return superType;
    }

    @Override
    public boolean hasSingleIdAttribute() {
        return singleIdAttribute;
    }

    @Override
    public boolean hasVersionAttribute() {
        return versionAttribute;
    }

    @Override
    public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
        if (hasSingleIdAttribute())
            throw new IllegalArgumentException("Not an IdClass composite key");

        return idClassAttributes;
    }

    @Override
    public Type<?> getIdType() {
        return id.getType();
    }

}
