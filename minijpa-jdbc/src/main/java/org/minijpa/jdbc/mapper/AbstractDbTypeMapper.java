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
    private final ObjectConverter offsetTimeObjectConverter = new OffsetTimeObjectConverter();
    private final ObjectConverter offsetDateTimeObjectConverter = new OffsetDateTimeObjectConverter();
    private final ObjectConverter durationObjectConverter = new DurationObjectConverter();
    private final ObjectConverter instantObjectConverter = new InstantObjectConverter();
    private final ObjectConverter localDateObjectConverter = new LocalDateObjectConverter();
    protected final ObjectConverter localDateToTimestampObjectConverter = new LocalDateToTimestampObjectConverter();
    protected final ObjectConverter localTimeToTimestampObjectConverter = new LocalTimeToTimestampObjectConverter();
    protected final ObjectConverter offsetTimeToTimestampObjectConverter = new OffsetTimeToTimestampObjectConverter();
    protected final ObjectConverter durationToBigDecimalObjectConverter = new DurationToBigDecimalObjectConverter();
    protected final ObjectConverter timeToTimestampObjectConverter = new TimeToTimestampObjectConverter();
    private final ObjectConverter localDateTimeObjectConverter = new LocalDateTimeObjectConverter();
    private final ObjectConverter localTimeObjectConverter = new LocalTimeObjectConverter();
    private final ObjectConverter utilDateObjectConverter = new UtilDateObjectConverter();
    private final ObjectConverter utilDateToSqlDateObjectConverter = new UtilDateToSqlDateObjectConverter();
    private final ObjectConverter utilDateToSqlTimeObjectConverter = new UtilDateToSqlTimeObjectConverter();
    private final ObjectConverter calendarToSqlDateObjectConverter = new CalendarToSqlDateObjectConverter();
    private final ObjectConverter zonedDateTimeObjectConverter = new ZonedDateTimeObjectConverter();
    private final ObjectConverter calendarObjectConverter = new CalendarObjectConverter();
    protected final ObjectConverter bigDecimalToDoubleObjectConverter = new BigDecimalToDoubleObjectConverter();
    protected final ObjectConverter integerToDoubleObjectConverter = new IntegerToDoubleObjectConverter();
    public static final ObjectConverter NUMBER_TO_LONG_OBJECT_CONVERTER = new NumberToLongObjectConverter();
    public static final ObjectConverter NUMBER_TO_BIG_INTEGER_OBJECT_CONVERTER = new NumberToBigIntegerObjectConverter();
    public static final ObjectConverter NUMBER_TO_DOUBLE_OBJECT_CONVERTER = new NumberToDoubleObjectConverter();
    public static final ObjectConverter NUMBER_TO_FLOAT_OBJECT_CONVERTER = new NumberToFloatObjectConverter();

    @Override
    public ObjectConverter attributeMapper(Class<?> attributeType, Class<?> databaseType) {
        if (attributeType.isEnum() && databaseType == String.class)
            return new StringEnumObjectConverter(attributeType);

        if (attributeType.isEnum() && databaseType == Integer.class)
            return new OrdinalEnumObjectConverter(attributeType);

        if (attributeType == LocalDate.class)
            return localDateObjectConverter;

        if (attributeType == LocalDateTime.class)
            return localDateTimeObjectConverter;

        if (attributeType == LocalTime.class)
            return localTimeObjectConverter;

        if (attributeType == java.util.Date.class && databaseType == java.sql.Date.class)
            return utilDateToSqlDateObjectConverter;

        if (attributeType == java.util.Date.class && databaseType == java.sql.Time.class)
            return utilDateToSqlTimeObjectConverter;

        if (attributeType == Calendar.class && databaseType == java.sql.Date.class)
            return calendarToSqlDateObjectConverter;

        if (attributeType == java.util.Date.class)
            return utilDateObjectConverter;

        if (attributeType == ZonedDateTime.class)
            return zonedDateTimeObjectConverter;

        if (attributeType == Calendar.class)
            return calendarObjectConverter;

        if (attributeType == Duration.class)
            return durationObjectConverter;

        if (attributeType == Instant.class)
            return instantObjectConverter;

        if (attributeType == OffsetTime.class)
            return offsetTimeObjectConverter;

        if (attributeType == OffsetDateTime.class)
            return offsetDateTimeObjectConverter;

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
