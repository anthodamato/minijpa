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

import org.minijpa.jdbc.PkSequenceGenerator;

public class PkGeneration {

	private PkStrategy pkStrategy = PkStrategy.PLAIN;
	private String generator;
	private PkSequenceGenerator pkSequenceGenerator;

	public PkStrategy getPkStrategy() {
		return pkStrategy;
	}

	public void setPkStrategy(PkStrategy pkStrategy) {
		this.pkStrategy = pkStrategy;
	}

	public void setGenerator(String generator) {
		this.generator = generator;
	}

	public String getGenerator() {
		return generator;
	}

	public PkSequenceGenerator getPkSequenceGenerator() {
		return pkSequenceGenerator;
	}

	public void setPkSequenceGenerator(PkSequenceGenerator pkSequenceGenerator) {
		this.pkSequenceGenerator = pkSequenceGenerator;
	}

}
