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

import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;

/**
 *
 * @author adamato
 */
public interface JpaEntityLoader extends EntityLoader {

    public void setLockType(LockType lockType);

    public LockType getLockType();

    public Object findById(MetaEntity metaEntityJE, Object primaryKey, LockType lockType) throws Exception;

    public Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType) throws Exception;

    public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey, LockType lockType)
            throws Exception;

    public Object loadAttribute(Object parentInstance, MetaAttribute a, Object value) throws Exception;

}
