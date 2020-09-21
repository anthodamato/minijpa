package org.tinyjpa.jpa.manytoone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jpa.PersistenceProviderImpl;
import org.tinyjpa.jpa.model.manytoone.Department;
import org.tinyjpa.jpa.model.manytoone.Employee;

/**
 * 
 * @author adamato
 *
 */
public class ManyToOneBidTest {

	@Test
	public void persist() throws Exception {
		EntityManagerFactory emf = new PersistenceProviderImpl()
				.createEntityManagerFactory("/org/tinyjpa/jpa/manytoone/persistence.xml", "manytoone_bid", null);
		final EntityManager em = emf.createEntityManager();
		try {
			final EntityTransaction tx = em.getTransaction();
			tx.begin();

			Department department = new Department();
			department.setName("Research");

			Employee employee = new Employee();
			employee.setName("John Smith");
			employee.setDepartment(department);

			Employee emp = new Employee();
			emp.setName("Margaret White");
			emp.setDepartment(department);

			em.persist(employee);
			em.persist(emp);
			em.persist(department);

			tx.commit();

			Assertions.assertTrue(department.getEmployees().isEmpty());

			em.detach(department);

			Department d = em.find(Department.class, department.getId());
			Assertions.assertTrue(!d.getEmployees().isEmpty());
			Assertions.assertEquals(2, d.getEmployees().size());
			Assertions.assertFalse(d == department);

		} finally {
			em.close();
			emf.close();
		}
	}

}
