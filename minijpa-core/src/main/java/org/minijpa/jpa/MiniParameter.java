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
package org.minijpa.jpa;

import javax.persistence.Parameter;

/**
 * @param <T>
 * @author adamato
 */
public class MiniParameter<T> implements Parameter<T> {

    private final String name;
    private final Integer position;
    private final Class<T> parameterType;

    public MiniParameter(String name, Integer position, Class<T> parameterType) {
        this.name = name;
        this.position = position;
        this.parameterType = parameterType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public Class<T> getParameterType() {
        return parameterType;
    }

}
