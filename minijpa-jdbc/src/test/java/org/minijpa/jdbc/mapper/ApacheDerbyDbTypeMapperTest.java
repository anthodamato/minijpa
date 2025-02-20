package org.minijpa.jdbc.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

public class ApacheDerbyDbTypeMapperTest {
    private ApacheDerbyDbTypeMapper apacheDerbyDbTypeMapper = new ApacheDerbyDbTypeMapper();

    @Test
    public void utilDateMapper() {
        ObjectConverter<Date, java.sql.Date> objectConverter = apacheDerbyDbTypeMapper
                .attributeMapper(java.util.Date.class, java.sql.Date.class);
        assertNotNull(objectConverter);
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = objectConverter.convertTo(utilDate);
        assertNotNull(sqlDate);
        assertEquals(utilDate, sqlDate);

        java.util.Date utilDateConv = objectConverter.convertFrom(sqlDate);
        assertNotNull(utilDateConv);
        assertEquals(utilDate, utilDateConv);
    }

    @Test
    public void localDateMapper() {
        ObjectConverter<LocalDate, java.sql.Date> objectConverter = apacheDerbyDbTypeMapper
                .attributeMapper(LocalDate.class, Object.class);
        assertNotNull(objectConverter);
        LocalDate localDate = LocalDate.of(2022, 10, 1);
        java.sql.Date date = objectConverter.convertTo(localDate);
        assertNotNull(date);
        assertEquals(localDate, date.toLocalDate());

        LocalDate ld = objectConverter.convertFrom(date);
        assertNotNull(ld);
        assertEquals(localDate, ld);
    }

    @Test
    public void localDateTimeMapper() {
        ObjectConverter<LocalDateTime, Timestamp> objectConverter = apacheDerbyDbTypeMapper
                .attributeMapper(LocalDateTime.class, Object.class);
        assertNotNull(objectConverter);
        LocalDateTime localDateTime = LocalDateTime.of(2022, 10, 1, 12, 35, 44);
        java.sql.Timestamp timestamp = objectConverter.convertTo(localDateTime);
        assertNotNull(timestamp);
        assertEquals(localDateTime, timestamp.toLocalDateTime());

        LocalDateTime ldt = objectConverter.convertFrom(timestamp);
        assertNotNull(ldt);
        assertEquals(localDateTime, ldt);
    }

    @Test
    public void localTimeMapper() {
        ObjectConverter<LocalTime, Time> objectConverter = apacheDerbyDbTypeMapper
                .attributeMapper(LocalTime.class, Object.class);
        assertNotNull(objectConverter);
        LocalTime localTime = LocalTime.of(12, 35, 44);
        java.sql.Time time = objectConverter.convertTo(localTime);
        assertNotNull(time);
        assertEquals(localTime, time.toLocalTime());

        LocalTime lt = objectConverter.convertFrom(time);
        assertNotNull(lt);
        assertEquals(localTime, lt);
    }

    @Test
    public void calendarToSqlDateMapper() {
        ObjectConverter<Calendar, java.sql.Date> objectConverter = apacheDerbyDbTypeMapper
                .attributeMapper(java.util.Calendar.class, java.sql.Date.class);
        assertNotNull(objectConverter);
        java.util.Calendar calendar = Calendar.getInstance();
        LocalDate localDate = LocalDate.of(2022, 10, 10);
        java.util.Date utilDate = java.util.Date
                .from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        calendar.setTime(utilDate);

        java.sql.Date sqlDate = objectConverter.convertTo(calendar);
        assertNotNull(sqlDate);
        assertEquals(utilDate, sqlDate);

        java.util.Calendar calendarConv = objectConverter.convertFrom(sqlDate);
        assertNotNull(calendarConv);
        assertEquals(calendar, calendarConv);
    }

    @Test
    public void calendarToTimestampMapper() {
        ObjectConverter<Calendar, Timestamp> objectConverter = apacheDerbyDbTypeMapper
                .attributeMapper(java.util.Calendar.class, java.sql.Timestamp.class);
        assertNotNull(objectConverter);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        LocalDateTime localDateTime = LocalDateTime.of(2022, 10, 10, 20, 32, 10);

        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        java.util.Date utilDate = java.util.Date.from(zdt.toInstant());

        calendar.setTime(utilDate);

        java.sql.Timestamp timestamp = objectConverter.convertTo(calendar);
        assertNotNull(timestamp);
        assertEquals(Timestamp.valueOf(localDateTime), timestamp);

        java.util.Calendar calendarConv = objectConverter.convertFrom(timestamp);
        assertNotNull(calendarConv);
        assertEquals(calendar, calendarConv);
    }

    @Test
    public void databaseType() {
        Class<?> cz = apacheDerbyDbTypeMapper.databaseType(int.class, Optional.empty());
        assertEquals(Integer.class, cz);

        cz = apacheDerbyDbTypeMapper.databaseType(long.class, Optional.empty());
        assertEquals(Long.class, cz);
    }

    @Test
    public void stringEnumMapper() {
        ObjectConverter<Enum, String> objectConverter = apacheDerbyDbTypeMapper.attributeMapper(StringEnum.class,
                String.class);
        assertNotNull(objectConverter);
        StringEnum en = StringEnum.V1;
        String sv1 = objectConverter.convertTo(en);
        assertNotNull(sv1);
        assertEquals("V1", sv1);

        StringEnum se1 = (StringEnum) objectConverter.convertFrom("V1");
        assertNotNull(se1);
        assertEquals(StringEnum.V1, se1);
    }

    @Test
    public void ordinalEnumMapper() {
        ObjectConverter<Enum, Integer> objectConverter = apacheDerbyDbTypeMapper.attributeMapper(OrdinalEnum.class,
                Integer.class);
        assertNotNull(objectConverter);
        OrdinalEnum oe = OrdinalEnum.N2;
        Integer iv1 = objectConverter.convertTo(oe);
        assertNotNull(iv1);
        assertEquals(Integer.valueOf(1), iv1);

        OrdinalEnum oe1 = (OrdinalEnum) objectConverter.convertFrom(Integer.valueOf(1));
        assertNotNull(oe1);
        assertEquals(OrdinalEnum.N2, oe1);
    }

    private enum StringEnum {
        V1, V2;
    }

    private enum OrdinalEnum {
        N1, N2;
    }
}
