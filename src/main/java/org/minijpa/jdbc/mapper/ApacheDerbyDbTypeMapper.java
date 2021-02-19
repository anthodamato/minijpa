package org.minijpa.jdbc.mapper;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;

public class ApacheDerbyDbTypeMapper extends DefaultDbTypeMapper {

    @Override
    public Class<?> map(Class<?> attributeType, Integer jdbcType) {
	if (attributeType == LocalDate.class)
	    return Date.class;

	if (attributeType == OffsetDateTime.class)
	    return Timestamp.class;

	if (attributeType.isEnum() && jdbcType == Types.VARCHAR)
	    return String.class;

	if (attributeType.isEnum() && jdbcType == Types.INTEGER)
	    return Integer.class;

	return attributeType;
    }

    @Override
    public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType) {
	if (attributeType == LocalDate.class && readWriteDbType == Date.class && value != null) {
	    Date date = (Date) value;
	    return new java.sql.Date(date.getTime()).toLocalDate();
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
