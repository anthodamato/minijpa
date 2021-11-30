/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.minijpa.jdbc.model.function;

import java.util.Optional;
import org.minijpa.jdbc.model.Value;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class Trim implements Function, Value {

	private Optional<TrimType> trimType = Optional.empty();
	private final Object argument;
	private String trimCharacter;

	public Trim(Object argument) {
		this.argument = argument;
	}

	public Trim(Object argument, String trimCharacter) {
		this.argument = argument;
		this.trimCharacter = trimCharacter;
	}

	public Trim(Object argument, Optional<TrimType> trimType) {
		this.argument = argument;
		this.trimType = trimType;
	}

	public Trim(Object argument, Optional<TrimType> trimType, String trimCharacter) {
		this.argument = argument;
		this.trimType = trimType;
		this.trimCharacter = trimCharacter;
	}

	public Optional<TrimType> getTrimType() {
		return trimType;
	}

	public Object getArgument() {
		return argument;
	}

	public String getTrimCharacter() {
		return trimCharacter;
	}

}
