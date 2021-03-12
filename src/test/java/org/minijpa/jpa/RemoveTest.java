package org.minijpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Address;
import org.minijpa.jpa.model.Citizen;

public class RemoveTest {

    @Test
    public void remove() throws Exception {
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("citizens");
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Citizen citizen = new Citizen();
	    citizen.setName("Marc");
	    em.persist(citizen);

	    Address address = new Address();
	    address.setName("Regent St");
	    em.persist(address);

	    tx.commit();

	    tx.begin();
	    em.remove(citizen);
	    em.remove(address);
	    tx.commit();

	    Citizen c = em.find(Citizen.class, citizen.getId());
	    Assertions.assertNull(c);

	    c = em.find(Citizen.class, address.getId());
	    Assertions.assertNull(c);
	} finally {
	    em.close();
	    emf.close();
	}
    }

}
