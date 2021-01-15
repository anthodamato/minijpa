package org.tinyjpa.jpa.db;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jdbc.MetaAttribute;
import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.SqlStatementGenerator;
import org.tinyjpa.jdbc.model.condition.BinaryCondition;
import org.tinyjpa.jdbc.model.condition.Condition;
import org.tinyjpa.jdbc.model.condition.ConditionType;
import org.tinyjpa.jdbc.model.condition.UnaryCondition;
import org.tinyjpa.jpa.model.Address;
import org.tinyjpa.jpa.model.Department;
import org.tinyjpa.jpa.model.Employee;
import org.tinyjpa.jpa.model.Item;
import org.tinyjpa.jpa.model.Store;
import org.tinyjpa.metadata.EntityContext;
import org.tinyjpa.metadata.EntityDelegate;

public class SqlStatementFactoryTest {
	private SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();

	@Test
	public void generateSelectByForeignKey() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("manytoone_bid");
		emf.createEntityManager();
		Optional<EntityContext> optional = EntityDelegate.getInstance().getEntityContext("manytoone_bid");
		if (!optional.isPresent())
			Assertions.fail("Meta entities not found");

		Map<String, MetaEntity> map = optional.get().getEntities();

		Department department = new Department();
		department.setName("Research");

		Employee employee = new Employee();
		employee.setName("John Smith");
		employee.setSalary(new BigDecimal(130000));
		employee.setDepartment(department);

		Employee emp = new Employee();
		emp.setName("Margaret White");
		emp.setSalary(new BigDecimal(170000));
		emp.setDepartment(department);

		MetaEntity employeeEntity = map.get(Employee.class.getName());
		SqlSelect sqlSelect = sqlStatementFactory.generateSelectByForeignKey(employeeEntity,
				employeeEntity.getAttribute("department"), department);
		Optional<List<Condition>> opt = sqlSelect.getConditions();
		Assertions.assertTrue(opt.isPresent());
		List<Condition> conditions = opt.get();
		Assertions.assertEquals(1, conditions.size());
		Condition condition = conditions.get(0);
		Assertions.assertTrue(condition instanceof BinaryCondition);
		BinaryCondition equalColumnExprCondition = (BinaryCondition) condition;
		Assertions.assertEquals("department_id", equalColumnExprCondition.getLeftColumn().get().getColumn().getName());

		String sql = new SqlStatementGenerator(new ApacheDerbyJdbc()).export(sqlSelect);
		Assertions.assertEquals(
				"select e.id, e.salary, e.name, e.department_id from Employee AS e where e.department_id = ?", sql);

		emf.close();
	}

	@Test
	public void generateSelectByJoinTable() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetomany_uni");
		final EntityManager em = emf.createEntityManager();

		Optional<EntityContext> optional = EntityDelegate.getInstance().getEntityContext("onetomany_uni");
		if (!optional.isPresent())
			Assertions.fail("Meta entities not found");

		Map<String, MetaEntity> map = optional.get().getEntities();
		Store store = new Store();
		store.setName("Upton Store");

		Item item1 = new Item();
		item1.setName("Notepad");
		item1.setModel("Free Ink");

		Item item2 = new Item();
		item2.setName("Pencil");
		item2.setModel("Staedtler");

		store.setItems(Arrays.asList(item1, item2));

		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		em.persist(item1);
		em.persist(store);
		em.persist(item2);

		tx.commit();

		MetaEntity storeEntity = map.get(Store.class.getName());
		MetaEntity itemEntity = map.get(Item.class.getName());
		MetaAttribute a = storeEntity.getId();
		SqlSelect sqlSelect = sqlStatementFactory.generateSelectByJoinTable(itemEntity, a, store.getId(),
				storeEntity.getAttribute("items").getRelationship().getJoinTable());
		Optional<List<Condition>> opt = sqlSelect.getConditions();
		Assertions.assertTrue(opt.isPresent());

		String sql = new SqlStatementGenerator(new ApacheDerbyJdbc()).export(sqlSelect);
		Assertions.assertEquals(
				"select i.id, i.model, i.name from Item AS i INNER JOIN Store_Item AS si ON i.id = si.items_id where si.Store_id = ?",
				sql);

		em.close();
		emf.close();
	}

	@Test
	public void generateIsNullSelectByCriteria() throws Exception {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("citizens");
		final EntityManager em = emf.createEntityManager();

		Optional<EntityContext> optional = EntityDelegate.getInstance().getEntityContext("citizens");
		if (!optional.isPresent())
			Assertions.fail("Meta entities not found");

		EntityTransaction tx = em.getTransaction();
		tx.begin();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Address> cq = cb.createQuery(Address.class);
		Root<Address> root = cq.from(Address.class);

		// postcode is null
		Predicate isNull = cb.isNull(root.get("postcode"));
		cq.where(isNull);

		cq.select(root);

		TypedQuery<Address> typedQuery = em.createQuery(cq);

		tx.commit();

		SqlSelect sqlSelect = sqlStatementFactory.select(typedQuery);
		Assertions.assertNotNull(sqlSelect.getValues());
		Optional<List<Condition>> opt = sqlSelect.getConditions();
		Assertions.assertTrue(opt.isPresent());
		List<Condition> conditions = opt.get();
		Assertions.assertEquals(1, conditions.size());
		Condition condition = conditions.get(0);
		Assertions.assertTrue(condition instanceof UnaryCondition);
		Assertions.assertEquals(ConditionType.IS_NULL, condition.getConditionType());
		UnaryCondition unaryCondition = (UnaryCondition) condition;
		Assertions.assertNotNull(unaryCondition.getTableColumn());
		Assertions.assertNotNull(unaryCondition.getExpression());

		String sql = new SqlStatementGenerator(new ApacheDerbyJdbc()).export(sqlSelect);
		Assertions.assertEquals("select a.id, a.name, a.postcode, a.tt from Address AS a where a.postcode IS NULL",
				sql);

		em.close();
		emf.close();
	}
}
