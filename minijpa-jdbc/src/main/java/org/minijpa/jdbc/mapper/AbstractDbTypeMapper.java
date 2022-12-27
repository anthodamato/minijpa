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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Optional;

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
    private final AttributeMapper utilDateToSqlDateAttributeMapper = new UtilDateToSqlDateAttributeMapper();
    private final AttributeMapper utilDateToSqlTimeAttributeMapper = new UtilDateToSqlTimeAttributeMapper();
    private final AttributeMapper calendarToSqlDateAttributeMapper = new CalendarToSqlDateAttributeMapper();
    private final AttributeMapper zonedDateTimeAttributeMapper = new ZonedDateTimeAttributeMapper();
    private final AttributeMapper calendarAttributeMapper = new CalendarAttributeMapper();
    protected final AttributeMapper bigDecimalToDoubleAttributeMapper = new BigDecimalToDoubleAttributeMapper();
    protected final AttributeMapper integerToDoubleAttributeMapper = new IntegerToDoubleAttributeMapper();
    public static final AttributeMapper numberToLongAttributeMapper = new NumberToLongAttributeMapper();
    public static final AttributeMapper numberToBigIntegerAttributeMapper = new NumberToBigIntegerAttributeMapper();
    public static final AttributeMapper numberToDoubleAttributeMapper = new NumberToDoubleAttributeMapper();
    public static final AttributeMapper numberToFloatAttributeMapper = new NumberToFloatAttributeMapper();

    @Override
    public AttributeMapper attributeMapper(Class<?> attributeType, Class<?> databaseType) {
        if (attributeType.isEnum() && databaseType == String.class)
            return new StringEnumAttributeMapper(attributeType);

        if (attributeType.isEnum() && databaseType == Integer.class)
            return new OrdinalEnumAttributeMapper(attributeType);

        if (attributeType == LocalDate.class)
            return localDateAttributeMapper;

        if (attributeType == LocalDateTime.class)
            return localDateTimeAttributeMapper;

        if (attributeType == LocalTime.class)
            return localTimeAttributeMapper;

        if (attributeType == java.util.Date.class && databaseType == java.sql.Date.class)
            return utilDateToSqlDateAttributeMapper;

        if (attributeType == java.util.Date.class && databaseType == java.sql.Time.class)
            return utilDateToSqlTimeAttributeMapper;

        if (attributeType == Calendar.class && databaseType == java.sql.Date.class)
            return calendarToSqlDateAttributeMapper;

        if (attributeType == java.util.Date.class)
            return utilDateAttributeMapper;

        if (attributeType == ZonedDateTime.class)
            return zonedDateTimeAttributeMapper;

        if (attributeType == Calendar.class)
            return calendarAttributeMapper;

        if (attributeType == Duration.class)
            return durationAttributeMapper;

        if (attributeType == Instant.class)
            return instantAttributeMapper;

        if (attributeType == OffsetTime.class)
            return offsetTimeAttributeMapper;

        if (attributeType == OffsetDateTime.class)
            return offsetDateTimeAttributeMapper;

        return null;
    }

    @Override
    public Class<?> databaseType(Class<?> attributeType, Optional<Class<?>> enumerationType) {
        if (attributeType == LocalDate.class)
            return java.sql.Date.class;

        if (attributeType == OffsetDateTime.class || attributeType == java.util.Date.class
                || attributeType == Calendar.class || attributeType == LocalDateTime.class
                || attributeType == Instant.class || attributeType == ZonedDateTime.class)
            return Timestamp.class;

        if (attributeType.isEnum() && enumerationType.get() == String.class)
            return String.class;

        if (attributeType.isEnum() && enumerationType.get() == Integer.class)
            return Integer.class;

        String typeName = attributeType.getName();
        if (attributeType.isPrimitive()) {
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

        if (attributeType == Duration.class)
            return Long.class;

        if (attributeType == OffsetTime.class || attributeType == LocalTime.class)
            return Time.class;

        return attributeType;
    }

}
