/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.LineItem;
import org.minijpa.jpa.model.SimpleOrder;
import org.minijpa.jpa.model.SimpleProduct;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlOrderTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("simple_order", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void simpleOrder() throws Exception {
		EntityManager em = emf.createEntityManager();

		em.getTransaction().begin();
		persistEntities(em);

		Query query = em.createQuery(
				"SELECT DISTINCT o FROM SimpleOrder AS o JOIN o.lineItems AS l WHERE l.shipped = FALSE");
		List list = query.getResultList();
		Assertions.assertTrue(!list.isEmpty());
		Assertions.assertEquals(1, list.size());
		Object so = list.get(0);
		Assertions.assertTrue(so instanceof SimpleOrder);
		em.getTransaction().rollback();

		em.close();
	}

	private void persistEntities(EntityManager em) {
		SimpleProduct simpleProduct1 = new SimpleProduct();
		simpleProduct1.setProductType("office_supplies");
		em.persist(simpleProduct1);
		SimpleProduct simpleProduct2 = new SimpleProduct();
		simpleProduct2.setProductType("department_supplies");
		em.persist(simpleProduct2);

		LineItem lineItem1 = new LineItem();
		lineItem1.setProduct(simpleProduct1);
		lineItem1.setShipped(Boolean.FALSE);
		em.persist(lineItem1);

		LineItem lineItem2 = new LineItem();
		lineItem2.setProduct(simpleProduct2);
		lineItem2.setShipped(Boolean.FALSE);
		em.persist(lineItem2);

		SimpleOrder simpleOrder = new SimpleOrder();
		simpleOrder.setLineItems(Arrays.asList(lineItem1, lineItem2));
		em.persist(simpleOrder);
	}
}
