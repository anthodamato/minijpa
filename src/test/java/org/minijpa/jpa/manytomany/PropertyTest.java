package org.minijpa.jpa.manytomany;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Property;
import org.minijpa.jpa.model.PropertyOwner;
import org.minijpa.jpa.model.PropertyType;

import java.util.Arrays;
import java.util.Collection;
import javax.persistence.PersistenceException;
import org.junit.jupiter.api.Assertions;

/**
 *
 * @author adamato
 *
 */
public class PropertyTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("property_many_to_many_uni", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void properties() throws Exception {
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			PropertyOwner owner1 = new PropertyOwner();
			owner1.setName("Media Ltd");
			em.persist(owner1);

			PropertyOwner owner2 = new PropertyOwner();
			owner2.setName("Simply Ltd");
			em.persist(owner2);

			Property property = new Property();
			property.setAddress("England Rd, London");
			property.setOwners(Arrays.asList(owner1, owner2));
			property.setPropertyType(PropertyType.apartment);
			em.persist(property);

			tx.commit();

			tx.begin();
			em.detach(property);
			Property p = em.find(Property.class, property.getId());
			Collection<PropertyOwner> owners = p.getOwners();
			Assertions.assertNotNull(owners);
			Assertions.assertEquals(2, owners.size());
			tx.commit();
		} finally {
			em.close();
		}
	}

	@Test
	public void optional() {
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			PropertyOwner owner1 = new PropertyOwner();
			owner1.setName("Media Ltd");
			em.persist(owner1);

			PropertyOwner owner2 = new PropertyOwner();
			owner2.setName("Simply Ltd");
			em.persist(owner2);

			Property property = new Property();
			property.setAddress("England Rd, London");
			property.setOwners(Arrays.asList(owner1, owner2));

			Assertions.assertThrows(PersistenceException.class, () -> {
				em.persist(property);
			});

			tx.commit();

		} finally {
			em.close();
		}
	}

}
