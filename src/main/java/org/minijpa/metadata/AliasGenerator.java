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

import java.util.Collection;
import java.util.Optional;

import org.minijpa.jdbc.MetaEntity;

public class AliasGenerator {

    public String calculateAlias(String tableName, Collection<MetaEntity> parsedEntities) {
	String alias = calculateBasicAlias(tableName);
	int counter = 1;
	while (true) {
	    Optional<MetaEntity> optional = aliasExists(alias, parsedEntities);
	    if (!optional.isPresent())
		return alias;

	    alias = alias + Integer.toString(counter);
	    ++counter;
	}
    }

    private String calculateBasicAlias(String tableName) {
	StringBuilder sb = new StringBuilder(tableName.substring(0, 1));
	int index = -1;
	while ((index = tableName.indexOf('_', index + 1)) != -1) {
	    if (index + 1 < tableName.length())
		sb.append(tableName.charAt(index + 1));
	}

	return sb.toString().toLowerCase();
    }

    private Optional<MetaEntity> aliasExists(String alias, Collection<MetaEntity> parsedEntities) {
	return parsedEntities.stream().filter(e -> e.getAlias().equals(alias)).findFirst();
    }

}
