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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConfigurationList {

	private Logger LOG = LoggerFactory.getLogger(DbConfigurationList.class);
	private static final DbConfigurationList dbConfiguration = new DbConfigurationList();

	private final Map<String, DbConfiguration> map = new HashMap<>();

	public static DbConfigurationList getInstance() {
		return dbConfiguration;
	}

	public DbConfiguration getDbConfiguration(String persistenceUnitName) {
		return map.get(persistenceUnitName);
	}

	public void setDbConfiguration(String persistenceUnitName, DbConfiguration dbConfiguration) {
		LOG.debug("Db Configuration -> Persistence Unit Name = {}", persistenceUnitName);
		map.put(persistenceUnitName, dbConfiguration);
	}
}
