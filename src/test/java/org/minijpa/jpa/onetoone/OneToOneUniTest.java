package org.minijpa.jpa.onetoone;

import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.City;
import org.minijpa.jpa.model.Region;

/**
 * @author adamato
 *
 */
public class OneToOneUniTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("cities_uni", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void persist() throws Exception {
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			City city = new City();
			city.setName("York");
			city.setPopulation(210618);

			Region region = createYorkshire();
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

			tx.begin();
			em.remove(c);
			tx.commit();

		} finally {
			em.close();
		}
	}

	private Region createYorkshire() {
		Region region = new Region();
		region.setName("Yorkshire and the Humber");
		region.setPopulation(5284000);
		return region;
	}

	private Region createNorthWest() {
		Region region = new Region();
		region.setName("North West");
		region.setPopulation(7341196);
		return region;
	}

	private Region createSouthWest() {
		Region region = new Region();
		region.setName("South West");
		region.setPopulation(5624696);
		return region;
	}

	private Region createSouthEast() {
		Region region = new Region();
		region.setName("South East");
		region.setPopulation(9180135);
		return region;
	}

	@Test
	public void sum() throws Exception {
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			City yorkCity = new City();
			yorkCity.setName("York");
			yorkCity.setPopulation(210618);

			Region region = createYorkshire();
			em.persist(region);

			yorkCity.setRegion(region);

			em.persist(yorkCity);

			City manchesterCity = new City();
			manchesterCity.setName("Manchester");
			manchesterCity.setPopulation(552858);

			region = createNorthWest();
			em.persist(region);

			manchesterCity.setRegion(region);

			em.persist(manchesterCity);

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteriaQuery = cb.createQuery();
			Root<City> root = criteriaQuery.from(City.class);
			criteriaQuery.select(cb.sum(root.get("population")));
			Query query = em.createQuery(criteriaQuery);
			Object totalPopulation = query.getSingleResult();
			Assertions.assertNotNull(totalPopulation);
			if (totalPopulation instanceof Integer)
				Assertions.assertEquals(763476, totalPopulation);
			else if (totalPopulation instanceof Long)
				Assertions.assertEquals(763476L, totalPopulation);

			em.remove(yorkCity);
			em.remove(manchesterCity);
			tx.commit();
		} finally {
			em.close();
		}
	}

	@Test
	public void avg() throws Exception {
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			City yorkCity = new City();
			yorkCity.setName("York");
			yorkCity.setPopulation(210618);

			Region region = createYorkshire();
			em.persist(region);

			yorkCity.setRegion(region);

			em.persist(yorkCity);

			City manchesterCity = new City();
			manchesterCity.setName("Manchester");
			manchesterCity.setPopulation(552858);

			region = createNorthWest();
			em.persist(region);

			manchesterCity.setRegion(region);

			em.persist(manchesterCity);

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery criteriaQuery = cb.createQuery();
			Root<City> root = criteriaQuery.from(City.class);
			criteriaQuery.select(cb.avg(root.get("population")));
			Query query = em.createQuery(criteriaQuery);
			Object avgPopulation = query.getSingleResult();
			Assertions.assertNotNull(avgPopulation);
			if (avgPopulation instanceof Double)
				Assertions.assertEquals(381738.0, avgPopulation);
			else if (avgPopulation instanceof Float)
				Assertions.assertEquals(381738.0, avgPopulation);
			else if (avgPopulation instanceof BigDecimal)
				Assertions.assertEquals(381738.0, ((BigDecimal) avgPopulation).doubleValue());

			em.remove(yorkCity);
			em.remove(manchesterCity);
			tx.commit();
		} finally {
			em.close();
		}
	}

	@Test
	public void jpqlIn() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		Region yorkShireRegion = createYorkshire();
		em.persist(yorkShireRegion);

		Region northWestRegion = createNorthWest();
		em.persist(northWestRegion);

		Region southWestRegion = createSouthWest();
		em.persist(southWestRegion);

		Region southEastRegion = createSouthEast();
		em.persist(southEastRegion);

		Query query = em.createQuery("select r.population from Region r"
				+ " where r.name is not null and r.name in ('North West','South West') order by r.name");
		List list = query.getResultList();
		Assertions.assertEquals(2, list.size());
		Assertions.assertEquals(7341196, list.get(0));
		Assertions.assertEquals(5624696, list.get(1));

		em.remove(yorkShireRegion);
		em.remove(northWestRegion);
		em.remove(southEastRegion);
		em.remove(southWestRegion);
		tx.commit();

		em.close();
	}

}
