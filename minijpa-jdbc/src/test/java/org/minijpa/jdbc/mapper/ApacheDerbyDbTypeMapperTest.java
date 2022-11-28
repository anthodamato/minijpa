package org.minijpa.jdbc.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ApacheDerbyDbTypeMapperTest {
    private ApacheDerbyDbTypeMapper apacheDerbyDbTypeMapper = new ApacheDerbyDbTypeMapper();

    @Test
    public void utilDateMapper() {
        AttributeMapper<java.util.Date, java.sql.Date> attributeMapper = apacheDerbyDbTypeMapper
                .attributeMapper(java.util.Date.class, java.sql.Date.class);
        assertNotNull(attributeMapper);
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = attributeMapper.attributeToDatabase(utilDate);
        assertNotNull(sqlDate);
        assertEquals(utilDate, sqlDate);

        java.util.Date utilDateConv = attributeMapper.databaseToAttribute(sqlDate);
        assertNotNull(utilDateConv);
        assertEquals(utilDate, utilDateConv);
    }

    @Test
    public void localDateMapper() {
        AttributeMapper<LocalDate, java.sql.Date> attributeMapper = apacheDerbyDbTypeMapper
                .attributeMapper(LocalDate.class, Object.class);
        assertNotNull(attributeMapper);
        LocalDate localDate = LocalDate.of(2022, 10, 1);
        java.sql.Date date = attributeMapper.attributeToDatabase(localDate);
        assertNotNull(date);
        assertEquals(localDate, date.toLocalDate());

        LocalDate ld = attributeMapper.databaseToAttribute(date);
        assertNotNull(ld);
        assertEquals(localDate, ld);
    }

    @Test
    public void localDateTimeMapper() {
        AttributeMapper<LocalDateTime, java.sql.Timestamp> attributeMapper = apacheDerbyDbTypeMapper
                .attributeMapper(LocalDateTime.class, Object.class);
        assertNotNull(attributeMapper);
        LocalDateTime localDateTime = LocalDateTime.of(2022, 10, 1, 12, 35, 44);
        java.sql.Timestamp timestamp = attributeMapper.attributeToDatabase(localDateTime);
        assertNotNull(timestamp);
        assertEquals(localDateTime, timestamp.toLocalDateTime());

        LocalDateTime ldt = attributeMapper.databaseToAttribute(timestamp);
        assertNotNull(ldt);
        assertEquals(localDateTime, ldt);
    }

    @Test
    public void localTimeMapper() {
        AttributeMapper<LocalTime, java.sql.Time> attributeMapper = apacheDerbyDbTypeMapper
                .attributeMapper(LocalTime.class, Object.class);
        assertNotNull(attributeMapper);
        LocalTime localTime = LocalTime.of(12, 35, 44);
        java.sql.Time time = attributeMapper.attributeToDatabase(localTime);
        assertNotNull(time);
        assertEquals(localTime, time.toLocalTime());

        LocalTime lt = attributeMapper.databaseToAttribute(time);
        assertNotNull(lt);
        assertEquals(localTime, lt);
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
        AttributeMapper<Enum, String> attributeMapper = apacheDerbyDbTypeMapper.attributeMapper(StringEnum.class,
                String.class);
        assertNotNull(attributeMapper);
        StringEnum en = StringEnum.V1;
        String sv1 = attributeMapper.attributeToDatabase(en);
        assertNotNull(sv1);
        assertEquals("V1", sv1);

        StringEnum se1 = (StringEnum) attributeMapper.databaseToAttribute("V1");
        assertNotNull(se1);
        assertEquals(StringEnum.V1, se1);
    }

    @Test
    public void ordinalEnumMapper() {
        AttributeMapper<Enum, Integer> attributeMapper = apacheDerbyDbTypeMapper.attributeMapper(OrdinalEnum.class,
                Integer.class);
        assertNotNull(attributeMapper);
        OrdinalEnum oe = OrdinalEnum.N2;
        Integer iv1 = attributeMapper.attributeToDatabase(oe);
        assertNotNull(iv1);
        assertEquals(Integer.valueOf(1), iv1);

        OrdinalEnum oe1 = (OrdinalEnum) attributeMapper.databaseToAttribute(Integer.valueOf(1));
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
