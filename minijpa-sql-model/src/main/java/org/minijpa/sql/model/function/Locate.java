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
package org.minijpa.sql.model.function;

import java.util.Optional;

import org.minijpa.sql.model.Value;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class Locate implements Function, Value {

    private final Object searchString;
    private final Object inputString;
    private Optional<Object> position = Optional.empty();

    public Locate(Object searchString, Object inputString) {
        this.searchString = searchString;
        this.inputString = inputString;
    }

    public Locate(Object searchString, Object inputString, Optional<Object> position) {
        this.searchString = searchString;
        this.inputString = inputString;
        this.position = position;
    }

    public Object getSearchString() {
        return searchString;
    }

    public Object getInputString() {
        return inputString;
    }

    public Optional<Object> getPosition() {
        return position;
    }

    public static class Builder {
        private Object searchString;
        private Object inputString;
        private Optional<Object> position = Optional.empty();

        public Builder withSearchString(Object searchString) {
            this.searchString = searchString;
            return this;
        }

        public Builder withInputString(Object inputString) {
            this.inputString = inputString;
            return this;
        }

        public Builder withPosition(Optional<Object> position) {
            this.position = position;
            return this;
        }

        public Locate build() {
            return new Locate(searchString, inputString, position);
        }
    }
}
