package org.minijpa.jpa;

import java.util.Arrays;
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

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Address;
import org.minijpa.jpa.model.Citizen;

/**
 * 
 * @author adamato
 *
 */
public class FindTest {
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
	public void find() throws Exception {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		Citizen citizen = new Citizen();
		citizen.setName("Anthony");
		em.persist(citizen);

		Assertions.assertNotNull(citizen.getId());

		// TODO after the 'detach' the 'find' must return null as it didn't call
		// 'commit' so
		// the instance is not persistent
//		em.detach(citizen);
		Citizen c = em.find(Citizen.class, citizen.getId());
		Assertions.assertNotNull(c);
		Assertions.assertTrue(citizen == c);
		Assertions.assertEquals("Anthony", c.getName());

		em.remove(c);
		tx.commit();
		em.close();
	}

	private Citizen createCitizenAnthonySmith() {
		Citizen citizen = new Citizen();
		citizen.setName("Anthony");
		citizen.setLastName("Smith");
		return citizen;
	}

	private Citizen createCitizenLucySmith() {
		Citizen citizen = new Citizen();
		citizen.setName("Lucy");
		citizen.setLastName("Smith");
		return citizen;
	}

	private Citizen createCitizenCrown() {
		Citizen citizen = new Citizen();
		citizen.setName("Bill");
		citizen.setLastName("Crown");
		return citizen;
	}

	private Citizen createCitizenWolf() {
		Citizen citizen = new Citizen();
		citizen.setName("David");
		citizen.setLastName("Wolf");
		return citizen;
	}

	@Test
	public void criteria() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();
		Citizen citizen = createCitizenAnthonySmith();
		em.persist(citizen);
		Citizen c_Smith = em.find(Citizen.class, citizen.getId());

		citizen = createCitizenCrown();
		em.persist(citizen);
		Citizen c_Crown = em.find(Citizen.class, citizen.getId());

		Assertions.assertNotNull(citizen.getId());

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
		Root<Citizen> root = cq.from(Citizen.class);
		CriteriaQuery<Citizen> cqCitizen = cq.select(root);

		TypedQuery<Citizen> typedQuery = em.createQuery(cqCitizen);
		List<Citizen> citizens = typedQuery.getResultList();

		Assertions.assertEquals(2, citizens.size());

		// check the references
		int counter = 0;
		for (Citizen ct : citizens) {
			if (ct.getId() == c_Crown.getId()) {
				++counter;
				Assertions.assertTrue(ct == c_Crown);
			}

			if (ct.getId() == c_Smith.getId()) {
				++counter;
				Assertions.assertTrue(ct == c_Smith);
			}
		}

		Assertions.assertEquals(2, counter);

		em.remove(c_Crown);
		em.remove(c_Smith);
		tx.commit();
		em.close();
	}

	@Test
	public void criteriaEquals() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Citizen citizen = createCitizenAnthonySmith();
			em.persist(citizen);
			Citizen c_Smith = em.find(Citizen.class, citizen.getId());

			citizen = createCitizenCrown();
			em.persist(citizen);
			Citizen c_Crown = em.find(Citizen.class, citizen.getId());

			Assertions.assertNotNull(citizen.getId());

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
			Root<Citizen> root = cq.from(Citizen.class);

			Predicate predicate = cb.equal(root.get("lastName"), "Smith");
			Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
			Assertions.assertEquals(Boolean.class, predicate.getJavaType());
			Assertions.assertFalse(predicate.isCompoundSelection());

			cq.where(predicate);
			CriteriaQuery<Citizen> cqCitizen = cq.select(root);

			TypedQuery<Citizen> typedQuery = em.createQuery(cqCitizen);
			List<Citizen> citizens = typedQuery.getResultList();

			Assertions.assertEquals(1, citizens.size());

			// check the references
			int counter = 0;
			for (Citizen ct : citizens) {
				if (ct.getId() == c_Smith.getId()) {
					++counter;
					Assertions.assertTrue(ct == c_Smith);
				}
			}

			Assertions.assertEquals(1, counter);

			em.remove(c_Crown);
			em.remove(c_Smith);
		} catch (Exception e) {
			tx.rollback();
			Assertions.fail(e.getMessage());
		} finally {
			tx.commit();
			em.close();
		}
	}

	@Test
	public void equalCriteriaParameter() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Citizen citizen = createCitizenAnthonySmith();
			em.persist(citizen);
			Citizen c_Smith = em.find(Citizen.class, citizen.getId());

			citizen = createCitizenCrown();
			em.persist(citizen);
			Citizen c_Crown = em.find(Citizen.class, citizen.getId());

			Assertions.assertNotNull(citizen.getId());

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
			Root<Citizen> root = cq.from(Citizen.class);

			Predicate predicate = cb.equal(root.get("lastName"), cb.parameter(String.class, "last_name"));
			Assertions.assertEquals(BooleanOperator.AND, predicate.getOperator());
			Assertions.assertEquals(Boolean.class, predicate.getJavaType());
			Assertions.assertFalse(predicate.isCompoundSelection());

			cq.where(predicate);

			Query query = em.createQuery(cq);
			String lastName = "Smith";
			query.setParameter("last_name", lastName);
			Citizen c = (Citizen) query.getSingleResult();

			Assertions.assertNotNull(c);

			em.remove(c_Crown);
			em.remove(c_Smith);
		} finally {
			tx.commit();
			em.close();
		}
	}

	@Test
	public void orCriteria() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Citizen citizen = createCitizenAnthonySmith();
			em.persist(citizen);
			Citizen c_AnthonySmith = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_AnthonySmith);

			citizen = createCitizenLucySmith();
			em.persist(citizen);
			Citizen c_LucySmith = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_LucySmith);

			citizen = createCitizenCrown();
			em.persist(citizen);
			Citizen c_Crown = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_Crown);

			citizen = createCitizenWolf();
			em.persist(citizen);
			Citizen c_Wolf = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_Wolf);

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
			Root<Citizen> root = cq.from(Citizen.class);

			// lastname=='Smith' or 'lastName'=='Wolf'
			Predicate lastNameSmith = cb.equal(root.get("lastName"), "Smith");
			Assertions.assertEquals(BooleanOperator.AND, lastNameSmith.getOperator());
			Assertions.assertEquals(Boolean.class, lastNameSmith.getJavaType());
			Assertions.assertFalse(lastNameSmith.isCompoundSelection());
			Assertions.assertTrue(lastNameSmith.getExpressions().isEmpty());

			Predicate lastNameWolf = cb.equal(root.get("lastName"), "Wolf");

			Predicate orLastName = cb.or(lastNameSmith, lastNameWolf);
			Assertions.assertFalse(orLastName.isCompoundSelection());
			Assertions.assertEquals(BooleanOperator.OR, orLastName.getOperator());
			Assertions.assertFalse(orLastName.getExpressions().isEmpty());
			Assertions.assertEquals(2, orLastName.getExpressions().size());

			cq.where(orLastName);
			CriteriaQuery<Citizen> cqCitizen = cq.select(root);

			TypedQuery<Citizen> typedQuery = em.createQuery(cqCitizen);
			List<Citizen> citizens = typedQuery.getResultList();

			Assertions.assertEquals(3, citizens.size());

			// two 'or', one 'and'
			Predicate nameDavid = cb.equal(root.get("name"), "David");
			Predicate nameAnthony = cb.equal(root.get("name"), "Anthony");
			Predicate orName = cb.or(nameDavid, nameAnthony);

			Predicate and = cb.and(orLastName, orName);

			cq.where(and);

			cqCitizen = cq.select(root);
			typedQuery = em.createQuery(cqCitizen);
			citizens = typedQuery.getResultList();

			Assertions.assertEquals(2, citizens.size());

			em.remove(c_Crown);
			em.remove(c_AnthonySmith);
			em.remove(c_LucySmith);
			em.remove(c_Wolf);
		} catch (Exception e) {
			tx.rollback();
			Assertions.fail(e.getMessage());
		} finally {
			tx.commit();
			em.close();
		}
	}

	@Test
	public void notCriteria() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Citizen citizen = createCitizenAnthonySmith();
			em.persist(citizen);
			Citizen c_AnthonySmith = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_AnthonySmith);

			citizen = createCitizenLucySmith();
			em.persist(citizen);
			Citizen c_LucySmith = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_LucySmith);

			citizen = createCitizenCrown();
			em.persist(citizen);
			Citizen c_Crown = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_Crown);

			citizen = createCitizenWolf();
			em.persist(citizen);
			Citizen c_Wolf = em.find(Citizen.class, citizen.getId());
			Assertions.assertNotNull(citizen.getId());
			Assertions.assertTrue(citizen == c_Wolf);

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Citizen> cq = cb.createQuery(Citizen.class);
			Root<Citizen> root = cq.from(Citizen.class);

			// not lastname=='Smith'
			Predicate lastNameSmith = cb.equal(root.get("lastName"), "Smith");
			Predicate not = cb.not(lastNameSmith);
			cq.where(not);
			Assertions.assertTrue(not.isNegated());

			CriteriaQuery<Citizen> cqCitizen = cq.select(root);

			TypedQuery<Citizen> typedQuery = em.createQuery(cqCitizen);
			List<Citizen> citizens = typedQuery.getResultList();

			Assertions.assertEquals(2, citizens.size());
			CollectionUtils.containsAll(citizens, Arrays.asList(c_Wolf, c_Crown));

			em.remove(c_Crown);
			em.remove(c_AnthonySmith);
			em.remove(c_LucySmith);
			em.remove(c_Wolf);
		} catch (Exception e) {
			tx.rollback();
			Assertions.fail(e.getMessage());
		} finally {
			tx.commit();
			em.close();
		}
	}

	private Address createRegentStAddress() {
		Address address = new Address();
		address.setName("Regent St");
		address.setPostcode("W1B4EA");
		return address;
	}

	private Address createRomfordRdAddress() {
		Address address = new Address();
		address.setName("Romford");
		return address;
	}

	@Test
	public void isNullCriteria() {
		final EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			Address address = createRegentStAddress();
			em.persist(address);
			Address a_RegentSt = em.find(Address.class, address.getId());
			Assertions.assertTrue(address == a_RegentSt);

			address = createRomfordRdAddress();
			em.persist(address);
			Address a_RomfordRd = em.find(Address.class, address.getId());
			Assertions.assertTrue(address == a_RomfordRd);

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Address> cq = cb.createQuery(Address.class);
			Root<Address> root = cq.from(Address.class);

			// postcode is null
			Predicate isNull = cb.isNull(root.get("postcode"));
			cq.where(isNull);

			cq.select(root);

			TypedQuery<Address> typedQuery = em.createQuery(cq);
			List<Address> citizens = typedQuery.getResultList();

			Assertions.assertEquals(1, citizens.size());
			CollectionUtils.containsAll(citizens, Arrays.asList(a_RegentSt));

			// postcode is not null
			Predicate isNotNull = cb.isNull(root.get("postcode"));
			cq.where(isNotNull);

			cq.select(root);

			typedQuery = em.createQuery(cq);
			citizens = typedQuery.getResultList();

			Assertions.assertEquals(1, citizens.size());
			CollectionUtils.containsAll(citizens, Arrays.asList(a_RomfordRd));

			em.remove(a_RegentSt);
			em.remove(a_RomfordRd);
		} catch (Exception e) {
			tx.rollback();
			Assertions.fail(e.getMessage());
		} finally {
			tx.commit();
			em.close();
		}
	}

}
