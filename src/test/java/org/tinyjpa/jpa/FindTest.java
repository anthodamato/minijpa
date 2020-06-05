package org.tinyjpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.model.Citizen;

/**
 * java -jar $DERBY_HOME/lib/derbyrun.jar server start
 * 
 * connect 'jdbc:derby://localhost:1527/test';
 * 
 * @author adamato
 *
 */
public class FindTest {

	@Test
	public void find() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("citizens");

//		Persistence.generateSchema("citizens", null);

		final EntityManager em = emf.createEntityManager();
		try {
			EntityTransaction tx = em.getTransaction();
			tx.begin();
			Citizen citizen = new Citizen();
			citizen.setName("Anthony");
			em.persist(citizen);

			Assertions.assertNotNull(citizen.getId());
			tx.commit();

			Citizen c = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(c);
			Assertions.assertEquals("Anthony", c.getName());

		} finally {
			em.close();
			emf.close();
		}
	}

}
