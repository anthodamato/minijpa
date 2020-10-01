package org.tinyjpa.jpa.onetoone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jpa.model.onetoone.lazy.Capital;
import org.tinyjpa.jpa.model.onetoone.lazy.State;

/**
 * 
 * @author adamato
 *
 */
public class OneToOneBidLazyTest {
	private Logger LOG = LoggerFactory.getLogger(OneToOneBidLazyTest.class);

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetoone_bid_lazy");
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
			LOG.info("Loading s.getCapital()");
			Capital c = s.getCapital();
			Assertions.assertNotNull(c);
			Assertions.assertEquals("London", c.getName());
		} finally {
			em.close();
			emf.close();
		}
	}

}
