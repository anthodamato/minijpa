package org.tinyjpa.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.model.Citizen;

/**
 * 
 * @author adamato
 *
 */
public class FindTest {
	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("citizens");
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void find() throws Exception {
//		Persistence.generateSchema("citizens", null);

		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		Citizen citizen = new Citizen();
		citizen.setName("Anthony");
		em.persist(citizen);

		Assertions.assertNotNull(citizen.getId());

		Citizen c = em.find(Citizen.class, citizen.getId());
		Assertions.assertNotNull(c);
		Assertions.assertEquals("Anthony", c.getName());

		em.remove(c);
		tx.commit();
		em.close();
	}

	@Test
	public void criteria() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		Citizen citizen = new Citizen();
		citizen.setName("Anthony");
		citizen.setLastName("Smith");
		em.persist(citizen);
		Citizen c_Smith = em.find(Citizen.class, citizen.getId());

		citizen = new Citizen();
		citizen.setName("Bill");
		citizen.setLastName("Crown");
		em.persist(citizen);
		Citizen c_Crown = em.find(Citizen.class, citizen.getId());

		Assertions.assertNotNull(citizen.getId());
		tx.commit();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
		Root<Citizen> root = cq.from(Citizen.class);
		CriteriaQuery<Citizen> cqCitizen = cq.select(root);

		TypedQuery<Citizen> typedQuery = em.createQuery(cqCitizen);
		List<Citizen> citizens = typedQuery.getResultList();

		Assertions.assertEquals(2, citizens.size());

		// check the references
		int counter = 0;
		for (Citizen ct : citizens) {
			if (ct.getId() == c_Crown.getId()) {
				++counter;
				Assertions.assertTrue(ct == c_Crown);
			}

			if (ct.getId() == c_Smith.getId()) {
				++counter;
				Assertions.assertTrue(ct == c_Smith);
			}
		}

		Assertions.assertEquals(2, counter);

		em.close();
	}

}
