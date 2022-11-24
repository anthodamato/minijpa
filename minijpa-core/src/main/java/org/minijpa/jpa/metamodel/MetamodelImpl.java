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

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

public class MetamodelImpl implements Metamodel {

    private Set<ManagedType<?>> managedTypes;
    private Set<EntityType<?>> entityTypes;
    private Set<EmbeddableType<?>> embeddableTypes;

    public MetamodelImpl(Set<ManagedType<?>> managedTypes, Set<EntityType<?>> entityTypes,
            Set<EmbeddableType<?>> embeddableTypes) {
        super();
        this.managedTypes = managedTypes;
        this.entityTypes = entityTypes;
        this.embeddableTypes = embeddableTypes;
    }

    @Override
    public <X> EntityType<X> entity(Class<X> cls) {
        for (EntityType entityType : entityTypes) {
            if (entityType.getJavaType() == cls)
                return entityType;
        }

        throw new IllegalArgumentException("Type '" + cls.getName() + "' is not an entity");
    }

    @Override
    public <X> ManagedType<X> managedType(Class<X> cls) {
        for (ManagedType managedType : managedTypes) {
            if (managedType.getJavaType() == cls)
                return managedType;
        }

        throw new IllegalArgumentException("Type '" + cls.getName() + "' is not a managed type");
    }

    @Override
    public <X> EmbeddableType<X> embeddable(Class<X> cls) {
        for (EmbeddableType embeddableType : embeddableTypes) {
            if (embeddableType.getJavaType() == cls)
                return embeddableType;
        }

        throw new IllegalArgumentException("Type '" + cls.getName() + "' is not an embeddable type");
    }

    @Override
    public Set<ManagedType<?>> getManagedTypes() {
        return managedTypes;
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        return entityTypes;
    }

    @Override
    public Set<EmbeddableType<?>> getEmbeddables() {
        return embeddableTypes;
    }

}
