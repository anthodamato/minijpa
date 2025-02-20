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

import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaEntity;

/**
 * @author adamato
 */
public interface EntityHandler extends EntityLoader {

    void setLockType(LockType lockType);

    LockType getLockType();

    Object findById(MetaEntity metaEntityJE, Object primaryKey, LockType lockType)
            throws Exception;

//    Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType)
//            throws Exception;

    void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey,
                 LockType lockType)
            throws Exception;

    Object loadAttribute(Object parentInstance, AbstractMetaAttribute a, Object value)
            throws Exception;

    void persist(MetaEntity entity, Object entityInstance,
                 ModelValueArray<AbstractMetaAttribute> modelValueArray)
            throws Exception;

    void persistJoinTableAttributes(MetaEntity entity, Object entityInstance) throws Exception;

    void delete(Object entityInstance, MetaEntity e) throws Exception;

    void removeJoinTableRecords(Object entityInstance, MetaEntity e) throws Exception;

}
