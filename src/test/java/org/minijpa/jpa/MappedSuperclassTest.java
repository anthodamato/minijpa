package org.minijpa.jpa;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.metamodel.Attribute;
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
import org.minijpa.jpa.model.Shape;
import org.minijpa.jpa.model.Square;
import org.minijpa.jpa.model.Triangle;

public class MappedSuperclassTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("mapped_superclass");
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

	Triangle triangle = new Triangle();
	triangle.setArea(35);
	em.persist(triangle);

	Assertions.assertNotNull(triangle.getId());

	Square square = new Square();
	square.setArea(40);
	em.persist(square);

	tx.commit();

	em.detach(triangle);
	Triangle t = em.find(Triangle.class, triangle.getId());
	Assertions.assertNotNull(t);
	Assertions.assertEquals(3, triangle.getSides());
	Assertions.assertEquals(35, triangle.getArea());

	em.close();
    }

    @Test
    public void metamodel() {
	final EntityManager em = emf.createEntityManager();
	Metamodel metamodel = em.getMetamodel();
	Assertions.assertNotNull(metamodel);

	Set<EntityType<?>> entityTypes = metamodel.getEntities();
	Assertions.assertEquals(2, entityTypes.size());

	for (EntityType<?> entityType : entityTypes) {
	    if (entityType.getName().equals("Triangle")) {
		checkTriangle(entityType);
	    } else if (entityType.getName().equals("Square")) {
		checkSquare(entityType);
	    }
	}

	Set<ManagedType<?>> managedTypes = metamodel.getManagedTypes();
	Assertions.assertNotNull(managedTypes);
	Assertions.assertEquals(3, managedTypes.size());

	long count = managedTypes.stream().filter(m -> m.getPersistenceType() == PersistenceType.MAPPED_SUPERCLASS)
		.count();
	Assertions.assertEquals(1, count);

	for (ManagedType<?> managedType : managedTypes) {
	    if (managedType.getPersistenceType() == PersistenceType.MAPPED_SUPERCLASS) {
		checkShape(managedType);
	    }
	}

	em.close();
    }

    private void checkShape(ManagedType<?> managedType) {
	MetamodelUtils.checkType(managedType, Shape.class, PersistenceType.MAPPED_SUPERCLASS);

	List<String> names = MetamodelUtils.getAttributeNames(managedType);
	Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("id", "sides", "area"), names));

	Attribute<?, ?> attribute = managedType.getAttribute("area");
	Assertions.assertNotNull(attribute);
	MetamodelUtils.checkAttribute(attribute, "area", Integer.class, PersistentAttributeType.BASIC, false, false);
	MetamodelUtils.checkAttribute(managedType.getAttribute("sides"), "sides", Integer.class,
		PersistentAttributeType.BASIC, false, false);
    }

    private void checkTriangle(EntityType<?> entityType) {
	Assertions.assertEquals("Triangle", entityType.getName());
	MetamodelUtils.checkType(entityType, Triangle.class, PersistenceType.ENTITY);
	MetamodelUtils.checkType(entityType.getIdType(), Long.class, PersistenceType.BASIC);

	Assertions.assertEquals(BindableType.ENTITY_TYPE, entityType.getBindableType());
	Assertions.assertEquals(Triangle.class, entityType.getBindableJavaType());

	List<String> names = MetamodelUtils.getAttributeNames(entityType);
	Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("id", "sides", "area"), names));

	Attribute<?, ?> attribute = entityType.getAttribute("area");
	Assertions.assertNotNull(attribute);
	MetamodelUtils.checkAttribute(attribute, "area", Integer.class, PersistentAttributeType.BASIC, false, false);
	MetamodelUtils.checkAttribute(entityType.getAttribute("sides"), "sides", Integer.class,
		PersistentAttributeType.BASIC, false, false);
    }

    private void checkSquare(EntityType<?> entityType) {
	Assertions.assertEquals("Square", entityType.getName());
	MetamodelUtils.checkType(entityType, Square.class, PersistenceType.ENTITY);
	MetamodelUtils.checkType(entityType.getIdType(), Long.class, PersistenceType.BASIC);

	Assertions.assertEquals(BindableType.ENTITY_TYPE, entityType.getBindableType());
	Assertions.assertEquals(Square.class, entityType.getBindableJavaType());

	List<String> names = MetamodelUtils.getAttributeNames(entityType);
	Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("id", "sides", "area"), names));

	Attribute<?, ?> attribute = entityType.getAttribute("area");
	Assertions.assertNotNull(attribute);
	MetamodelUtils.checkAttribute(attribute, "area", Integer.class, PersistentAttributeType.BASIC, false, false);
	MetamodelUtils.checkAttribute(entityType.getAttribute("sides"), "sides", Integer.class,
		PersistentAttributeType.BASIC, false, false);
    }

}
