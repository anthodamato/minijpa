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

import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

public class PersistenceMetaData {

    private final Map<String, PersistenceUnitInfo> persistenceUnitMetaDatas;

    public PersistenceMetaData(Map<String, PersistenceUnitInfo> persistenceUnitMetaDatas) {
	super();
	this.persistenceUnitMetaDatas = persistenceUnitMetaDatas;
    }

    public Map<String, PersistenceUnitInfo> getPersistenceUnitMetaDatas() {
	return persistenceUnitMetaDatas;
    }

    public PersistenceUnitInfo getPersistenceUnitMetaData(String name) {
	return persistenceUnitMetaDatas.get(name);
    }
}
