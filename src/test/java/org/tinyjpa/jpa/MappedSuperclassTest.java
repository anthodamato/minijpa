package org.tinyjpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.model.mappedsuperclass.Square;
import org.tinyjpa.jpa.model.mappedsuperclass.Triangle;

public class MappedSuperclassTest {
	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("mapped_superclass");
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		Triangle triangle = new Triangle();
		triangle.setArea(35);
		em.persist(triangle);

		Assertions.assertNotNull(triangle.getId());

		Square square = new Square();
		square.setArea(40);
		em.persist(square);

		tx.commit();

		em.detach(triangle);
		Triangle t = em.find(Triangle.class, triangle.getId());
		Assertions.assertNotNull(t);
		Assertions.assertEquals(3, triangle.getSides());
		Assertions.assertEquals(35, triangle.getArea());

		em.close();
		emf.close();
	}

}
