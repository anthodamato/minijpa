package org.minijpa.jpa;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hamcrest.MatcherAssert;
import org.hamcrest.number.IsCloseTo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.SearchData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchDataTest {
    private Logger LOG = LoggerFactory.getLogger(SearchDataTest.class);
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("search_data", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void notEqualCriteria() throws Exception {
        final EntityManager em = emf.createEntityManager();

        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData1.setAverageValue(4);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(5);
        searchData1.setAverageValue(5);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SearchData> criteriaQuery = cb.createQuery(SearchData.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate1 = cb.notEqual(root.get("model"), "Free");
        Predicate predicate2 = cb.notEqual(root.get("model"), "Usual");
        Predicate predicate = cb.and(predicate1, predicate2);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        TypedQuery<SearchData> typedQuery = em.createQuery(criteriaQuery);
        List<SearchData> searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        SearchData searchData = searchDataList.get(0);
        Assertions.assertEquals("SearchName2", searchData.getName());
        Assertions.assertEquals("Special", searchData.getModel());
        tx.commit();

        tx.begin();
        predicate = cb.notEqual(root.get("occurences"), root.get("averageValue"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    @Test
    public void greaterThanLessThanCriteria() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(5);
        searchData3.setAverageValue(5);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SearchData> criteriaQuery = cb.createQuery(SearchData.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.greaterThan(root.get("occurences"), 4);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(root);
        TypedQuery<SearchData> typedQuery = em.createQuery(criteriaQuery);
        List<SearchData> searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName3", searchDataList.get(0).getName());
        Assertions.assertEquals("Usual", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.gt(root.get("occurences"), new BigInteger("4"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName3", searchDataList.get(0).getName());
        Assertions.assertEquals("Usual", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.gt(root.get("occurences"), Float.valueOf(4.0f));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName3", searchDataList.get(0).getName());
        Assertions.assertEquals("Usual", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.greaterThan(root.get("averageValue"), root.get("occurences"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.gt(root.get("averageValue"), root.get("occurences"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.greaterThanOrEqualTo(root.get("occurences"), root.get("averageValue"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(2, searchDataList.size());
        Assertions.assertEquals("SearchName2", searchDataList.get(0).getName());
        Assertions.assertEquals("Special", searchDataList.get(0).getModel());
        Assertions.assertEquals("SearchName3", searchDataList.get(1).getName());
        Assertions.assertEquals("Usual", searchDataList.get(1).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.ge(root.get("occurences"), root.get("averageValue"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(2, searchDataList.size());
        Assertions.assertEquals("SearchName2", searchDataList.get(0).getName());
        Assertions.assertEquals("Special", searchDataList.get(0).getModel());
        Assertions.assertEquals("SearchName3", searchDataList.get(1).getName());
        Assertions.assertEquals("Usual", searchDataList.get(1).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.greaterThanOrEqualTo(root.get("occurences"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName3", searchDataList.get(0).getName());
        Assertions.assertEquals("Usual", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.ge(root.get("occurences"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName3", searchDataList.get(0).getName());
        Assertions.assertEquals("Usual", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.lessThan(root.get("occurences"), root.get("averageValue"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.lt(root.get("occurences"), root.get("averageValue"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.lessThan(root.get("occurences"), 4);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.lt(root.get("occurences"), 4);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.lessThanOrEqualTo(root.get("averageValue"), root.get("occurences"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(2, searchDataList.size());
        Assertions.assertEquals("SearchName2", searchDataList.get(0).getName());
        Assertions.assertEquals("Special", searchDataList.get(0).getModel());
        Assertions.assertEquals("SearchName3", searchDataList.get(1).getName());
        Assertions.assertEquals("Usual", searchDataList.get(1).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.le(root.get("averageValue"), root.get("occurences"));
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(2, searchDataList.size());
        Assertions.assertEquals("SearchName2", searchDataList.get(0).getName());
        Assertions.assertEquals("Special", searchDataList.get(0).getModel());
        Assertions.assertEquals("SearchName3", searchDataList.get(1).getName());
        Assertions.assertEquals("Usual", searchDataList.get(1).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.lessThanOrEqualTo(root.get("occurences"), 3);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        predicate = cb.le(root.get("occurences"), 3);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(1, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        tx.commit();

        tx.begin();
        Predicate predicate1 = cb.equal(root.get("occurences"), 3);
        Predicate predicate2 = cb.equal(root.get("occurences"), 4);
        predicate = cb.or(predicate1, predicate2);
        criteriaQuery.where(predicate);
        criteriaQuery.select(root);
        typedQuery = em.createQuery(criteriaQuery);
        searchDataList = typedQuery.getResultList();
        Assertions.assertEquals(2, searchDataList.size());
        Assertions.assertEquals("SearchName1", searchDataList.get(0).getName());
        Assertions.assertEquals("Free", searchDataList.get(0).getModel());
        Assertions.assertEquals("SearchName2", searchDataList.get(1).getName());
        Assertions.assertEquals("Special", searchDataList.get(1).getModel());
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    @Test
    public void criteriaSumProdDiff() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(5);
        searchData3.setAverageValue(5);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = cb.createQuery(BigDecimal.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 4);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.sum(root.get("averageValue"), root.get("occurences")));
        TypedQuery<BigDecimal> typedQuery = em.createQuery(criteriaQuery);
        List<BigDecimal> sumList = typedQuery.getResultList();
        Assertions.assertEquals(2, sumList.size());
        Assertions.assertEquals(7, sumList.get(0));
        Assertions.assertEquals(8, sumList.get(1));
        tx.commit();

        tx.begin();
        testSum2(em);
        tx.commit();

        tx.begin();
        testOr(em);
        tx.commit();

        tx.begin();
        testProd1(em);
        tx.commit();

        tx.begin();
        testProd2(em);
        tx.commit();

        tx.begin();
        testProd3(em);
        tx.commit();

        tx.begin();
        testDiff1(em);
        tx.commit();

        tx.begin();
        testDiff2(em);
        tx.commit();

        tx.begin();
        testDiff3(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testSum2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 4);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.sum(4.1f, root.get("occurences")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> sumList = typedQuery.getResultList();
        Assertions.assertEquals(2, sumList.size());
        Assertions.assertEquals(7.1f, sumList.get(0));
        Assertions.assertEquals(8.1f, sumList.get(1));
    }

    private void testOr(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.or(cb.equal(root.get("occurences"), 4), cb.equal(root.get("occurences"), 3));
        criteriaQuery.where(predicate).orderBy(cb.desc(root.get("name")));
        criteriaQuery.select(cb.sum(4.1f, root.get("occurences")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> sumList = typedQuery.getResultList();
        Assertions.assertEquals(2, sumList.size());
        Assertions.assertEquals(8.1f, sumList.get(0));
        Assertions.assertEquals(7.1f, sumList.get(1));
    }

    private void testProd1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = cb.createQuery(BigDecimal.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.prod(root.get("averageValue"), root.get("occurences")));
        TypedQuery<BigDecimal> typedQuery = em.createQuery(criteriaQuery);
        List<BigDecimal> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(25, sumList.get(0));
    }

    private void testProd2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = cb.createQuery(BigDecimal.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.prod(root.get("averageValue"), BigDecimal.valueOf(3)));
        TypedQuery<BigDecimal> typedQuery = em.createQuery(criteriaQuery);
        List<BigDecimal> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(BigDecimal.valueOf(15), resultList.get(0));
    }

    private void testProd3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = cb.createQuery(BigDecimal.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.prod(BigDecimal.valueOf(2), root.get("occurences")));
        TypedQuery<BigDecimal> typedQuery = em.createQuery(criteriaQuery);
        List<BigDecimal> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(BigDecimal.valueOf(10), sumList.get(0));
    }

    private void testDiff1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = cb.createQuery(BigDecimal.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.diff(root.get("averageValue"), root.get("occurences")));
        TypedQuery<BigDecimal> typedQuery = em.createQuery(criteriaQuery);
        List<BigDecimal> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(0, sumList.get(0));
    }

    private void testDiff2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.diff(root.get("averageValue"), 1));
        TypedQuery<Integer> typedQuery = em.createQuery(criteriaQuery);
        List<Integer> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(4, sumList.get(0));
    }

    private void testDiff3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.diff(10, root.get("averageValue")));
        TypedQuery<Integer> typedQuery = em.createQuery(criteriaQuery);
        List<Integer> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(5, sumList.get(0));
    }

    @Test
    public void criteriaQuot() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testQuot1(em);
        tx.commit();

        tx.begin();
        testQuot2(em);
        tx.commit();

        tx.begin();
        testQuot3(em);
        tx.commit();

        tx.begin();
        testSqrt(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testQuot1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.quot(root.get("occurences"), root.get("averageValue")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(4, sumList.get(0));
    }

    private void testQuot2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.quot(root.get("occurences"), 5f));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(4.8f, sumList.get(0));
    }

    private void testQuot3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.quot(26f, root.get("averageValue")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(5.2f, sumList.get(0));
    }

    private void testSqrt(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.sqrt(root.get("occurences")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        MatcherAssert.assertThat((double) resultList.get(0), IsCloseTo.closeTo(4.8989797f, 0.000001f));
    }

    @Test
    public void criteriaTypecast() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testToBigDecimal(em);
        tx.commit();

        tx.begin();
        testToBigInteger(em);
        tx.commit();

        tx.begin();
        testToDouble(em);
        tx.commit();

        tx.begin();
        testToFloat(em);
        tx.commit();

        tx.begin();
        testToInteger(em);
        tx.commit();

        tx.begin();
        testToLong(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testToBigDecimal(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> criteriaQuery = cb.createQuery(BigDecimal.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.toBigDecimal(cb.prod(root.get("averageValue"), root.get("occurences"))));
        TypedQuery<BigDecimal> typedQuery = em.createQuery(criteriaQuery);
        List<BigDecimal> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        LOG.debug("testToBigDecimal: sumList.get(0).getClass()={}", sumList.get(0).getClass());
        Assertions.assertEquals(BigDecimal.valueOf(120), sumList.get(0));
    }

    private void testToBigInteger(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<BigInteger> criteriaQuery = cb.createQuery(BigInteger.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.toBigInteger(cb.prod(root.get("averageValue"), root.get("occurences"))));
        TypedQuery<BigInteger> typedQuery = em.createQuery(criteriaQuery);
        List<BigInteger> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(BigInteger.valueOf(120), sumList.get(0));
    }

    private void testToDouble(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = cb.createQuery(Double.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.toDouble(cb.prod(root.get("averageValue"), root.get("occurences"))));
        TypedQuery<Double> typedQuery = em.createQuery(criteriaQuery);
        List<Double> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(Double.valueOf(120), sumList.get(0));
    }

    private void testToFloat(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Float> criteriaQuery = cb.createQuery(Float.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.toFloat(cb.prod(root.get("averageValue"), root.get("occurences"))));
        TypedQuery<Float> typedQuery = em.createQuery(criteriaQuery);
        List<Float> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(Float.valueOf(120), sumList.get(0));
    }

    private void testToInteger(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.toInteger(cb.prod(root.get("averageValue"), root.get("occurences"))));
        TypedQuery<Integer> typedQuery = em.createQuery(criteriaQuery);
        List<Integer> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(Integer.valueOf(120), sumList.get(0));
    }

    private void testToLong(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate).orderBy(cb.asc(root.get("name")));
        criteriaQuery.select(cb.toLong(cb.prod(root.get("averageValue"), root.get("occurences"))));
        TypedQuery<Long> typedQuery = em.createQuery(criteriaQuery);
        List<Long> sumList = typedQuery.getResultList();
        Assertions.assertEquals(1, sumList.size());
        Assertions.assertEquals(Long.valueOf(120), sumList.get(0));
    }

    @Test
    public void notSum() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);
        searchData1.setFloatAverageValue(-4.0f);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);
        searchData2.setFloatAverageValue(-5.0f);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);
        searchData3.setFloatAverageValue(-7.0f);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testNotSumAsLong(em);
        tx.commit();

        tx.begin();
        testNotSumAsDouble(em);
        tx.commit();

        tx.begin();
        testAbs(em);
        tx.commit();

        tx.begin();
        testMod1(em);
        tx.commit();

        tx.begin();
        testMod2(em);
        tx.commit();

        tx.begin();
        testMod3(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testNotSumAsLong(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.not(cb.equal(root.get("averageValue"), 5));
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.sumAsLong(root.get("occurences")));
        TypedQuery<Long> typedQuery = em.createQuery(criteriaQuery);
        List<Long> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(7L, resultList.get(0));
    }

    private void testNotSumAsDouble(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Double> criteriaQuery = cb.createQuery(Double.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.not(cb.equal(root.get("averageValue"), 5));
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.sumAsDouble(root.get("occurences")));
        TypedQuery<Double> typedQuery = em.createQuery(criteriaQuery);
        List<Double> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(7d, resultList.get(0));
    }

    private void testAbs(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.orderBy(cb.desc(root.get("name")));
        criteriaQuery.select(cb.abs(root.get("floatAverageValue")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> resultList = typedQuery.getResultList();
        Assertions.assertEquals(3, resultList.size());

        // Oracle returns a BigDecimal, MySql a Double
        Assertions.assertEquals(7.0f, ((Number) resultList.get(0)).floatValue());

        // Oracle returns a BigDecimal, MySql a Double
        Assertions.assertEquals(5.0f, ((Number) resultList.get(1)).floatValue());

        // Oracle returns a BigDecimal, MySql a Double
        Assertions.assertEquals(4.0f, ((Number) resultList.get(2)).floatValue());
    }

    private void testMod1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        criteriaQuery.orderBy(cb.desc(root.get("name")));
        criteriaQuery.select(cb.mod(root.get("occurences"), root.get("averageValue")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> resultList = typedQuery.getResultList();
        Assertions.assertEquals(3, resultList.size());
        Assertions.assertEquals(4, resultList.get(0));
        Assertions.assertEquals(0, resultList.get(1));
        Assertions.assertEquals(3, resultList.get(2));
    }

    private void testMod2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        criteriaQuery.orderBy(cb.desc(root.get("name")));
        criteriaQuery.select(cb.mod(50, root.get("occurences")));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> resultList = typedQuery.getResultList();
        Assertions.assertEquals(3, resultList.size());
        Assertions.assertEquals(2, resultList.get(0));
        Assertions.assertEquals(2, resultList.get(1));
        Assertions.assertEquals(2, resultList.get(2));
    }

    private void testMod3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Number> criteriaQuery = cb.createQuery(Number.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        criteriaQuery.orderBy(cb.desc(root.get("name")));
        criteriaQuery.select(cb.mod(root.get("occurences"), 2));
        TypedQuery<Number> typedQuery = em.createQuery(criteriaQuery);
        List<Number> resultList = typedQuery.getResultList();
        Assertions.assertEquals(3, resultList.size());
        Assertions.assertEquals(0, resultList.get(0));
        Assertions.assertEquals(0, resultList.get(1));
        Assertions.assertEquals(1, resultList.get(2));
    }

    @Test
    public void concat() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);
        searchData1.setFloatAverageValue(-4.2f);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);
        searchData2.setFloatAverageValue(-5.2f);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);
        searchData3.setFloatAverageValue(-7.8f);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testConcat1(em);
        tx.commit();

        tx.begin();
        testConcat2(em);
        tx.commit();

        tx.begin();
        testConcat3(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testConcat1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.concat(root.get("name"), root.get("model")));
        TypedQuery<String> typedQuery = em.createQuery(criteriaQuery);
        List<String> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("SearchName3Usual", resultList.get(0));
    }

    private void testConcat2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.upper(cb.concat("Nice ", root.get("model"))));
        TypedQuery<String> typedQuery = em.createQuery(criteriaQuery);
        List<String> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("NICE USUAL", resultList.get(0));
    }

    private void testConcat3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.lower(cb.concat(root.get("model"), " N. 43")));
        TypedQuery<String> typedQuery = em.createQuery(criteriaQuery);
        List<String> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("usual n. 43", resultList.get(0));
    }

    @Test
    public void locate() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);
        searchData1.setFloatAverageValue(-4.2f);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2Name");
        searchData2.setModel("Special");
        searchData2.setPattern("Na");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(7);
        searchData2.setFloatAverageValue(-5.2f);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);
        searchData3.setFloatAverageValue(-7.8f);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testLocate1(em);
        tx.commit();

        tx.begin();
        testLocate2(em);
        tx.commit();

        tx.begin();
        testLocate3(em);
        tx.commit();

        tx.begin();
        testLocate4(em);
        tx.commit();

        tx.begin();
        testLocate5(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testLocate1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.locate(root.get("name"), "Name"));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(7, resultList.get(0));
    }

    private void testLocate2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        // "SearchName2Name"
        // "Name" -> "ame2Name" 5+8-1=12
        // "No" -> "ame2Name" 0+8-1=7
        // SELECT COALESCE(NULLIF(STRPOS(SUBSTRING(NAME FROM ?), PATTERN), 0) - 1 + ?,
        // 0) FROM search_data WHERE (average_value = ?)
        //
        // select (position(searchdata0_.pattern in substring(searchdata0_.name,
        // ?))+?-1) as col_0_0_ from search_data searchdata0_ where
        // searchdata0_.average_value=7
        //
        // select coalesce(nullif(position(searchdata0_.pattern in substring(searchdata0_.name,
        // ?)),0)+?-1,0) as col_0_0_ from search_data searchdata0_ where
        // searchdata0_.average_value=7
        criteriaQuery.select(cb.locate(root.get("name"), "Name", 8));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(12, resultList.get(0));
    }

    private void testLocate3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.locate(root.get("name"), root.get("pattern")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(7, resultList.get(0));
    }

    private void testLocate4(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.locate(root.get("name"), root.get("pattern"), cb.parameter(Integer.class, "from")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("from", 8);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(12, resultList.get(0));
    }

    private void testLocate5(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = cb.createQuery(Integer.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        // "SearchName2Name"
        // "Name" -> "ame2Name" 5+8-1=12
        // "No" -> "ame2Name" 0+8-1=7
        // SELECT COALESCE(NULLIF(STRPOS(SUBSTRING(NAME FROM ?), PATTERN), 0) - 1 + ?,
        // 0) FROM search_data WHERE (average_value = ?)
        // select (position(searchdata0_.pattern in substring(searchdata0_.name,
        // ?))+?-1) as col_0_0_ from search_data searchdata0_ where
        // searchdata0_.average_value=7
        criteriaQuery.select(cb.locate(root.get("name"), "No", 8));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals(0, resultList.get(0));
    }

    @Test
    public void trim() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern(" ## ");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);
        searchData1.setFloatAverageValue(-4.2f);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2Name");
        searchData2.setModel("SpecialS");
        searchData2.setPattern("Na");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(7);
        searchData2.setFloatAverageValue(-5.2f);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);
        searchData3.setFloatAverageValue(-7.8f);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testTrim1(em);
        tx.commit();

        tx.begin();
        testTrim2(em);
        tx.commit();

        tx.begin();
        testTrim3(em);
        tx.commit();

        tx.begin();
        testTrim4(em);
        tx.commit();

        tx.begin();
        testTrim5(em);
        tx.commit();

        tx.begin();
        testTrim6(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testTrim1(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.trim('3', root.get("name")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("SearchName", resultList.get(0));
    }

    private void testTrim2(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.trim(Trimspec.LEADING, 'S', root.get("model")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("pecialS", resultList.get(0));
    }

    private void testTrim3(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.trim(Trimspec.LEADING, cb.parameter(Character.class, "t"), root.get("model")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("t", 'S');
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("pecialS", resultList.get(0));
    }

    private void testTrim4(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 4);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.trim(Trimspec.LEADING, root.get("pattern")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("## ", resultList.get(0));
    }

    private void testTrim5(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 7);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.trim(cb.parameter(Character.class, "t"), root.get("model")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        typedQuery.setParameter("t", 'S');
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("pecial", resultList.get(0));
    }

    private void testTrim6(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteriaQuery = cb.createQuery(String.class);
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 4);
        criteriaQuery.where(predicate);
        criteriaQuery.select(cb.trim(root.get("pattern")));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Assertions.assertEquals("##", resultList.get(0));
    }

    @Test
    public void currentDate() throws Exception {
        final EntityManager em = emf.createEntityManager();
        SearchData searchData1 = new SearchData();
        searchData1.setName("SearchName1");
        searchData1.setModel("Free");
        searchData1.setPattern("##");
        searchData1.setOccurences(3);
        searchData1.setAverageValue(4);
        searchData1.setFloatAverageValue(-4.2f);

        SearchData searchData2 = new SearchData();
        searchData2.setName("SearchName2");
        searchData2.setModel("Special");
        searchData2.setPattern("##");
        searchData2.setOccurences(4);
        searchData2.setAverageValue(4);
        searchData2.setFloatAverageValue(-5.2f);

        SearchData searchData3 = new SearchData();
        searchData3.setName("SearchName3");
        searchData3.setModel("Usual");
        searchData3.setPattern("##%");
        searchData3.setOccurences(24);
        searchData3.setAverageValue(5);
        searchData3.setFloatAverageValue(-7.8f);

        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(searchData1);
        em.persist(searchData2);
        em.persist(searchData3);

        tx.commit();

        tx.begin();
        testCurrentDate(em);
        tx.commit();

        tx.begin();
        em.remove(searchData1);
        em.remove(searchData2);
        em.remove(searchData3);
        tx.commit();

        em.close();
    }

    private void testCurrentDate(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<?> criteriaQuery = cb.createQuery();
        Root<SearchData> root = criteriaQuery.from(SearchData.class);
        Predicate predicate = cb.equal(root.get("averageValue"), 5);
        criteriaQuery.where(predicate);
        criteriaQuery.multiselect(List.of(root.get("name"), cb.currentDate(), cb.currentTime(), cb.currentTimestamp()));
        TypedQuery<?> typedQuery = em.createQuery(criteriaQuery);
        List<?> resultList = typedQuery.getResultList();
        Assertions.assertEquals(1, resultList.size());
        Object[] result = (Object[]) resultList.get(0);
        Assertions.assertEquals("SearchName3", result[0]);
        Assertions.assertTrue(result[1] instanceof java.sql.Date);
        Assertions.assertTrue(result[2] instanceof java.sql.Time);
        Assertions.assertTrue(result[3] instanceof java.sql.Timestamp);
    }

}
