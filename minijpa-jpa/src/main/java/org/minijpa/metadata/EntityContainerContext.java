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
package org.minijpa.metadata;

import org.minijpa.jdbc.EntityLoader;

import org.minijpa.jpa.db.EntityContainer;

public class EntityContainerContext {

	private final PersistenceUnitContext persistenceUnitContext;
	private final EntityContainer entityContainer;
	private final EntityLoader entityLoader;

	public EntityContainerContext(PersistenceUnitContext persistenceUnitContext, EntityContainer entityContainer,
			EntityLoader entityLoader) {
		super();
		this.persistenceUnitContext = persistenceUnitContext;
		this.entityContainer = entityContainer;
		this.entityLoader = entityLoader;
	}

	public EntityContainer getEntityContainer() {
		return entityContainer;
	}

	public EntityLoader getEntityLoader() {
		return entityLoader;
	}

	public boolean isManaged(Object entityInstance) throws Exception {
		return entityContainer.isManaged(entityInstance);
	}
}
