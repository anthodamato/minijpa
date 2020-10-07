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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jpa.model.Citizen;

/**
 * 
 * @author adamato
 *
 */
public class FindTest {
	private Logger LOG = LoggerFactory.getLogger(FindTest.class);
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
		Citizen c = em.find(Citizen.class, citizen.getId());
		LOG.info("criteria: c=" + c + "; c.getName()=" + c.getName());

		citizen = new Citizen();
		citizen.setName("Bill");
		citizen.setLastName("Crown");
		em.persist(citizen);
		c = em.find(Citizen.class, citizen.getId());
		LOG.info("criteria: c=" + c + "; c.getName()=" + c.getName());

		Assertions.assertNotNull(citizen.getId());
		tx.commit();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
		Root<Citizen> root = cq.from(Citizen.class);
		CriteriaQuery<Citizen> cqCitizen = cq.select(root);

		TypedQuery<Citizen> typedQuery = em.createQuery(cqCitizen);
		List<Citizen> citizens = typedQuery.getResultList();

		Assertions.assertEquals(2, citizens.size());

		for (Citizen ct : citizens) {
			LOG.info("criteria: ct=" + ct + "; ct.getName()=" + ct.getName());
		}

		em.close();
	}

}
