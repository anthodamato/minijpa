package org.minijpa.jpa;

import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.DataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTypesTest {
    private Logger LOG = LoggerFactory.getLogger(DataTypesTest.class);
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("data_types", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void dataTypes() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        DataTypes dataTypes = new DataTypes();
        dataTypes.setTimeValue(java.sql.Date.valueOf(LocalDate.now()));
        em.persist(dataTypes);
        Assertions.assertThrows(PersistenceException.class, () -> {
            em.flush();
        });

        Assertions.assertNotNull(dataTypes.getId());

        DataTypes d = em.find(DataTypes.class, dataTypes.getId());
        Assertions.assertNotNull(d);
        Assertions.assertEquals(dataTypes.getTimeValue(), d.getTimeValue());

        em.remove(d);
        tx.commit();
        em.close();
    }

}
