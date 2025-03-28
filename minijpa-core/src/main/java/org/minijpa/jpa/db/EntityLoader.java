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

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.model.MetaEntity;

/**
 * @author adamato
 */
public interface EntityLoader {

  Object build(ModelValueArray<FetchParameter> modelValueArray, MetaEntity entity)
      throws Exception;

  /**
   * Build the entity instance.
   *
   * @param modelValueArray
   * @param entity
   * @return
   * @throws Exception
   */
//  public Object buildNoQueries(
//          ModelValueArray<FetchParameter> modelValueArray,
//          MetaEntity entity,
//          LockType lockType)
//      throws Exception;

//  public Object buildEntityByValues(
//          ModelValueArray<FetchParameter> modelValueArray,
//          MetaEntity entity,
//          LockType lockType,
//          EntityBuilder entityBuilder) throws Exception;

    /**
     * Build the entity instance. Relationship attributes are not loaded. Useful in case of Fetch
     * Join.
     *
     * @param modelValueArray
     * @param entity
     * @return
     * @throws Exception
     */
  public Object buildEntityNoRelationshipAttributeLoading(
      ModelValueArray<FetchParameter> modelValueArray,
      MetaEntity entity)
      throws Exception;

}
