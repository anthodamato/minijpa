package org.minijpa.metadata;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;

public class EnhancerCitizenTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("citizens");
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void enhance() throws Exception {
	String className = "org.minijpa.jpa.model.Citizen";
	EnhEntity enhEntity = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer().enhance(className);

	Assertions.assertNotNull(enhEntity);
	Assertions.assertNotNull(enhEntity.getClassName());
    }

}
