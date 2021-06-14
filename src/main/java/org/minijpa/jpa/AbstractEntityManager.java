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
package org.minijpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jdbc.ConnectionHolder;
import org.minijpa.metadata.PersistenceUnitContext;

public abstract class AbstractEntityManager implements EntityManager {

    protected PersistenceUnitContext persistenceUnitContext;
    protected PersistenceUnitInfo persistenceUnitInfo;
    protected MiniPersistenceContext persistenceContext;
    protected PersistenceContextType persistenceContextType = PersistenceContextType.TRANSACTION;
    protected ConnectionHolder connectionHolder;

    public MiniPersistenceContext getPersistenceContext() {
	return persistenceContext;
    }

    public ConnectionHolder getConnectionHolder() {
	return connectionHolder;
    }

    public PersistenceUnitContext getPersistenceUnitContext() {
	return persistenceUnitContext;
    }

}
