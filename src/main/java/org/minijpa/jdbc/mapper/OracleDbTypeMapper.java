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

public class OracleDbTypeMapper extends AbstractDbTypeMapper {

    @Override
    public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
	if (value == null)
	    return null;

	if (value instanceof Number) {
	    if (attributeType == Boolean.class || (attributeType.isPrimitive() && attributeType.getName().equals("boolean"))) {
		int v = ((Number) value).intValue();
		return v == 0 ? Boolean.FALSE : Boolean.TRUE;
	    }
	}

	return super.convert(value, readWriteDbType, attributeType);
    }
}
