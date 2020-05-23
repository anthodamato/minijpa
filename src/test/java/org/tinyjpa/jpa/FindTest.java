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
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			Citizen citizen = em.find(Citizen.class, 1L);
			Assertions.assertNotNull(citizen);
			Assertions.assertEquals(1L, citizen.getId());
			Assertions.assertEquals("Marc", citizen.getName());

			tx.commit();

		} finally {
			em.close();
			emf.close();
		}
	}

}
