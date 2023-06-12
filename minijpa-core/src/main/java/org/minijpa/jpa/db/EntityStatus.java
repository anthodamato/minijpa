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

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public enum EntityStatus {
    /**
     * This status is set when the entity instance is created.
     */
    NEW,
    /**
     * The EntityManager.persist method has been called,
     * the entity instance has been added to the persistence context but the entity is not flushed yet.
     */
    PERSIST_NOT_FLUSHED,
    /**
     * The entity instance is flushed.
     */
    FLUSHED,
    /**
     * In this case some data are missing, for example the join columns
     */
    PARTIALLY_FLUSHED,
    /**
     * The entity is detached.
     */
    DETACHED,
    /**
     * The entity already exists on db,
     * it's added in the persistence context using, for example, the EntityManager.find method
     */
    FLUSHED_LOADED_FROM_DB,
    /**
     * The entity instance has been removed from persistence context using the EntityManager.remove method
     * but it's not flushed yet.
     */
    REMOVED_NOT_FLUSHED,
    /**
     * The entity instance has been removed from persistence context and from db.
     */
    REMOVED,
    /**
     * This status is used for entities related to join columns.
     * When an entity instance is removed from db the join column entity must be removed first.
     */
    EARLY_REMOVE,
    /**
     * This status is used for entities related to join columns.
     * When an entity instance is inserted into db the join column entity must be inserted first.
     */
    EARLY_INSERT;
}
