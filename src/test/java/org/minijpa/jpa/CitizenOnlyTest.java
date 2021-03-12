package org.minijpa.jpa;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Citizen;

public class CitizenOnlyTest {
    
    private static EntityManagerFactory emf;
    
    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("citizens_only");
    }
    
    @AfterAll
    public static void afterAll() {
	emf.close();
    }
    
    @Test
    public void persist() throws Exception {
	final EntityManager em = emf.createEntityManager();
	final EntityTransaction tx = em.getTransaction();
	tx.begin();
	
	Citizen citizen = new Citizen();
	citizen.setName("Marc");
	em.persist(citizen);
	
	Assertions.assertNotNull(citizen.getId());
	Citizen c = em.find(Citizen.class, citizen.getId());
	Assertions.assertNotNull(c);
	
	tx.commit();
	
	c = em.find(Citizen.class, citizen.getId());
	Assertions.assertNotNull(c);
	Assertions.assertEquals(citizen.getId(), c.getId());
	
	tx.begin();
	em.remove(c);
	tx.commit();
	
	em.close();
    }
    
    @Test
    public void metamodel() {
	final EntityManager em = emf.createEntityManager();
	Metamodel metamodel = em.getMetamodel();
	Assertions.assertNotNull(metamodel);
	
	Set<EntityType<?>> entityTypes = metamodel.getEntities();
	Assertions.assertEquals(1, entityTypes.size());
	
	for (EntityType<?> entityType : entityTypes) {
	    checkCitizen(entityType);
	}
	
	Set<ManagedType<?>> managedTypes = metamodel.getManagedTypes();
	Assertions.assertNotNull(managedTypes);
	Assertions.assertEquals(1, managedTypes.size());
	
	em.close();
    }
    
    private void checkCitizen(EntityType<?> entityType) {
	Assertions.assertEquals("Citizen", entityType.getName());
	MetamodelUtils.checkType(entityType, Citizen.class, PersistenceType.ENTITY);
	MetamodelUtils.checkType(entityType.getIdType(), Long.class, PersistenceType.BASIC);
	
	Assertions.assertEquals(BindableType.ENTITY_TYPE, entityType.getBindableType());
	Assertions.assertEquals(Citizen.class, entityType.getBindableJavaType());
	
	List<String> names = MetamodelUtils.getAttributeNames(entityType);
	Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("id", "name", "lastName"), names));
	
	MetamodelUtils.checkAttribute(entityType.getAttribute("name"), "name", String.class,
		PersistentAttributeType.BASIC, false, false);
	MetamodelUtils.checkAttribute(entityType.getAttribute("lastName"), "lastName", String.class,
		PersistentAttributeType.BASIC, false, false);
    }
    
}
