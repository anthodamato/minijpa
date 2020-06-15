package org.tinyjpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jpa.model.Address;
import org.tinyjpa.jpa.model.Citizen;

public class PersistTest {
	private Logger LOG = LoggerFactory.getLogger(PersistTest.class);

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("citizens");
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			Citizen citizen = new Citizen();
			citizen.setName("Marc");
			em.persist(citizen);

			Assertions.assertNotNull(citizen.getId());
			Citizen c = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(c);

			Address address = new Address();
			address.setId(2L);
			address.setName("Regent St");
			em.persist(address);

			tx.commit();

			LOG.info("persist: address.getId()=" + address.getId());
			LOG.info("persist: citizen.getId()=" + citizen.getId());
			c = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(c);

			Assertions.assertNotNull(address.getId());
			c = em.find(Citizen.class, address.getId());
			Assertions.assertNull(c);
		} finally {
			em.close();
			emf.close();
		}
	}

}
