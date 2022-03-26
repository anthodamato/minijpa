package org.minijpa.jpa.onetoone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Capital;
import org.minijpa.jpa.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 *
 */
public class OneToOneBidLazyTest {

    private Logger LOG = LoggerFactory.getLogger(OneToOneBidLazyTest.class);

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("onetoone_bid_lazy", PersistenceUnitProperties.getProperties());
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

	    State state = new State();
	    state.setName("England");

	    Capital capital = new Capital();
	    capital.setName("London");

	    state.setCapital(capital);

	    em.persist(capital);
	    em.persist(state);

	    tx.commit();

	    em.detach(capital);
	    em.detach(state);
	    State s = em.find(State.class, state.getId());

	    Assertions.assertFalse(s == state);
	    Assertions.assertEquals("England", state.getName());
	    Capital c = s.getCapital();
	    Assertions.assertNotNull(c);
	    Assertions.assertEquals("London", c.getName());
	    em.remove(c);
	    em.remove(s);
	    em.flush();
	} finally {
	    em.close();
	}
    }

}
