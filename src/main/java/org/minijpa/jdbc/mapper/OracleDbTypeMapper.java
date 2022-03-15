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

//	@Override
//	public Object convertToAttributeType(Object value, Class<?> attributeType) {
//		Object v = super.convertToAttributeType(value, attributeType);
//		if (v == null)
//			return null;
//
//		if (attributeType == Boolean.class
//				|| (attributeType.isPrimitive() && attributeType.getName().equals("boolean"))) {
//			if (value instanceof Number) {
//				return ((Number) value).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
//			}
//		}
//
//		return v;
//	}

//	@Override
//	public AttributeMapper attributeMapper(Class<?> attributeType, Class<?> databaseType) {
////		if (attributeType == LocalDate.class)
////			return localDateToTimestampAttributeMapper;
//
////		if (attributeType == LocalTime.class)
////			return localTimeToTimestampAttributeMapper;
//
////		if (attributeType == OffsetTime.class)
////			return offsetTimeToTimestampAttributeMapper;
//
////		if (attributeType == Duration.class)
////			return durationToBigDecimalAttributeMapper;
//
////		if (attributeType == Time.class)
////			return timeToTimestampAttributeMapper;
//
//		return super.attributeMapper(attributeType, databaseType);
//	}
//
//	@Override
//	public Class<?> databaseType(Class<?> attributeType, Optional<Class<?>> enumerationType) {
////		if (attributeType == java.sql.Date.class || attributeType == java.sql.Time.class)
////			return Timestamp.class;
//
//		return super.databaseType(attributeType, enumerationType);
//	}

}
