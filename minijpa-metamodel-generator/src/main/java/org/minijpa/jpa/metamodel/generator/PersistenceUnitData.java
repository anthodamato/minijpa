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
package org.minijpa.jpa.metamodel.generator;

import java.util.List;

public class PersistenceUnitData {

    private String name;
    private List<String> managedClassNames;

    public PersistenceUnitData() {
        super();
    }

    public String getName() {
        return name;
    }

    public String getPersistenceUnitName() {
        return name;
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public static class Builder {

        private String name;
        private List<String> managedClassNames;

        public Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withManagedClassNames(List<String> managedClassNames) {
            this.managedClassNames = managedClassNames;
            return this;
        }

        public PersistenceUnitData build() {
            PersistenceUnitData impl = new PersistenceUnitData();
            impl.name = name;
            impl.managedClassNames = managedClassNames;
            return impl;
        }
    }
}
