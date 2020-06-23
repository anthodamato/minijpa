package org.tinyjpa.jpa.onetoone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.PersistenceProviderImpl;
import org.tinyjpa.jpa.model.onetoone.Fingerprint;
import org.tinyjpa.jpa.model.onetoone.Person;

/**
 * java -jar $DERBY_HOME/lib/derbyrun.jar server start
 * 
 * connect 'jdbc:derby://localhost:1527/test';
 * 
 * @author adamato
 *
 */
public class OneToOneBidTest {

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = new PersistenceProviderImpl()
				.createEntityManagerFactory("/org/tinyjpa/jpa/onetoone/persistence.xml", "onetoone_bid", null);
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			Person person = new Person();
			person.setName("John Smith");

			Fingerprint fingerprint = new Fingerprint();
			fingerprint.setType("arch");
			fingerprint.setPerson(person);
			person.setFingerprint(fingerprint);

			em.persist(person);
			em.persist(fingerprint);

			tx.commit();

			em.detach(person);

			Person p = em.find(Person.class, person.getId());
			Assertions.assertNotNull(p);
			Assertions.assertFalse(p == person);
			Assertions.assertEquals(person.getId(), p.getId());
			Assertions.assertNotNull(p.getFingerprint());
			Assertions.assertEquals("John Smith", p.getName());
			Assertions.assertEquals("arch", p.getFingerprint().getType());

		} finally {
			em.close();
			emf.close();
		}
	}

}
