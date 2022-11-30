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

import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class QueryResultMapping {

    private String name;
    private final List<EntityMapping> entityMappings;
    private final List<ConstructorMapping> constructorMappings;
    private final List<SingleColumnMapping> singleColumnMappings;

    public QueryResultMapping(String name, List<EntityMapping> entityMappings,
            List<ConstructorMapping> constructorMappings, List<SingleColumnMapping> singleColumnMappings) {
        this.entityMappings = entityMappings;
        this.constructorMappings = constructorMappings;
        this.singleColumnMappings = singleColumnMappings;
    }

    public String getName() {
        return name;
    }

    public List<EntityMapping> getEntityMappings() {
        return entityMappings;
    }

    public List<ConstructorMapping> getConstructorMappings() {
        return constructorMappings;
    }

    public List<SingleColumnMapping> getSingleColumnMappings() {
        return singleColumnMappings;
    }

    public int size() {
        return entityMappings.size() + constructorMappings.size() + singleColumnMappings.size();
    }
}
