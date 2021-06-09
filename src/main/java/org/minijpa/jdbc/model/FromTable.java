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
package org.minijpa.jdbc.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.model.join.FromJoin;

public interface FromTable {

    public String getName();

    public Optional<String> getAlias();

    public Optional<List<FromJoin>> getJoins();

    public static FromTable of(MetaEntity entity) {
	return new FromTableImpl(entity.getTableName(), entity.getAlias());
    }

    public static FromTable of(MetaEntity entity, FromJoin fromJoin) {
	return new FromTableImpl(entity.getTableName(), entity.getAlias(), Arrays.asList(fromJoin));
    }

    public static FromTable of(String tableName) {
	return new FromTableImpl(tableName);
    }
}
