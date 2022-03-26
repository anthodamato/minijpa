/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Citizen;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class NativeQueryTest {

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
    public void nativeQuery() throws Exception {
	final EntityManager em = emf.createEntityManager();
	EntityTransaction tx = em.getTransaction();
	tx.begin();
	Citizen citizenPeter = new Citizen();
	citizenPeter.setName("Peter");
	citizenPeter.setLastName("Scott");
	em.persist(citizenPeter);

	Citizen citizenEmily = new Citizen();
	citizenEmily.setName("Emily");
	em.persist(citizenEmily);
	tx.commit();

	tx.begin();
	Query query = em.createNativeQuery("select c.* from citizen c", Citizen.class);
	List<Citizen> citizens = query.getResultList();
	Assertions.assertEquals(2, citizens.size());
	Assertions.assertTrue(citizens.get(0).getName().equals("Peter")
		|| citizens.get(1).getName().equals("Peter"));
	Assertions.assertTrue(citizens.get(0).getName().equals("Emily")
		|| citizens.get(1).getName().equals("Emily"));

	// after detach
	em.detach(citizenPeter);
	em.detach(citizenEmily);
	citizens = query.getResultList();
	Assertions.assertEquals(2, citizens.size());
	Assertions.assertTrue(citizens.get(0).getName().equals("Peter")
		|| citizens.get(1).getName().equals("Peter"));
	Assertions.assertTrue(citizens.get(0).getName().equals("Emily")
		|| citizens.get(1).getName().equals("Emily"));

	em.remove(citizens.get(0));
	em.remove(citizens.get(1));
	tx.commit();

	em.close();
    }
}
