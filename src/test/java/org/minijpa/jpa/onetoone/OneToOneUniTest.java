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
		em.remove(c.getRegion());
		tx.commit();

		em.close();
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

	private Region createNorthEast() {
		Region region = new Region();
		region.setName("North East");
		region.setPopulation(2669941);
		return region;
	}

	@Test
	public void sum() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		City yorkCity = new City();
		yorkCity.setName("York");
		yorkCity.setPopulation(210618);

		Region yorkShireRegion = createYorkshire();
		em.persist(yorkShireRegion);

		yorkCity.setRegion(yorkShireRegion);

		em.persist(yorkCity);

		City manchesterCity = new City();
		manchesterCity.setName("Manchester");
		manchesterCity.setPopulation(552858);

		Region northWestRegion = createNorthWest();
		em.persist(northWestRegion);

		manchesterCity.setRegion(northWestRegion);

		em.persist(manchesterCity);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery criteriaQuery = cb.createQuery();
		Root<City> root = criteriaQuery.from(City.class);
		criteriaQuery.select(cb.sum(root.get("population")));
		Query query = em.createQuery(criteriaQuery);
		Long totalPopulation = (Long) query.getSingleResult();
		Assertions.assertNotNull(totalPopulation);
		Assertions.assertEquals(763476, totalPopulation);

		em.remove(yorkCity);
		em.remove(yorkCity.getRegion());
		em.remove(manchesterCity);
		em.remove(manchesterCity.getRegion());
		tx.commit();
		em.close();
	}

	/**
	 * Avg function returns an integer value but it should be double, refactoring
	 * needed on fetching strategy.
	 *
	 * @throws Exception
	 */
	@Test
	public void avg() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		City yorkCity = new City();
		yorkCity.setName("York");
		yorkCity.setPopulation(210618);

		Region yorkShireRegion = createYorkshire();
		em.persist(yorkShireRegion);

		yorkCity.setRegion(yorkShireRegion);

		em.persist(yorkCity);

		City manchesterCity = new City();
		manchesterCity.setName("Manchester");
		manchesterCity.setPopulation(552858);

		Region northWestRegion = createNorthWest();
		em.persist(northWestRegion);

		manchesterCity.setRegion(northWestRegion);

		em.persist(manchesterCity);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery criteriaQuery = cb.createQuery();
		Root<City> root = criteriaQuery.from(City.class);
		criteriaQuery.select(cb.avg(root.get("population")));
		Query query = em.createQuery(criteriaQuery);
		BigDecimal avgPopulation = (BigDecimal) query.getSingleResult();
		Assertions.assertNotNull(avgPopulation);
		Assertions.assertEquals(381738.0, avgPopulation.doubleValue());

		em.remove(yorkCity);
		em.remove(manchesterCity);
		em.remove(yorkShireRegion);
		em.remove(northWestRegion);
		tx.commit();
		em.close();
	}

	@Test
	public void avgNativeQuery() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		City yorkCity = new City();
		yorkCity.setName("York");
		yorkCity.setPopulation(210618);

		Region yorkShireRegion = createYorkshire();
		em.persist(yorkShireRegion);

		yorkCity.setRegion(yorkShireRegion);

		em.persist(yorkCity);

		City manchesterCity = new City();
		manchesterCity.setName("Manchester");
		manchesterCity.setPopulation(552858);

		Region northWestRegion = createNorthWest();
		em.persist(northWestRegion);

		manchesterCity.setRegion(northWestRegion);

		em.persist(manchesterCity);

		Query query = em.createNativeQuery("select AVG(city0.population) from City city0");
		Object avgPopulation = query.getSingleResult();
		Assertions.assertNotNull(avgPopulation);
		if (avgPopulation instanceof BigDecimal) {
			// Oracle
			Assertions.assertEquals(new BigDecimal(381738).longValue(), ((BigDecimal) avgPopulation).longValue());
		} else if (avgPopulation instanceof Double) {
			// H2
			Assertions.assertEquals(381738.0d, avgPopulation);
		} else
			Assertions.assertEquals(381738, avgPopulation);

		em.remove(yorkCity);
		em.remove(manchesterCity);
		em.remove(yorkShireRegion);
		em.remove(northWestRegion);
		tx.commit();
		em.close();
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

	@Test
	public void jpqlConcatFunction() throws Exception {
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

		Query query = em
				.createQuery("select CONCAT('Region',' ',r.name), r.population from Region r" + " order by r.name");
		List list = query.getResultList();
		Assertions.assertEquals(4, list.size());

		Object[] objects = (Object[]) list.get(0);
		Assertions.assertEquals("Region North West", objects[0]);
		Assertions.assertEquals(7341196, objects[1]);

		objects = (Object[]) list.get(1);
		Assertions.assertEquals("Region South East", objects[0]);
		Assertions.assertEquals(9180135, objects[1]);

		objects = (Object[]) list.get(2);
		Assertions.assertEquals("Region South West", objects[0]);
		Assertions.assertEquals(5624696, objects[1]);

		objects = (Object[]) list.get(3);
		Assertions.assertEquals("Region Yorkshire and the Humber", objects[0]);
		Assertions.assertEquals(5284000, objects[1]);

		// 2nd test
		query = em.createQuery(
				"select r from Region r where LENGTH(CONCAT('Region',' ',r.name)) = (select MAX(LENGTH(CONCAT('Region',' ',r2.name))) from Region r2)");

		list = query.getResultList();
		Assertions.assertEquals(1, list.size());

		Region r = (Region) list.get(0);
		Assertions.assertNotNull(r);
		Assertions.assertEquals("Yorkshire and the Humber", r.getName());

		em.remove(yorkShireRegion);
		em.remove(northWestRegion);
		em.remove(southEastRegion);
		em.remove(southWestRegion);
		tx.commit();

		em.close();
	}

	@Test
	public void jpqlLocateSubstringFunctions() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		Region northEastRegion = createNorthEast();
		em.persist(northEastRegion);

		Region northWestRegion = createNorthWest();
		em.persist(northWestRegion);

		Region southWestRegion = createSouthWest();
		em.persist(southWestRegion);

		Region southEastRegion = createSouthEast();
		em.persist(southEastRegion);

		Query query = em.createQuery(
				"select distinct TRIM(SUBSTRING(r.name,LOCATE(' ',r.name))) as sub from Region r order by sub");
		List list = query.getResultList();
		Assertions.assertEquals(2, list.size());

		String sub = (String) list.get(0);
		Assertions.assertNotNull(sub);
		Assertions.assertEquals("East", sub);

		sub = (String) list.get(1);
		Assertions.assertNotNull(sub);
		Assertions.assertEquals("West", sub);

		em.remove(northEastRegion);
		em.remove(northWestRegion);
		em.remove(southEastRegion);
		em.remove(southWestRegion);
		tx.commit();

		em.close();
	}

}
