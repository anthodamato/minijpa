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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class TableAliasGeneratorImpl implements AliasGenerator {

    /**
     * Counter for each table name. They start from one.
     */
    private final Map<String, Integer> aliasCounters = new HashMap<>();
    private final Map<String, String> tableAliasMap = new HashMap<>();

    @Override
    public String getDefault(String objectName) {
        String alias = objectName.toLowerCase() + "0";
        tableAliasMap.put(alias, objectName);
        return alias;
    }

    @Override
    public String next(String objectName) {
        Integer counter = aliasCounters.get(objectName);
        if (counter == null) {
            counter = 1;
        } else {
            ++counter;
        }

        aliasCounters.put(objectName, counter);
        String alias = objectName.toLowerCase() + counter.toString();
        tableAliasMap.put(alias, objectName);
        return alias;
    }

    @Override
    public void reset() {
        aliasCounters.clear();
    }

    @Override
    public Optional<String> findObjectNameByAlias(String alias) {
        System.out.println("TableAliasGeneratorImpl.findObjectNameByAlias: alias=" + alias);
        String tableName = tableAliasMap.get(alias);
        tableAliasMap.forEach((k, v) -> System.out.println(k + "; " + v));
        if (tableName == null) {
            return Optional.empty();
        }

        return Optional.of(tableName);
    }

    @Override
    public Optional<String> findAliasByObjectName(String objectName) {
        for (Map.Entry<String, String> entry : tableAliasMap.entrySet()) {
            if (entry.getValue().equals(objectName)) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }
}
