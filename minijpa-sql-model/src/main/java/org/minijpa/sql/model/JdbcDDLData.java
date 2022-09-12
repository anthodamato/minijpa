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

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JdbcDDLData {

	private final Optional<String> columnDefinition;
	private final Optional<Integer> length;
	private final Optional<Integer> precision;
	private final Optional<Integer> scale;
	private final Optional<Boolean> nullable;

	public JdbcDDLData(Optional<String> columnDefinition, Optional<Integer> length, Optional<Integer> precision,
			Optional<Integer> scale, Optional<Boolean> nullable) {
		this.columnDefinition = columnDefinition;
		this.length = length;
		this.precision = precision;
		this.scale = scale;
		this.nullable = nullable;
	}

	public Optional<String> getColumnDefinition() {
		return columnDefinition;
	}

	public Optional<Integer> getLength() {
		return length;
	}

	public Optional<Integer> getPrecision() {
		return precision;
	}

	public Optional<Integer> getScale() {
		return scale;
	}

	public Optional<Boolean> getNullable() {
		return nullable;
	}

}
