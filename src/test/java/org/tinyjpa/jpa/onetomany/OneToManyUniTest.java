package org.tinyjpa.jpa.onetomany;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.model.onetomany.Item;
import org.tinyjpa.jpa.model.onetomany.Store;

/**
 * 
 * @author adamato
 *
 */
public class OneToManyUniTest {

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetomany_uni");
		final EntityManager em = emf.createEntityManager();
		Store store = new Store();
		store.setName("Upton Store");

		Item item1 = new Item();
		item1.setName("Notepad");
		item1.setModel("Free Ink");

		Item item2 = new Item();
		item2.setName("Pencil");
		item2.setModel("Staedtler");

		store.setItems(Arrays.asList(item1, item2));

		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.persist(item1);
		em.persist(store);
		em.persist(item2);

		tx.commit();

		Assertions.assertFalse(store.getItems().isEmpty());

		em.detach(store);

		Store s = em.find(Store.class, store.getId());
		Assertions.assertTrue(!s.getItems().isEmpty());
		Assertions.assertEquals(2, s.getItems().size());
		Assertions.assertFalse(s == store);

		em.close();
		emf.close();
	}

}
