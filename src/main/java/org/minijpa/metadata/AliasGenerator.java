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
package org.minijpa.metadata;

import java.util.Optional;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface AliasGenerator {

	/**
	 * Returns the default alias for the given table.
	 *
	 * @param objectName database object name, can be a table name or column name
	 * @return the current alias
	 */
	public String getDefault(String objectName);

	/**
	 * Calculate the next alias.
	 *
	 * @param objectName database object name, can be a table name or column name
	 * @return the next alias
	 */
	public String next(String objectName);

	/**
	 * Restarts the next counter. If the 'next' method is called this method set to zero the counter so the next alias
	 * will be the second one.
	 */
	public void reset();

	/**
	 * Finds the table or column name by its associated alias.
	 *
	 * @param alias the alias to find by
	 * @return the table or column name associated
	 */
	public Optional<String> findObjectNameByAlias(String alias);

	public Optional<String> findAliasByObjectName(String objectName);
}
