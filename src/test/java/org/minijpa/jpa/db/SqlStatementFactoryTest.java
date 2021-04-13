package org.minijpa.jpa.db;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.minijpa.jdbc.AbstractAttribute;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.MetaEntityHelper;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.StatementParameters;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.condition.UnaryCondition;
import org.minijpa.jdbc.relationship.RelationshipJoinTable;
import org.minijpa.jpa.model.Address;
import org.minijpa.jpa.model.Department;
import org.minijpa.jpa.model.Employee;
import org.minijpa.jpa.model.Item;
import org.minijpa.jpa.model.Store;
import org.minijpa.metadata.EntityContext;
import org.minijpa.metadata.EntityDelegate;

public class SqlStatementFactoryTest {

    private final SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
    private final MetaEntityHelper metaEntityHelper = new MetaEntityHelper();

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
	MetaAttribute foreignKeyAttribute = employeeEntity.getAttribute("department");
	List<QueryParameter> parameters = metaEntityHelper.convertAVToQP(foreignKeyAttribute, department);
	List<String> columns = parameters.stream().map(p -> p.getColumnName())
		.collect(Collectors.toList());

	SqlSelect sqlSelect = sqlStatementFactory.generateSelectByForeignKey(employeeEntity,
		foreignKeyAttribute, columns);
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

	MetaAttribute relationshipAttribute = storeEntity.getAttribute("items");
	RelationshipJoinTable relationshipJoinTable = relationshipAttribute.getRelationship().getJoinTable();
	ModelValueArray<AbstractAttribute> attributeValueArray = sqlStatementFactory.expandJoinColumnAttributes(a, store.getId(),
		relationshipJoinTable.getJoinColumnOwningAttributes());
	List<AbstractAttribute> attributes = attributeValueArray.getModels();
	List<QueryParameter> parameters = metaEntityHelper.convertAbstractAVToQP(attributeValueArray);
	SqlSelect sqlSelect = sqlStatementFactory.generateSelectByJoinTable(itemEntity,
		relationshipJoinTable, attributes);

//	SqlSelect sqlSelect = sqlStatementFactory.generateSelectByJoinTable(itemEntity, a, store.getId(),
//		relationshipJoinTable);
	Optional<List<Condition>> opt = sqlSelect.getConditions();
	Assertions.assertTrue(opt.isPresent());

	String sql = new SqlStatementGenerator(new ApacheDerbyJdbc()).export(sqlSelect);
	Assertions.assertEquals(
		"select i.id, i.model, i.name from Item AS i INNER JOIN store_items AS si ON i.id = si.items_id where si.Store_id = ?",
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

	StatementParameters statementParameters = sqlStatementFactory.select(typedQuery);
	SqlSelect sqlSelect = (SqlSelect) statementParameters.getSqlStatement();
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
