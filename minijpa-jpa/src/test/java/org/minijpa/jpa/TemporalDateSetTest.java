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
package org.minijpa.jpa;

import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.TemporalDateSet;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class TemporalDateSetTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() throws Exception {
		emf = Persistence.createEntityManagerFactory("temporal_dates", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void temporalDates() throws Exception {
		TemporalDateSet dateSet = new TemporalDateSet();
		java.util.Date utilDate = new java.util.Date();
		dateSet.setDateToDate(utilDate);
		dateSet.setDateToTime(utilDate);
		dateSet.setDateToTimestamp(utilDate);

		Calendar calendar = new GregorianCalendar(2021, 2, 14, 10, 20, 30);
		dateSet.setCalendarToDate(calendar);
//	dateSet.setCalendarToTime(calendar);
		dateSet.setCalendarToTimestamp(calendar);

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		em.persist(dateSet);
		em.flush();

		em.detach(dateSet);

		TemporalDateSet ds = em.find(TemporalDateSet.class, dateSet.getId());

		Assertions.assertEquals(utilDate.getYear(), ds.getDateToDate().getYear());
		Assertions.assertEquals(utilDate.getMonth(), ds.getDateToDate().getMonth());
		Assertions.assertEquals(utilDate.getDay(), ds.getDateToDate().getDay());

		// MariaDB truncates the last three digits
		Assertions.assertEquals(utilDate.getTime() / 1000, ds.getDateToTimestamp().getTime() / 1000);

		Assertions.assertEquals(calendar.get(Calendar.YEAR), ds.getCalendarToDate().get(Calendar.YEAR));
		Assertions.assertEquals(calendar.get(Calendar.MONTH), ds.getCalendarToDate().get(Calendar.MONTH));
		Assertions.assertEquals(calendar.get(Calendar.DAY_OF_WEEK), ds.getCalendarToDate().get(Calendar.DAY_OF_WEEK));

//	Assertions.assertEquals(calendar, ds.getCalendarToTime());
		Assertions.assertEquals(calendar.get(Calendar.YEAR), ds.getCalendarToTimestamp().get(Calendar.YEAR));
		Assertions.assertEquals(calendar.get(Calendar.MONTH), ds.getCalendarToTimestamp().get(Calendar.MONTH));
		Assertions.assertEquals(calendar.get(Calendar.DAY_OF_WEEK),
				ds.getCalendarToTimestamp().get(Calendar.DAY_OF_WEEK));
		Assertions.assertEquals(calendar.get(Calendar.HOUR), ds.getCalendarToTimestamp().get(Calendar.HOUR));
		Assertions.assertEquals(calendar.get(Calendar.MINUTE), ds.getCalendarToTimestamp().get(Calendar.MINUTE));
		Assertions.assertEquals(calendar.get(Calendar.SECOND), ds.getCalendarToTimestamp().get(Calendar.SECOND));
		Assertions.assertEquals(calendar.get(Calendar.MILLISECOND),
				ds.getCalendarToTimestamp().get(Calendar.MILLISECOND));
		em.remove(ds);

		em.getTransaction().commit();
		em.close();

	}
}
