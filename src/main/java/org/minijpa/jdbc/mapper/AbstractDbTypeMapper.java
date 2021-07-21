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

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import org.minijpa.jdbc.DbTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDbTypeMapper implements DbTypeMapper {

    private Logger LOG = LoggerFactory.getLogger(AbstractDbTypeMapper.class);

    // attribute converters
    private final AttributeMapper offsetTimeAttributeMapper = new OffsetTimeAttributeMapper();
    private final AttributeMapper offsetDateTimeAttributeMapper = new OffsetDateTimeAttributeMapper();
    private final AttributeMapper durationAttributeMapper = new DurationAttributeMapper();
    private final AttributeMapper instantAttributeMapper = new InstantAttributeMapper();
    private final AttributeMapper localDateAttributeMapper = new LocalDateAttributeMapper();
    protected final AttributeMapper localDateToTimestampAttributeMapper = new LocalDateToTimestampAttributeMapper();
    protected final AttributeMapper localTimeToTimestampAttributeMapper = new LocalTimeToTimestampAttributeMapper();
    protected final AttributeMapper offsetTimeToTimestampAttributeMapper = new OffsetTimeToTimestampAttributeMapper();
    protected final AttributeMapper durationToBigDecimalAttributeMapper = new DurationToBigDecimalAttributeMapper();
    protected final AttributeMapper timeToTimestampAttributeMapper = new TimeToTimestampAttributeMapper();
    private final AttributeMapper localDateTimeAttributeMapper = new LocalDateTimeAttributeMapper();
    private final AttributeMapper localTimeAttributeMapper = new LocalTimeAttributeMapper();
    private final AttributeMapper utilDateAttributeMapper = new UtilDateAttributeMapper();
    private final AttributeMapper zonedDateTimeAttributeMapper = new ZonedDateTimeAttributeMapper();
    private final AttributeMapper calendarAttributeMapper = new CalendarAttributeMapper();

    @Override
    public AttributeMapper attributeMapper(Class<?> attributeType, Class<?> databaseType) {
	if (attributeType.isEnum() && databaseType == String.class)
	    return new StringEnumAttributeMapper(attributeType);

	if (attributeType.isEnum() && databaseType == Integer.class)
	    return new OrdinalEnumAttributeMapper(attributeType);

	if (attributeType == Duration.class)
	    return durationAttributeMapper;

	if (attributeType == Instant.class)
	    return instantAttributeMapper;

	if (attributeType == LocalDate.class)
	    return localDateAttributeMapper;

	if (attributeType == LocalDateTime.class)
	    return localDateTimeAttributeMapper;

	if (attributeType == LocalTime.class)
	    return localTimeAttributeMapper;

	if (attributeType == OffsetTime.class)
	    return offsetTimeAttributeMapper;

	if (attributeType == OffsetDateTime.class)
	    return offsetDateTimeAttributeMapper;

	if (attributeType == java.util.Date.class)
	    return utilDateAttributeMapper;

	if (attributeType == ZonedDateTime.class)
	    return zonedDateTimeAttributeMapper;

	if (attributeType == Calendar.class)
	    return calendarAttributeMapper;

	return null;
    }

    @Override
    public Class<?> map(Class<?> attributeType, Integer jdbcType) {
	if (attributeType == LocalDate.class)
	    return java.util.Date.class;

	if (attributeType == OffsetDateTime.class || attributeType == java.util.Date.class)
	    return Timestamp.class;

	if (attributeType.isEnum() && jdbcType == Types.VARCHAR)
	    return String.class;

	if (attributeType.isEnum() && jdbcType == Types.INTEGER)
	    return Integer.class;

	if (attributeType.isPrimitive()) {
	    String typeName = attributeType.getName();
	    if (typeName.equals("int") || typeName.equals("byte") || typeName.equals("short"))
		return Integer.class;

	    if (typeName.equals("long"))
		return Long.class;

	    if (typeName.equals("float"))
		return Float.class;

	    if (typeName.equals("double"))
		return Double.class;

	    if (typeName.equals("boolean"))
		return Boolean.class;
	}

	return attributeType;
    }

    @Override
    public Object convertToAttributeType(Object value, Class<?> attributeType) {
	if (value == null)
	    return null;

	Class<?> type = value.getClass();
	if (value instanceof Number) {
	    if (attributeType == Long.class || (attributeType.isPrimitive() && attributeType.getName().equals("long")))
		return ((Number) value).longValue();

	    if (attributeType == Integer.class || (attributeType.isPrimitive() && attributeType.getName().equals("int")))
		return ((Number) value).intValue();

	    if (attributeType == Float.class || (attributeType.isPrimitive() && attributeType.getName().equals("float")))
		return ((Number) value).floatValue();

	    if (attributeType == Double.class || (attributeType.isPrimitive() && attributeType.getName().equals("double")))
		return ((Number) value).doubleValue();
	}

	if (type == String.class && attributeType.isEnum())
	    return Enum.valueOf((Class<Enum>) attributeType, (String) value);

	if (type == Integer.class && attributeType.isEnum()) {
	    Object[] enums = attributeType.getEnumConstants();
	    for (Object o : enums) {
		if (((Enum) o).ordinal() == (Integer) value)
		    return o;
	    }

	    return null;
	}

	if (attributeType == LocalDate.class) {
	    if (type == java.util.Date.class) {
		java.util.Date date = (java.util.Date) value;
		return new java.sql.Date(date.getTime()).toLocalDate();
	    }

	    if (type == java.sql.Date.class) {
		java.sql.Date date = (java.sql.Date) value;
		return date.toLocalDate();
	    }
	}

	if (attributeType == LocalTime.class) {
	    if (type == java.sql.Time.class) {
		java.sql.Time date = (java.sql.Time) value;
		return date.toLocalTime();
	    }
	}

	if (attributeType == Duration.class) {
	    if (type == Long.class) {
		return Duration.ofMillis((Long) value);
	    }
	}

	if (type == Timestamp.class) {
	    if (attributeType == OffsetDateTime.class) {
		Timestamp date = (Timestamp) value;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return OffsetDateTime.ofInstant(date.toInstant(), calendar.getTimeZone().toZoneId());
	    }

	    if (attributeType == LocalDateTime.class) {
		Timestamp timestamp = (Timestamp) value;
		return timestamp.toLocalDateTime();
	    }

	    if (attributeType == java.sql.Date.class) {
		Timestamp timestamp = (Timestamp) value;
		return new java.sql.Date(timestamp.getTime());
	    }

	    if (attributeType == java.util.Date.class) {
		return value;
	    }

	    if (attributeType == Calendar.class) {
		Timestamp date = (Timestamp) value;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	    }

	    if (attributeType == Instant.class) {
		Timestamp timestamp = (Timestamp) value;
		return timestamp.toInstant();
	    }

	    if (attributeType == ZonedDateTime.class) {
		Timestamp timestamp = (Timestamp) value;
		return ZonedDateTime.of(timestamp.toLocalDateTime(), ZoneOffset.UTC);
	    }
	}

	if (type == Time.class) {
	    if (attributeType == OffsetTime.class) {
		Time time = (Time) value;
		return OffsetTime.of(time.toLocalTime(), OffsetTime.now().getOffset());
	    }
	}

	if (type == java.sql.Date.class) {
	    if (attributeType == java.util.Date.class) {
		return value;
	    }
	}

	return value;
    }

    @Override
    public Object convertGeneratedKey(Object value, Class<?> attributeType) {
	return ((Number) value).longValue();
    }

}
