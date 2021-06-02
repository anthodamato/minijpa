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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;

public class MariaDBDbTypeMapper extends AbstractDbTypeMapper {

    @Override
    public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
	if (attributeType == LocalDate.class) {
	    if (readWriteDbType == Date.class && value != null) {
		Date date = (Date) value;
		return new java.sql.Date(date.getTime()).toLocalDate();
	    }

	    if (readWriteDbType == java.sql.Date.class && value != null) {
		java.sql.Date date = (java.sql.Date) value;
		return date.toLocalDate();
	    }
	}

	if (attributeType == OffsetDateTime.class && readWriteDbType == Timestamp.class && value != null) {
	    Timestamp date = (Timestamp) value;
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
	    return OffsetDateTime.ofInstant(date.toInstant(), calendar.getTimeZone().toZoneId());
	}

	return super.convert(value, readWriteDbType, attributeType);
    }

}
