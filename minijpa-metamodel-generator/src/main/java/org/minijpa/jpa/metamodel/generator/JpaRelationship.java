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
package org.minijpa.jpa.metamodel.generator;

import java.util.Optional;

public abstract class JpaRelationship {

    protected JpaEntity owningEntity;
    /**
     * This is the target entity.
     */
    protected JpaEntity attributeType;
    protected Class<?> targetEntityClass;
    protected Optional<String> mappedBy = Optional.empty();

    public JpaRelationship() {
        super();
    }

    public JpaEntity getOwningEntity() {
        return owningEntity;
    }

    public void setOwningEntity(JpaEntity owningEntity) {
        this.owningEntity = owningEntity;
    }

    public JpaEntity getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(JpaEntity attributeType) {
        this.attributeType = attributeType;
    }

    public Optional<String> getMappedBy() {
        return mappedBy;
    }

    public void setMappedBy(Optional<String> mappedBy) {
        this.mappedBy = mappedBy;
    }

    public boolean isOwner() {
        return mappedBy.isEmpty();
    }

    public boolean toMany() {
        return false;
    }

    public boolean toOne() {
        return false;
    }

    public boolean fromOne() {
        return false;
    }

    public Class<?> getTargetEntityClass() {
        return targetEntityClass;
    }

    public void setTargetEntityClass(Class<?> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

}
