package org.minijpa.jpa.onetomany;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Item;
import org.minijpa.jpa.model.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 *
 */
public class OneToManyUniTest {

    private Logger LOG = LoggerFactory.getLogger(OneToManyUniTest.class);
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("onetomany_uni", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void persist() throws Exception {
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
    }

    @Test
    public void persistCollection() throws Exception {
	final EntityManager em = emf.createEntityManager();
	Store store = new Store();
	store.setName("Upton Store");

	Item item1 = new Item();
	item1.setName("Notepad");
	item1.setModel("Free Inch");

	Item item2 = new Item();
	item2.setName("Pencil");
	item2.setModel("Staedtler");

	List list = new ArrayList();
	list.add(item1);
	list.add(item2);
	store.setItems(list);

	final EntityTransaction tx = em.getTransaction();
	tx.begin();

	em.persist(item1);
	em.persist(store);
	em.persist(item2);

	tx.commit();

	tx.begin();
	Item item3 = new Item();
	item3.setName("Pen");
	item3.setModel("Bic");
	em.persist(item3);

	store.getItems().add(item3);
	em.persist(store);

	tx.commit();

	Assertions.assertFalse(store.getItems().isEmpty());

	em.detach(store);

	tx.begin();
	Store s = em.find(Store.class, store.getId());
	Assertions.assertTrue(!s.getItems().isEmpty());
	Assertions.assertEquals(3, s.getItems().size());
	Assertions.assertFalse(s == store);

	Query query = em.createNativeQuery("select count(*) from store_items where store_id=" + s.getId());
	Number count = (Number) query.getSingleResult();
	Assertions.assertEquals(3, count.intValue());

	tx.commit();

	LOG.info("persistCollection: s=" + s);
	em.close();
    }

}
