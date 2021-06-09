package org.minijpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.minijpa.jpa.model.PurchaseStats;

public class PurchaseStatsTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("purchase_stats", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void sum() throws Exception {
	final EntityManager em = emf.createEntityManager();
	final EntityTransaction tx = em.getTransaction();
	tx.begin();

	PurchaseStats ps1 = new PurchaseStats();
	ps1.setStartDate(LocalDate.of(2020, Month.MARCH, 1));
	ps1.setEndDate(LocalDate.of(2020, Month.APRIL, 1));
	ps1.setDebitCard(Double.valueOf("20.0"));
	ps1.setCreditCard(Double.valueOf("40.0"));
	ps1.setCash(Double.valueOf("40.0"));
	em.persist(ps1);

	PurchaseStats ps2 = new PurchaseStats();
	ps2.setStartDate(LocalDate.of(2020, Month.APRIL, 1));
	ps2.setEndDate(LocalDate.of(2020, Month.JUNE, 1));
	ps2.setDebitCard(Double.valueOf("30.0"));
	ps2.setCreditCard(Double.valueOf("20.0"));
	em.persist(ps2);

	CriteriaBuilder cb = em.getCriteriaBuilder();
	CriteriaQuery criteriaQuery = cb.createQuery();
	Root<PurchaseStats> root = criteriaQuery.from(PurchaseStats.class);
	Expression<?> sumExpr = cb.sum(root.get("debitCard"), root.get("creditCard"));
	Class c = sumExpr.getJavaType();
	Assertions.assertEquals(c, Double.class);
	Assertions.assertNotNull(sumExpr);
	criteriaQuery.select(sumExpr);

	Query query = em.createQuery(criteriaQuery);
	List result = query.getResultList();
	Assertions.assertNotNull(result);
	Assertions.assertEquals(2, result.size());
	Assertions.assertTrue(result.get(0) instanceof Number);
	if (result.get(0) instanceof Double)
	    Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList(60d, 50d), result));
	else if (result.get(0) instanceof Integer)
	    Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList(60, 50), result));

	criteriaQuery.select(root.get("cash"));
	query = em.createQuery(criteriaQuery);
	result = query.getResultList();
	Assertions.assertNotNull(result);
	Assertions.assertEquals(2, result.size());
	List<? extends Number> r = new ArrayList<>(result);
	Comparator<Number> comparator = new Comparator<Number>() {
	    @Override
	    public int compare(Number o1, Number o2) {
		if (o1 == null)
		    return -1;

		if (o2 == null)
		    return 1;

		return ((Double) o1.doubleValue()).compareTo((Double) o2.doubleValue());
	    }
	};

	r.sort(comparator);
	Assertions.assertNull(r.get(0));
	Assertions.assertEquals(40d, ((Number) r.get(1)).doubleValue());

	// sum of column and double value
	sumExpr = cb.sum(root.get("debitCard"), 10.456d);
	c = sumExpr.getJavaType();
	Assertions.assertEquals(c, Double.class);
	Assertions.assertNotNull(sumExpr);
	criteriaQuery.select(sumExpr);
	query = em.createQuery(criteriaQuery);
	result = query.getResultList();
	Assertions.assertNotNull(result);
	r = new ArrayList<>(result);
	r.sort(comparator);
	Assertions.assertEquals(30.456d, ((Number) r.get(0)).doubleValue());
	Assertions.assertEquals(40.456d, ((Number) r.get(1)).doubleValue());
//	Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList(30.456d, 40.456d), result));

	em.remove(ps1);
	em.remove(ps2);
	tx.commit();

	em.close();
    }

}
