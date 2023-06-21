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
package org.minijpa.jpa.db.querymapping;

import java.util.List;
import java.util.Optional;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ConstructorMapping {

    private final Class<?> targetClass;
    private final List<SingleColumnMapping> singleColumnMappings;

    public ConstructorMapping(Class<?> targetClass, List<SingleColumnMapping> singleColumnMappings) {
        this.targetClass = targetClass;
        this.singleColumnMappings = singleColumnMappings;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public List<SingleColumnMapping> getSingleColumnMappings() {
        return singleColumnMappings;
    }


    public Optional<SingleColumnMapping> findColumnMapping(String columnName) {
        for (SingleColumnMapping singleColumnMapping : singleColumnMappings) {
            if (singleColumnMapping.getName().equalsIgnoreCase(columnName))
                return Optional.of(singleColumnMapping);
        }

        return Optional.empty();
    }
}
