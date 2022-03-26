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
package org.minijpa.jdbc.mapper;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class StringEnumAttributeMapper implements AttributeMapper<Enum, String> {

    private final Class<?> attributeType;

    public StringEnumAttributeMapper(Class<?> attributeType) {
	this.attributeType = attributeType;
    }

    @Override
    public String attributeToDatabase(Enum k) {
	return k.name();
    }

    @Override
    public Enum databaseToAttribute(String v) {
	return Enum.valueOf((Class<Enum>) attributeType, v);
    }

}
