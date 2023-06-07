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
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

/**
 * @author adamato
 */
public interface EntityHandler extends EntityLoader {

  public void setLockType(LockType lockType);

  public LockType getLockType();

  public Object findById(MetaEntity metaEntityJE, Object primaryKey, LockType lockType)
      throws Exception;

  public Object queryVersionValue(MetaEntity metaEntity, Object primaryKey, LockType lockType)
      throws Exception;

  public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey,
      LockType lockType)
      throws Exception;

  public Object loadAttribute(Object parentInstance, AbstractMetaAttribute a, Object value)
      throws Exception;

  public void persist(MetaEntity entity, Object entityInstance,
      ModelValueArray<AbstractMetaAttribute> modelValueArray)
      throws Exception;

  public void persistJoinTableAttributes(MetaEntity entity, Object entityInstance) throws Exception;

  public void delete(Object entityInstance, MetaEntity e) throws Exception;

  public void removeJoinTableRecords(Object entityInstance, MetaEntity e) throws Exception;

}
