package org.tinyjpa.jpa.onetoone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.PersistenceProviderImpl;
import org.tinyjpa.jpa.model.onetoone.City;
import org.tinyjpa.jpa.model.onetoone.Region;

/**
 * @author adamato
 *
 */
public class OneToOneUniTest {

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = new PersistenceProviderImpl()
				.createEntityManagerFactory("/org/tinyjpa/jpa/onetoone/persistence.xml", "cities_uni", null);
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			City city = new City();
			city.setName("York");

			Region region = new Region();
			region.setName("Yorkshire and the Humber");
			region.setPopulation(5284000);
			em.persist(region);

			city.setRegion(region);

			em.persist(city);

			tx.commit();

			em.detach(city);

			City c = em.find(City.class, city.getId());
			Assertions.assertNotNull(c);
			Assertions.assertFalse(c == city);
			Assertions.assertEquals(city.getId(), c.getId());
			Assertions.assertNotNull(c.getRegion());
			Assertions.assertEquals("York", c.getName());
			Assertions.assertEquals("Yorkshire and the Humber", c.getRegion().getName());

		} finally {
			em.close();
			emf.close();
		}
	}

}
