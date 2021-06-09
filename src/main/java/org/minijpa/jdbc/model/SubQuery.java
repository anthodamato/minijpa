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

import java.util.Optional;

public class SubQuery {

    private SqlSelect query;
    private Optional<String> alias = Optional.empty();

    public SubQuery(SqlSelect query, String alias) {
	super();
	this.query = query;
	if (alias != null)
	    this.alias = Optional.of(alias);
    }

    public Optional<String> getAlias() {
	return alias;
    }

    public SqlSelect getQuery() {
	return query;
    }

}
