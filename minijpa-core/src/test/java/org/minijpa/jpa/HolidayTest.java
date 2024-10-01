package org.minijpa.jpa;

import java.time.LocalDate;
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
import javax.persistence.criteria.Predicate.BooleanOperator;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Holiday;

public class HolidayTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("holidays", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    private Holiday holiday1() {
        Holiday holiday = new Holiday();
        holiday.setCheckIn(LocalDate.of(2020, 1, 10));
        holiday.setNights(7);
        holiday.setTravellers(2);
        holiday.setReferenceName("George Bann");
        return holiday;
    }

    private Holiday holiday2() {
        Holiday holiday = new Holiday();
        holiday.setCheckIn(LocalDate.of(2020, 3, 12));
        holiday.setNights(4);
        holiday.setTravellers(3);
        holiday.setReferenceName("Jennifer Gold");
        return holiday;
    }

    private Holiday holiday3() {
        Holiday holiday = new Holiday();
        holiday.setCheckIn(LocalDate.of(2020, 4, 18));
        holiday.setNights(10);
        holiday.setTravellers(4);
        holiday.setReferenceName("Albert Gould");
        return holiday;
    }

    private Holiday holiday4() {
        Holiday holiday = new Holiday();
        holiday.setCheckIn(LocalDate.of(2021, 1, 8));
        holiday.setNights(9);
        holiday.setTravellers(6);
        holiday.setReferenceName("Robert Stevenson");
        return holiday;
    }

    private Holiday holiday5() {
        Holiday holiday = new Holiday();
        holiday.setCheckIn(LocalDate.of(2021, 2, 15));
        holiday.setNights(5);
        holiday.setTravellers(5);
        holiday.setReferenceName("Robert Blond");
        return holiday;
    }

    @Test
    public void gtCriteria() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Holiday h1 = holiday1();
        Holiday h2 = holiday2();
        Holiday h3 = holiday3();
        Holiday h4 = holiday4();
        Holiday h5 = holiday5();

        em.persist(h1);
        em.persist(h2);
        em.persist(h3);
        em.persist(h4);
        em.persist(h5);

        // Holiday h1r = em.find(Holiday.class, h1.getId());
        // Assertions.assertTrue(h1 == h1r);
        // em.detach(h1);
        // Assertions.assertNotNull(h1.getId());
        // h1r = em.find(Holiday.class, h1.getId());
        // Assertions.assertNull(h1r);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Holiday> cq = cb.createQuery(Holiday.class);
        Root<Holiday> root = cq.from(Holiday.class);

        // greaterThan on int
        Predicate predicate = cb.greaterThan(root.get("nights"), 4);
        Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
        Assertions.assertEquals(Boolean.class, predicate.getJavaType());
        Assertions.assertFalse(predicate.isCompoundSelection());

        cq.where(predicate);
        cq.select(root);

        TypedQuery<Holiday> typedQuery = em.createQuery(cq);
        List<Holiday> holidays = typedQuery.getResultList();
        Assertions.assertEquals(4, holidays.size());

        // gt on int
        predicate = cb.gt(root.get("nights"), 4);
        Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
        Assertions.assertEquals(Boolean.class, predicate.getJavaType());
        Assertions.assertFalse(predicate.isCompoundSelection());

        cq.where(predicate);
        cq.select(root);

        typedQuery = em.createQuery(cq);
        holidays = typedQuery.getResultList();
        Assertions.assertEquals(4, holidays.size());

        // greaterThan on date
        predicate = cb.greaterThan(root.get("checkIn"), LocalDate.of(2020, 4, 18));
        Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
        Assertions.assertEquals(Boolean.class, predicate.getJavaType());
        Assertions.assertFalse(predicate.isCompoundSelection());

        cq.where(predicate);
        cq.select(root);

        typedQuery = em.createQuery(cq);
        holidays = typedQuery.getResultList();
        Assertions.assertEquals(2, holidays.size());

        // between on int
        predicate = cb.between(root.get("nights"), 7, 10);
        Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
        Assertions.assertEquals(Boolean.class, predicate.getJavaType());
        Assertions.assertFalse(predicate.isCompoundSelection());

        cq.where(predicate);
        cq.select(root);

        typedQuery = em.createQuery(cq);
        holidays = typedQuery.getResultList();
        Assertions.assertEquals(3, holidays.size());

        // like on referenceName
        predicate = cb.like(root.get("referenceName"), "%Robert%");
        Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
        Assertions.assertEquals(Boolean.class, predicate.getJavaType());
        Assertions.assertFalse(predicate.isCompoundSelection());

        cq.where(predicate);
        cq.select(root);

        typedQuery = em.createQuery(cq);
        holidays = typedQuery.getResultList();
        Assertions.assertEquals(2, holidays.size());

        // lessThan on int
        predicate = cb.lessThan(root.get("nights"), 5);
        Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
        Assertions.assertEquals(Boolean.class, predicate.getJavaType());
        Assertions.assertFalse(predicate.isCompoundSelection());

        cq.where(predicate);
        cq.select(root);

        typedQuery = em.createQuery(cq);
        holidays = typedQuery.getResultList();
        Assertions.assertEquals(1, holidays.size());

        em.remove(h1);
        em.remove(h2);
        em.remove(h3);
        em.remove(h4);
        em.remove(h5);

        tx.commit();
        em.close();
    }

    @Test
    public void pc() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Holiday h1 = holiday1();

        em.persist(h1);

        tx.commit();

        tx.begin();
        Holiday h2 = holiday2();
        Holiday h3 = holiday3();
        Holiday h4 = holiday4();
        Holiday h5 = holiday5();
        em.persist(h2);
        em.persist(h3);
        em.persist(h4);
        em.persist(h5);

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Holiday> cq = cb.createQuery(Holiday.class);
        Root<Holiday> root = cq.from(Holiday.class);

        cq.select(root);

        TypedQuery<Holiday> typedQuery = em.createQuery(cq);
        List<Holiday> holidays = typedQuery.getResultList();
        Assertions.assertEquals(5, holidays.size());

        em.remove(h1);
        em.remove(h2);
        em.remove(h3);
        em.remove(h4);
        em.remove(h5);

        tx.commit();
        em.close();
    }

    @Test
    public void jpqlBetween() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Holiday h1 = holiday1();
        Holiday h2 = holiday2();
        Holiday h3 = holiday3();
        Holiday h4 = holiday4();
        Holiday h5 = holiday5();

        em.persist(h1);
        em.persist(h2);
        em.persist(h3);
        em.persist(h4);
        em.persist(h5);

        Query query = em.createQuery("select hl from Holiday hl where hl.nights between 7 and 10");
        List list = query.getResultList();
        Assertions.assertEquals(3, list.size());

        em.remove(h1);
        em.remove(h2);
        em.remove(h3);
        em.remove(h4);
        em.remove(h5);

        tx.commit();
        em.close();
    }

    @Test
    public void betweenDates() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Holiday h1 = holiday1();
        Holiday h2 = holiday2();

        em.persist(h1);
        em.persist(h2);
        tx.commit();

        tx.begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Holiday> cq = cb.createQuery(Holiday.class);
        Root<Holiday> root = cq.from(Holiday.class);

        Predicate predicate = cb.between(root.get("checkIn"), LocalDate.of(2020, 1, 9), LocalDate.of(2020, 1, 11));

        cq.where(predicate);
        cq.select(root);

        TypedQuery<Holiday> typedQuery = em.createQuery(cq);
        List<Holiday> holidays = typedQuery.getResultList();
        Assertions.assertEquals(1, holidays.size());

        em.remove(h1);
        em.remove(h2);
        tx.commit();

        em.close();
    }


    @Test
    public void betweenDatesNamedQuery() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Holiday h1 = holiday1();
        Holiday h2 = holiday2();

        em.persist(h1);
        em.persist(h2);
        tx.commit();

        tx.begin();
        TypedQuery<Holiday> query = em.createNamedQuery("checkInPeriod", Holiday.class);
        query.setParameter("dateStart", LocalDate.of(2020, 1, 9));
        query.setParameter("dateEnd", LocalDate.of(2020, 1, 11));
        List<Holiday> holidays = query.getResultList();
        Assertions.assertEquals(1, holidays.size());

        em.remove(h1);
        em.remove(h2);
        tx.commit();

        em.close();
    }


    @Test
    public void betweenDatesNamedNativeQuery() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Holiday h1 = holiday1();
        Holiday h2 = holiday2();

        em.persist(h1);
        em.persist(h2);
        tx.commit();

        tx.begin();
        TypedQuery<Holiday> query = em.createNamedQuery("nativeCheckInPeriod", Holiday.class);
        query.setParameter("dateStart", LocalDate.of(2020, 1, 9));
        query.setParameter("dateEnd", LocalDate.of(2020, 1, 11));
        List<Holiday> holidays = query.getResultList();
        Assertions.assertEquals(1, holidays.size());

        em.remove(h1);
        em.remove(h2);
        tx.commit();

        em.close();
    }
}
