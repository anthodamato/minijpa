/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

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
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Citizen;

/**
 *
 * @author adamato
 */
public class EntityDelegateTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("citizens", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void modifications() {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Citizen c1 = new Citizen();
	    c1.setName("Arthur");

	    Citizen c2 = new Citizen();
	    c2.setName("Arthur");

	    Assertions.assertTrue(c1.equals(c2));
	    Assertions.assertEquals(c1.hashCode(), c2.hashCode());
	    em.persist(c1);
	    em.persist(c2);

	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
	    Root<Citizen> root = cq.from(Citizen.class);
	    cq.select(root);
	    TypedQuery<Citizen> typedQuery = em.createQuery(cq);
	    List<Citizen> citizens = typedQuery.getResultList();
	    for (Citizen c : citizens) {
		System.out.println("modifications: c=" + c);
		if (c != null)
		    System.out.println("modifications: c.getName()=" + c.getName() + "; c.getLastName()=" + c.getLastName());
	    }

	    Assertions.assertEquals(2, citizens.size());

	    em.remove(c1);
	    em.remove(c2);
	    tx.commit();
	} finally {
	    em.close();
	}
    }
}
