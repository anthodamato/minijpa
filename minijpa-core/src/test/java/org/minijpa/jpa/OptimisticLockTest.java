package org.minijpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Citizen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adamato
 */
public class OptimisticLockTest {

    private Logger LOG = LoggerFactory.getLogger(OptimisticLockTest.class);
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("citizens", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void optimisticLock() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Citizen citizen = new Citizen();
        citizen.setName("Anthony");
        citizen.setLastName("Quinn");
        Assertions.assertNull(citizen.getVersion());
        em.persist(citizen);
        tx.commit();

        Assertions.assertNotNull(citizen.getVersion());
        long version0 = citizen.getVersion();
        em.refresh(citizen);

        em.close();

        Long id = citizen.getId();
        Assertions.assertEquals(version0, citizen.getVersion());

        // first transaction
        EntityManager em3 = emf.createEntityManager();
        EntityTransaction tx3 = em3.getTransaction();
        tx3.begin();
        Citizen citizen3 = em3.find(Citizen.class, id);
        Assertions.assertNotSame(citizen, citizen3);
        Assertions.assertEquals(id, citizen3.getId());
        Assertions.assertEquals(version0, citizen3.getVersion());

        // second transaction
        EntityManager em2 = emf.createEntityManager();
        EntityTransaction tx2 = em2.getTransaction();
        tx2.begin();
        Citizen citizen2 = em2.find(Citizen.class, id);
        Assertions.assertEquals(id, citizen2.getId());
        Assertions.assertEquals(version0, citizen2.getVersion());
        Assertions.assertNotSame(citizen3, citizen2);
        citizen2.setName("Oliver");
        em2.flush();
        long nextVersion = version0 + 1;
        Assertions.assertEquals(nextVersion, citizen2.getVersion());
        em2.refresh(citizen2);
        Assertions.assertEquals(nextVersion, citizen2.getVersion());
        tx2.commit();
        em2.close();

        // update on the first transaction
        citizen3.setName("Guy");
        em3.persist(citizen3);
        Assertions.assertThrows(OptimisticLockException.class, em3::flush);

        Assertions.assertEquals(version0, citizen3.getVersion());
        tx3.rollback();

        em = emf.createEntityManager();
        citizen = em.find(Citizen.class, id);
        Assertions.assertEquals("Oliver", citizen.getName());
        Assertions.assertEquals(nextVersion, citizen.getVersion());
        em.close();

        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
        citizen = em.find(Citizen.class, id);
        em.remove(citizen);
        tx.commit();
        em.close();
    }

}
