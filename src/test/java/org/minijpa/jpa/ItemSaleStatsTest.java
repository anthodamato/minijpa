/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.minijpa.jpa;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.ItemSaleStats;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ItemSaleStatsTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("item_sale_stats", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void groupBy() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		ItemSaleStats i1 = create1();
		em.persist(i1);

		ItemSaleStats i2 = create2();
		em.persist(i2);

		ItemSaleStats i3 = create3();
		em.persist(i3);

		ItemSaleStats i4 = create4();
		em.persist(i4);

		ItemSaleStats i5 = create5();
		em.persist(i5);

		ItemSaleStats i6 = create6();
		em.persist(i6);

		ItemSaleStats i7 = create7();
		em.persist(i7);

		Query query = em.createQuery("select i.category, count(i.count) from ItemSaleStats i group by i.category order by i.category");
		List list = query.getResultList();
		Assertions.assertEquals(3, list.size());
		Object[] r0 = (Object[]) list.get(0);
		Assertions.assertEquals("pen", r0[0]);
		Assertions.assertEquals(3L, ((Number) r0[1]).longValue());
		Object[] r1 = (Object[]) list.get(1);
		Assertions.assertEquals("pencil", r1[0]);
		Assertions.assertEquals(2L, ((Number) r1[1]).longValue());
		Object[] r2 = (Object[]) list.get(2);
		Assertions.assertEquals("rubber", r2[0]);
		Assertions.assertEquals(2L, ((Number) r2[1]).longValue());

		em.remove(i1);
		em.remove(i2);
		em.remove(i3);
		em.remove(i4);
		em.remove(i5);
		em.remove(i6);
		em.remove(i7);
		em.getTransaction().commit();
		em.close();
	}

	private ItemSaleStats create1() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 10));
		itemSaleStats.setCategory("pencil");
		itemSaleStats.setModel("Rocket");
		itemSaleStats.setCount(14);
		itemSaleStats.setPrice(2.0f);
		return itemSaleStats;
	}

	private ItemSaleStats create2() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 11));
		itemSaleStats.setCategory("pencil");
		itemSaleStats.setModel("Rocket");
		itemSaleStats.setCount(9);
		itemSaleStats.setPrice(2.1f);
		return itemSaleStats;
	}

	private ItemSaleStats create3() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 10));
		itemSaleStats.setCategory("pen");
		itemSaleStats.setModel("Flag");
		itemSaleStats.setCount(24);
		itemSaleStats.setPrice(3.0f);
		return itemSaleStats;
	}

	private ItemSaleStats create4() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 11));
		itemSaleStats.setCategory("pen");
		itemSaleStats.setModel("Unbreakable");
		itemSaleStats.setCount(44);
		itemSaleStats.setPrice(3.6f);
		return itemSaleStats;
	}

	private ItemSaleStats create5() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 12));
		itemSaleStats.setCategory("pen");
		itemSaleStats.setModel("Unbreakable");
		itemSaleStats.setCount(64);
		itemSaleStats.setPrice(4.6f);
		return itemSaleStats;
	}

	private ItemSaleStats create6() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 11));
		itemSaleStats.setCategory("rubber");
		itemSaleStats.setModel("Soft");
		itemSaleStats.setCount(29);
		itemSaleStats.setPrice(1.6f);
		return itemSaleStats;
	}

	private ItemSaleStats create7() {
		ItemSaleStats itemSaleStats = new ItemSaleStats();
		itemSaleStats.setDate(LocalDate.of(2020, Month.MARCH, 12));
		itemSaleStats.setCategory("rubber");
		itemSaleStats.setModel("Soft");
		itemSaleStats.setCount(2);
		itemSaleStats.setPrice(1.8f);
		return itemSaleStats;
	}

}
