package org.minijpa.jpa.manytoone;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Department;
import org.minijpa.jpa.model.Employee;

/**
 *
 * @author adamato
 *
 */
public class ManyToOneBidTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("manytoone_bid", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void persist() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee = jsEmployee(department);
	    Employee emp = mwEmployee(department);

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

	    tx.begin();
	    em.remove(employee);
	    em.remove(emp);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    private Employee jsEmployee(Department department) {
	Employee employee = new Employee();
	employee.setName("John Smith");
	employee.setSalary(new BigDecimal(130000));
	employee.setDepartment(department);
	return employee;
    }

    private Employee mwEmployee(Department department) {
	Employee emp = new Employee();
	emp.setName("Margaret White");
	emp.setSalary(new BigDecimal(170000));
	emp.setDepartment(department);
	return emp;
    }

    private Employee jbEmployee(Department department) {
	Employee employee3 = new Employee();
	employee3.setName("Joshua Bann");
	employee3.setSalary(new BigDecimal(140000f));
	employee3.setDepartment(department);
	return employee3;
    }

    @Test
    public void max() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee1 = jsEmployee(department);
	    Employee employee2 = mwEmployee(department);
	    Employee employee3 = jbEmployee(department);

	    em.persist(employee1);
	    em.persist(employee2);
	    em.persist(employee3);
	    em.persist(department);

	    tx.commit();

	    Assertions.assertTrue(department.getEmployees().isEmpty());

	    // max
	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery criteriaQuery = cb.createQuery();
	    Root<Employee> root = criteriaQuery.from(Employee.class);
	    criteriaQuery.select(cb.max(root.get("salary")));
	    Query query = em.createQuery(criteriaQuery);
	    BigDecimal s = (BigDecimal) query.getSingleResult();

	    Assertions.assertNotNull(s);
	    BigDecimal salary = new BigDecimal(new BigInteger("17000000"), 2);
	    Assertions.assertEquals(salary, s);

	    // min
	    criteriaQuery.select(cb.min(root.get("salary")));
	    query = em.createQuery(criteriaQuery);
	    s = (BigDecimal) query.getSingleResult();

	    Assertions.assertNotNull(s);
	    Assertions.assertEquals(130000l, s.longValue());

	    tx.begin();
	    em.remove(employee1);
	    em.remove(employee2);
	    em.remove(employee3);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    @Test
    public void multiSelect() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee1 = jsEmployee(department);
	    Employee employee2 = mwEmployee(department);
	    Employee employee3 = jbEmployee(department);

	    em.persist(employee1);
	    em.persist(employee2);
	    em.persist(employee3);
	    em.persist(department);

	    tx.commit();

	    Assertions.assertTrue(department.getEmployees().isEmpty());

	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery criteriaQuery = cb.createQuery();
	    Root<Employee> root = criteriaQuery.from(Employee.class);
	    criteriaQuery.multiselect(root.get("name"), root.get("salary")).orderBy(cb.asc(root.get("name")));
	    Query query = em.createQuery(criteriaQuery);
	    List<Object[]> result = query.getResultList();
	    Assertions.assertEquals(3, result.size());
	    Object[] r1 = result.get(0);
	    Assertions.assertEquals("John Smith", r1[0]);
	    BigDecimal salary = new BigDecimal(new BigInteger("13000000"), 2);
	    Assertions.assertEquals(salary, r1[1]);
	    Object[] r2 = result.get(1);
	    Assertions.assertEquals("Joshua Bann", r2[0]);
	    salary = new BigDecimal(new BigInteger("14000000"), 2);
	    Assertions.assertEquals(salary, r2[1]);
	    Object[] r3 = result.get(2);
	    Assertions.assertEquals("Margaret White", r3[0]);
	    salary = new BigDecimal(new BigInteger("17000000"), 2);
	    Assertions.assertEquals(salary, r3[1]);

	    Assertions.assertTrue(criteriaQuery.getSelection().isCompoundSelection());
	    Assertions.assertEquals(Object[].class, criteriaQuery.getSelection().getJavaType());

	    tx.begin();
	    em.remove(employee1);
	    em.remove(employee2);
	    em.remove(employee3);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    @Test
    public void like() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee1 = jsEmployee(department);
	    Employee employee2 = mwEmployee(department);
	    Employee employee3 = jbEmployee(department);

	    em.persist(employee1);
	    em.persist(employee2);
	    em.persist(employee3);
	    em.persist(department);

	    tx.commit();
	    // like
	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery criteriaQuery = cb.createQuery();
	    Root<Employee> root = criteriaQuery.from(Employee.class);
	    Predicate predicate = cb.like(root.get("name"), "Jo%");
	    criteriaQuery.select(root);
	    criteriaQuery.where(predicate);
	    Query query = em.createQuery(criteriaQuery);
	    List<Employee> list = query.getResultList();

	    Assertions.assertEquals(2, list.size());

	    // not like
	    predicate = cb.notLike(root.get("name"), "Jo%");
	    criteriaQuery.select(root);
	    criteriaQuery.where(predicate);
	    query = em.createQuery(criteriaQuery);
	    list = query.getResultList();

	    Assertions.assertEquals(1, list.size());

	    tx.begin();
	    em.remove(employee1);
	    em.remove(employee2);
	    em.remove(employee3);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    @Test
    public void tuple() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee1 = jsEmployee(department);
	    Employee employee2 = mwEmployee(department);
	    Employee employee3 = jbEmployee(department);

	    em.persist(employee1);
	    em.persist(employee2);
	    em.persist(employee3);
	    em.persist(department);

	    tx.commit();

	    Assertions.assertTrue(department.getEmployees().isEmpty());

	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery<Tuple> criteriaQuery = cb.createTupleQuery();
	    Assertions.assertNotNull(criteriaQuery);
	    Root<Employee> root = criteriaQuery.from(Employee.class);
	    criteriaQuery.multiselect(root.get("name").alias("name"), root.get("salary").alias("salary"))
		    .orderBy(cb.asc(root.get("name")));
	    Query query = em.createQuery(criteriaQuery);
	    List<Tuple> result = query.getResultList();
	    Assertions.assertEquals(3, result.size());
	    Tuple r1 = result.get(0);
	    Assertions.assertEquals("John Smith", r1.get("name"));
	    Assertions.assertEquals("John Smith", r1.get(0));
	    BigDecimal salary = new BigDecimal(new BigInteger("13000000"), 2);
	    Assertions.assertEquals(salary, r1.get("salary"));
	    Assertions.assertEquals(salary, r1.get(1));
	    Tuple r2 = result.get(1);
	    Assertions.assertEquals("Joshua Bann", r2.get("name"));
	    Assertions.assertEquals("Joshua Bann", r2.get(0));
	    salary = new BigDecimal(new BigInteger("14000000"), 2);
	    Assertions.assertEquals(salary, r2.get("salary"));
	    Assertions.assertEquals(salary, r2.get(1));
	    Tuple r3 = result.get(2);
	    Assertions.assertEquals("Margaret White", r3.get("name"));
	    Assertions.assertEquals("Margaret White", r3.get(0));
	    salary = new BigDecimal(new BigInteger("17000000"), 2);
	    Assertions.assertEquals(salary, r3.get("salary"));
	    Assertions.assertEquals(salary, r3.get(1));

	    Assertions.assertTrue(criteriaQuery.getSelection().isCompoundSelection());
	    Assertions.assertEquals(Tuple.class, criteriaQuery.getSelection().getJavaType());

	    tx.begin();
	    em.remove(employee1);
	    em.remove(employee2);
	    em.remove(employee3);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    @Test
    public void criteriaUpdate() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee1 = jsEmployee(department);
	    Employee employee2 = mwEmployee(department);
	    Employee employee3 = jbEmployee(department);

	    em.persist(employee1);
	    em.persist(employee2);
	    em.persist(employee3);
	    em.persist(department);

	    tx.commit();

	    tx.begin();
	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaUpdate<Employee> criteriaUpdate = cb.createCriteriaUpdate(Employee.class);
	    Assertions.assertNotNull(criteriaUpdate);
	    Root<Employee> root = criteriaUpdate.from(Employee.class);
	    criteriaUpdate.set("salary", new BigDecimal(140000));
	    criteriaUpdate.where(cb.equal(root.get("name"), "John Smith"));

	    Query query = em.createQuery(criteriaUpdate);
	    Assertions.assertNotNull(query);
	    int rowCount = query.executeUpdate();
	    Assertions.assertEquals(1, rowCount);
	    em.refresh(employee1);

	    CriteriaQuery<Employee> criteriaQuery = cb.createQuery(Employee.class);
	    criteriaQuery.where(cb.equal(root.get("name"), "John Smith"));
	    root = criteriaQuery.from(Employee.class);
	    query = em.createQuery(criteriaQuery);
	    Employee employee = (Employee) query.getSingleResult();
	    Assertions.assertEquals(new BigDecimal(new BigInteger("14000000"), 2), employee.getSalary());

	    em.remove(employee1);
	    em.remove(employee2);
	    em.remove(employee3);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    @Test
    public void criteriaDelete() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Department department = new Department();
	    department.setName("Research");

	    Employee employee1 = jsEmployee(department);
	    Employee employee2 = mwEmployee(department);
	    Employee employee3 = jbEmployee(department);

	    em.persist(employee1);
	    em.persist(employee2);
	    em.persist(employee3);
	    em.persist(department);

	    tx.commit();

	    tx.begin();
	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaDelete<Employee> criteriaDelete = cb.createCriteriaDelete(Employee.class);
	    Assertions.assertNotNull(criteriaDelete);
	    Root<Employee> root = criteriaDelete.from(Employee.class);
	    criteriaDelete.where(cb.equal(root.get("name"), "John Smith"));

	    Query query = em.createQuery(criteriaDelete);
	    Assertions.assertNotNull(query);
	    int rowCount = query.executeUpdate();
	    Assertions.assertEquals(1, rowCount);
//			em.refresh(employee1);

	    CriteriaQuery<Employee> criteriaQuery = cb.createQuery(Employee.class);
	    criteriaQuery.where(cb.equal(root.get("name"), "John Smith"));
	    root = criteriaQuery.from(Employee.class);
	    query = em.createQuery(criteriaQuery);
	    List<?> result = query.getResultList();
	    Assertions.assertTrue(result.isEmpty());

//			em.remove(employee1);
	    em.remove(employee2);
	    em.remove(employee3);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

}
