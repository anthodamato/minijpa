/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.DateSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class DateSetTest {

	private Logger LOG = LoggerFactory.getLogger(DateSetTest.class);
	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("date_set", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void dates() throws Exception {
		DateSet dateSet = new DateSet();
		Duration duration = Duration.ofDays(2);
		dateSet.setDuration(duration);
		Instant instant = Instant.now();
		dateSet.setInstant(instant);
		LocalDate localDate = LocalDate.now();
		dateSet.setLocalDate(localDate);
		LocalDateTime localDateTime = LocalDateTime.now();
		dateSet.setLocalDateTime(localDateTime);
		LocalTime localTime = LocalTime.now();
		dateSet.setLocalTime(localTime);
		OffsetDateTime offsetDateTime = OffsetDateTime.now();
		dateSet.setOffsetDateTime(offsetDateTime);
		OffsetTime offsetTime = OffsetTime.now();
		dateSet.setOffsetTime(offsetTime);
		java.sql.Date sqlDate = new java.sql.Date(instant.toEpochMilli());
		dateSet.setSqlDate(sqlDate);
		Time time = Time.valueOf(localTime);
		dateSet.setSqlTime(time);
		Timestamp timestamp = Timestamp.valueOf(localDateTime);
		dateSet.setSqlTimestamp(timestamp);
		Calendar calendar = new GregorianCalendar(2021, 2, 14, 10, 20, 30);
		dateSet.setUtilCalendar(calendar);
		java.util.Date utilDate = new java.util.Date();
		dateSet.setUtilDate(utilDate);
		ZonedDateTime zonedDateTime = ZonedDateTime.now();
		dateSet.setZonedDateTime(zonedDateTime);

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(dateSet);
		em.flush();

		em.detach(dateSet);

		DateSet ds = em.find(DateSet.class, dateSet.getId());

		Assertions.assertEquals(duration, ds.getDuration());

		// MariaDB timestamp doesn't keep nanoseconds precision but up to microseconds.
		// on Apache Derby 'assertEquals(instant, ds.getInstant());' works
		Assertions.assertEquals(instant.getEpochSecond(), ds.getInstant().getEpochSecond());

		Assertions.assertEquals(localDate, ds.getLocalDate());
		// on Apache Derby 'assertEquals(localDateTime, ds.getLocalDateTime());' works
		Assertions.assertEquals(localDateTime.toEpochSecond(ZoneOffset.UTC),
				ds.getLocalDateTime().toEpochSecond(ZoneOffset.UTC));

		Assertions.assertEquals(localTime.getHour(), ds.getLocalTime().getHour());
		Assertions.assertEquals(localTime.getMinute(), ds.getLocalTime().getMinute());
		Assertions.assertEquals(localTime.getSecond(), ds.getLocalTime().getSecond());

		// on Apache Derby 'assertEquals(offsetDateTime, ds.getOffsetDateTime());' works
		Assertions.assertEquals(offsetDateTime.toEpochSecond(), ds.getOffsetDateTime().toEpochSecond());
		Assertions.assertEquals(offsetDateTime.getOffset(), ds.getOffsetDateTime().getOffset());

//	log(offsetTime, "1");
//	log(ds.getOffsetTime(), "2");
		Assertions.assertEquals(offsetTime.getHour(), ds.getOffsetTime().getHour());
		Assertions.assertEquals(offsetTime.getMinute(), ds.getOffsetTime().getMinute());
		Assertions.assertEquals(offsetTime.getSecond(), ds.getOffsetTime().getSecond());
		Assertions.assertEquals(offsetTime.getOffset(), ds.getOffsetTime().getOffset());

		Assertions.assertEquals(sqlDate.toLocalDate(), ds.getSqlDate().toLocalDate());

		Assertions.assertEquals(time.getHours(), ds.getSqlTime().getHours());
		Assertions.assertEquals(time.getMinutes(), ds.getSqlTime().getMinutes());
		Assertions.assertEquals(time.getSeconds(), ds.getSqlTime().getSeconds());
		// on Apache Derby 'assertEquals(timestamp, ds.getSqlTimestamp());' works
		Assertions.assertEquals(timestamp.getTime() / 1000, ds.getSqlTimestamp().getTime() / 1000);
		Assertions.assertEquals(calendar, ds.getUtilCalendar());
		// on Apache Derby 'assertEquals(utilDate, ds.getUtilDate());' works
		Assertions.assertEquals(utilDate.getTime() / 1000, ds.getUtilDate().getTime() / 1000);
		// on Apache Derby 'assertEquals(zonedDateTime, ds.getZonedDateTime());' works
		Assertions.assertEquals(zonedDateTime.toEpochSecond(), ds.getZonedDateTime().toEpochSecond());

		em.remove(ds);

		em.getTransaction().commit();
		em.close();
	}

	private void log(OffsetTime offsetTime, String prefix) {
		LOG.debug("log: " + prefix + " offsetTime.getHour()=" + offsetTime.getHour());
		LOG.debug("log: " + prefix + " offsetTime.getMinute()=" + offsetTime.getMinute());
		LOG.debug("log: " + prefix + " offsetTime.getSecond()=" + offsetTime.getSecond());
		LOG.debug("log: " + prefix + " offsetTime.getNano()=" + offsetTime.getNano());
		LOG.debug("log: " + prefix + " offsetTime.getOffset()=" + offsetTime.getOffset());
		LOG.debug("log: " + prefix + " ZoneId.systemDefault()=" + ZoneId.systemDefault());
		LOG.debug("log: " + prefix + " Calendar.getInstance().getTimeZone()=" + Calendar.getInstance().getTimeZone());
	}
}
