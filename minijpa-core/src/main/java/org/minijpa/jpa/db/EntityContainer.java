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
package org.minijpa.jpa.db;

import org.minijpa.jpa.model.MetaEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EntityContainer {

    /**
     * Finds an entity instance.
     *
     * @param entityClass
     * @param primaryKey
     * @return
     * @throws Exception
     */
    public Object find(Class<?> entityClass, Object primaryKey) throws Exception;

    public void addManaged(Object entityInstance, Object idValue) throws Exception;

    public void removeManaged(Object entityInstance) throws Exception;

    public void markForRemoval(Object entityInstance) throws Exception;

    public List<Object> getManagedEntityList();

    public boolean isManaged(Object entityInstance) throws Exception;

    public boolean isManaged(Collection<?> entityInstanceList) throws Exception;

    public void close();

    public void detach(Object entityInstance) throws Exception;

    public void detachAll() throws Exception;

    public void resetLockType();

    public Optional<MetaEntity> isManagedClass(Class<?> c);
}
