package org.minijpa.jpa;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.hamcrest.MatcherAssert;
import org.hamcrest.number.IsCloseTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.NumericSet;

public class NumericSetTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("numeric_set", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void persist() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		NumericSet numericSet1 = new NumericSet();
		numericSet1.setDoubleValue(10.1);
		numericSet1.setBdValue(new BigDecimal(10.2));
		numericSet1.setIntValue(1000000);
		em.persist(numericSet1);

		NumericSet numericSet2 = new NumericSet();
		numericSet2.setDoubleValue(10.3);
		numericSet2.setBdValue(new BigDecimal(10.5));
		numericSet2.setIntValue(1000000);
		em.persist(numericSet2);

		Query query = em.createQuery("select sum(ns.doubleValue) from NumericSet ns");
		Object result = query.getSingleResult();
		Assertions.assertNotNull(result);
		if (result instanceof Double) {
			Assertions.assertEquals(Double.class, result.getClass());
			Assertions.assertEquals(20.4d, result);
		} else if (result instanceof BigDecimal) {
			// oracle
			Assertions.assertEquals(BigDecimal.class, result.getClass());
			Assertions.assertEquals(20.4d, ((BigDecimal) result).doubleValue());
		}

		query = em.createQuery("select sum(ns.intValue) from NumericSet ns");
		result = query.getSingleResult();
		if (result instanceof Integer) {
			Assertions.assertEquals(2000000, result);
		} else if (result instanceof Long) {
			Assertions.assertEquals(2000000L, result);
		}

		tx.rollback();
		em.close();
	}

	@Test
	public void jpqlSum() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		NumericSet numericSet1 = new NumericSet();
		numericSet1.setDoubleValue(10.1d);
		numericSet1.setBdValue(new BigDecimal(10.2));
		numericSet1.setIntValue(1000000);
		em.persist(numericSet1);

		Query query = em.createQuery("select sum(ns.doubleValue)+0.2 from NumericSet ns");
		Object result = query.getSingleResult();
		Assertions.assertNotNull(result);
		if (result instanceof Double) {
			Assertions.assertEquals(Double.class, result.getClass());
			MatcherAssert.assertThat((double) result, IsCloseTo.closeTo(10.3d, 0.1));
		} else if (result instanceof BigDecimal) {
			// oracle
			Assertions.assertEquals(BigDecimal.class, result.getClass());
			Assertions.assertEquals(10.3d, ((BigDecimal) result).doubleValue());
		}

		tx.rollback();
		em.close();
	}

}
