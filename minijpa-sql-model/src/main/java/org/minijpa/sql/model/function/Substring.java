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
public class Substring implements Function, Value {

    private final Object argument;
    private final Object startIndex;
    private Optional<Object> length = Optional.empty();

    public Substring(Object argument, Object startIndex) {
        this.argument = argument;
        this.startIndex = startIndex;
    }

    public Substring(Object argument, Object startIndex, Optional<Object> length) {
        this.argument = argument;
        this.startIndex = startIndex;
        this.length = length;
    }

    public Object getArgument() {
        return argument;
    }

    public Object getStartIndex() {
        return startIndex;
    }

    public Optional<Object> getLength() {
        return length;
    }

    public static class Builder {
        private Object argument;
        private Object startIndex;
        private Optional<Object> len = Optional.empty();

        public Builder withArgument(Object argument) {
            this.argument = argument;
            return this;
        }

        public Builder withFrom(Object startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public Builder withLen(Optional<Object> len) {
            this.len = len;
            return this;
        }

        public Substring build() {
            return new Substring(argument, startIndex, len);
        }
    }

}
