package org.minijpa.jpa.onetomany;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
    public static void beforeAll() throws Exception {
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

        tx.begin();
        Store s = em.find(Store.class, store.getId());
        Assertions.assertTrue(!s.getItems().isEmpty());
        Assertions.assertEquals(2, s.getItems().size());
        Assertions.assertFalse(s == store);

        em.remove(s);
        em.remove(item1);
        em.remove(item2);
        tx.commit();

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

        em.remove(s);
        em.remove(item1);
        em.remove(item2);
        em.remove(item3);
        tx.commit();

        LOG.info("persistCollection: s=" + s);
        em.close();
    }

    @Test
    public void likeCriteria() throws Exception {
        final EntityManager em = emf.createEntityManager();
        Store store = new Store();
        store.setName("Upton Store");

        Item item1 = new Item();
        item1.setName("Notepad");
        item1.setModel("Free_Inch");

        Item item2 = new Item();
        item2.setName("Pencil");
        item2.setModel("Staedtler%Walls");

        Item item3 = new Item();
        item3.setName("Pen");
        item3.setModel("Pen");

        store.setItems(Arrays.asList(item1, item2, item3));

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(item1);
        em.persist(store);
        em.persist(item2);
        em.persist(item3);

        tx.commit();

        Assertions.assertFalse(store.getItems().isEmpty());

        em.detach(store);

        tx.begin();
        testLike1(em);
        tx.commit();

        tx.begin();
        testLike2(em);
        tx.commit();

        tx.begin();
        testLike3(em);
        tx.commit();

        tx.begin();
        testLike4(em);
        tx.commit();

        tx.begin();
        testLike5(em);
        tx.commit();

        tx.begin();
        Store s = em.find(Store.class, store.getId());
        em.remove(s);
        em.remove(item1);
        em.remove(item2);
        em.remove(item3);
        tx.commit();

        em.close();
    }

    private void testLike1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.like(root.get("model"), "Free\\_I%", '\\');
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("Notepad", items.get(0).getName());
        Assertions.assertEquals("Free_Inch", items.get(0).getModel());
    }

    private void testLike2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.like(root.get("model"), root.get("name"), '\\');
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("Pen", items.get(0).getName());
        Assertions.assertEquals("Pen", items.get(0).getModel());
    }

    private void testLike3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.like(root.get("model"), cb.parameter(String.class, "pattern"), '\\');
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("pattern", "Free\\_I%");
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("Notepad", items.get(0).getName());
        Assertions.assertEquals("Free_Inch", items.get(0).getModel());
    }

    private void testLike4(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.like(root.get("model"), root.get("name"), cb.parameter(Character.class, "escape"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("escape", '\\');
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("Pen", items.get(0).getName());
        Assertions.assertEquals("Pen", items.get(0).getModel());
    }

    private void testLike5(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.like(root.get("model"), "Free\\_I%", cb.parameter(Character.class, "escape"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("escape", '\\');
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("Notepad", items.get(0).getName());
        Assertions.assertEquals("Free_Inch", items.get(0).getModel());
    }

    @Test
    public void notLikeCriteria() throws Exception {
        final EntityManager em = emf.createEntityManager();
        Store store = new Store();
        store.setName("Upton Store");

        Item item1 = new Item();
        item1.setName("Notepad");
        item1.setModel("Free_Inch");

        Item item2 = new Item();
        item2.setName("Pencil");
        item2.setModel("Staedtler%Walls");

        Item item3 = new Item();
        item3.setName("Pen");
        item3.setModel("Pen");

        store.setItems(Arrays.asList(item1, item2, item3));

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(item1);
        em.persist(store);
        em.persist(item2);
        em.persist(item3);

        tx.commit();

        Assertions.assertFalse(store.getItems().isEmpty());

        em.detach(store);

        tx.begin();
        testNotLike1(em);
        tx.commit();

        tx.begin();
        testNotLike2(em);
        tx.commit();

        tx.begin();
        testNotLike3(em);
        tx.commit();

        tx.begin();
        testNotLike4(em);
        tx.commit();

        tx.begin();
        testNotLike5(em);
        tx.commit();

        tx.begin();
        Store s = em.find(Store.class, store.getId());
        em.remove(s);
        em.remove(item1);
        em.remove(item2);
        em.remove(item3);
        tx.commit();

        em.close();
    }

    private void testNotLike1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.notLike(root.get("model"), "Free\\_I%", '\\');
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals("Pen", items.get(0).getName());
        Assertions.assertEquals("Pen", items.get(0).getModel());
        Assertions.assertEquals("Pencil", items.get(1).getName());
        Assertions.assertEquals("Staedtler%Walls", items.get(1).getModel());
    }

    private void testNotLike2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.notLike(root.get("model"), root.get("name"), '\\');
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals("Notepad", items.get(0).getName());
        Assertions.assertEquals("Free_Inch", items.get(0).getModel());
        Assertions.assertEquals("Pencil", items.get(1).getName());
        Assertions.assertEquals("Staedtler%Walls", items.get(1).getModel());
    }

    private void testNotLike3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.notLike(root.get("model"), cb.parameter(String.class, "pattern"), '\\');
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("pattern", "Free\\_I%");
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals("Pen", items.get(0).getName());
        Assertions.assertEquals("Pen", items.get(0).getModel());
        Assertions.assertEquals("Pencil", items.get(1).getName());
        Assertions.assertEquals("Staedtler%Walls", items.get(1).getModel());
    }

    private void testNotLike4(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.notLike(root.get("model"), root.get("name"), cb.parameter(Character.class, "escape"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("escape", '\\');
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals("Notepad", items.get(0).getName());
        Assertions.assertEquals("Free_Inch", items.get(0).getModel());
        Assertions.assertEquals("Pencil", items.get(1).getName());
        Assertions.assertEquals("Staedtler%Walls", items.get(1).getModel());
    }

    private void testNotLike5(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteriaQuery = cb.createQuery(Item.class);
        Root<Item> root = criteriaQuery.from(Item.class);
        Predicate predicate = cb.notLike(root.get("model"), "Free\\_I%", cb.parameter(Character.class, "escape"));
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(root);
        TypedQuery<Item> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("escape", '\\');
        List<Item> items = typedQuery.getResultList();
        Assertions.assertEquals(2, items.size());
        Assertions.assertEquals("Pen", items.get(0).getName());
        Assertions.assertEquals("Pen", items.get(0).getModel());
        Assertions.assertEquals("Pencil", items.get(1).getName());
        Assertions.assertEquals("Staedtler%Walls", items.get(1).getModel());
    }

}
