package org.minijpa.jpa.onetoone;

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
	emf = Persistence.createEntityManagerFactory("cities_uni");
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
	    Integer totalPopulation = (Integer) query.getSingleResult();
	    Assertions.assertNotNull(totalPopulation);
	    Assertions.assertEquals(763476, totalPopulation);

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
	    Double avgPopulation = (Double) query.getSingleResult();
	    Assertions.assertNotNull(avgPopulation);
	    Assertions.assertEquals(381738.0, avgPopulation);

	    em.remove(yorkCity);
	    em.remove(manchesterCity);
	    tx.commit();
	} finally {
	    em.close();
	}
    }
}
