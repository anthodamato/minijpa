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
package org.minijpa.sql.model;

import java.util.Optional;

public class FromTableImpl implements FromTable {

	private String name;
	private Optional<String> alias = Optional.empty();
//    private Optional<List<FromJoin>> fromJoins = Optional.empty();

	public FromTableImpl(String name) {
		super();
		this.name = name;
	}

	public FromTableImpl(String name, String alias) {
		super();
		this.name = name;
		this.alias = Optional.of(alias);
	}

//    public FromTableImpl(String name, String alias, List<FromJoin> fromJoins) {
//	super();
//	this.name = name;
//	this.alias = Optional.of(alias);
//	this.fromJoins = Optional.of(Collections.unmodifiableList(fromJoins));
//    }

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<String> getAlias() {
		return alias;
	}

//    @Override
//    public Optional<List<FromJoin>> getJoins() {
//	return fromJoins;
//    }

}
