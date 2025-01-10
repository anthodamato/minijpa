package org.minijpa.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EntityManagerFactoryTest {

    @Test
    public void entityManagerFactory() throws Exception {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("citizens",
                PersistenceUnitProperties.getProperties());
        Assertions.assertNotNull(entityManagerFactory);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Assertions.assertNotNull(entityManager);
    }
}
