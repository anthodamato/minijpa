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

import java.sql.Time;
import java.util.Optional;

import org.minijpa.jdbc.DDLData;

public class OracleJdbc extends BasicDbJdbc {

//	private final NameTranslator nameTranslator = new OracleNameTranslator();
//
//	@Override
//	public NameTranslator getNameTranslator() {
//		return nameTranslator;
//	}

//	@Override
//	public String buildColumnDefinition(Class<?> type, Optional<DDLData> ddlData) {
//		if (type == Long.class || (type.isPrimitive() && type.getName().equals("long")))
//			return "number(19)";
//
//		if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int")))
//			return "number(10)";
//
//		if (type == Double.class || (type.isPrimitive() && type.getName().equals("double")))
//			return "double precision";
//
//		if (type == Float.class || (type.isPrimitive() && type.getName().equals("float")))
//			return "number(19,4)";
//
//		if (type == Boolean.class || (type.isPrimitive() && type.getName().equals("boolean")))
//			return "number(1)";
//
//		if (type == Time.class)
//			return "date";
//
//		return super.buildColumnDefinition(type, ddlData);
//	}

//	@Override
//	public String trueValue() {
//		return "1";
//	}
//
//	@Override
//	public String falseValue() {
//		return "0";
//	}

}
