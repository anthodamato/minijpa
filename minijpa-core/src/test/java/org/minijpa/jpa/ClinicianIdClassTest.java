package org.minijpa.jpa;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Clinician;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.time.LocalDate;

public class ClinicianIdClassTest {
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws IOException {
        emf = Persistence.createEntityManagerFactory("idclass", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void persist() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Clinician clinician_1 = new Clinician();
        clinician_1.setName("Robert Coley");
        clinician_1.setDob(LocalDate.of(1990, 1, 1));
        em.persist(clinician_1);

        Clinician clinician_2 = new Clinician();
        clinician_2.setName("Karl Mallory");
        clinician_2.setDob(LocalDate.of(1991, 1, 10));
        em.persist(clinician_2);

        tx.commit();

        tx.begin();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Clinician> root = criteriaQuery.from(Clinician.class);
        criteriaQuery.select(criteriaBuilder.count(root));
        Long count = em.createQuery(criteriaQuery).getSingleResult();
        Assertions.assertEquals(2, count);

        em.remove(clinician_1);
        em.remove(clinician_2);
        tx.commit();

        em.close();
    }
}
