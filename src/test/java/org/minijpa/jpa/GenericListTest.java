package org.minijpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.GenericList;

public class GenericListTest {
	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("generic_list");
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void persistNoAttributes() {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		GenericList genericList = new GenericList();
		em.persist(genericList);
		tx.commit();

		Assertions.assertNotNull(genericList.getId());
		Assertions.assertNull(genericList.getName1());
		Assertions.assertNull(genericList.getName2());
		
		GenericList gl = em.find(GenericList.class, genericList.getId());
		Assertions.assertNotNull(gl);
		Assertions.assertEquals(genericList.getId(), gl.getId());
		Assertions.assertNull(gl.getName1());
		Assertions.assertNull(gl.getName2());

		em.close();
	}

}
