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
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public enum EntityStatus {
    NEW, PERSIST_NOT_FLUSHED,
    FLUSHED,
    PARTIALLY_FLUSHED, // in this case some data are missing, for example the join columns
    DETACHED, FLUSHED_LOADED_FROM_DB,
    REMOVED_NOT_FLUSHED, REMOVED, EARLY_REMOVE,
    EARLY_INSERT;
}
