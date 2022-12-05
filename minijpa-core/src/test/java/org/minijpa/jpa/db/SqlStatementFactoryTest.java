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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.AbstractAttribute;
import org.minijpa.jpa.model.Address;
import org.minijpa.jpa.model.Department;
import org.minijpa.jpa.model.Employee;
import org.minijpa.jpa.model.Item;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.Store;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.EntityDelegate;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.ApacheDerbySqlStatementGenerator;
import org.minijpa.sql.model.SqlStatementGenerator;
import org.minijpa.sql.model.TableColumn;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.condition.UnaryCondition;

public class SqlStatementFactoryTest {
    private final SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();

    @BeforeEach
    void init() {
        sqlStatementGenerator.init();
    }

    @Test
    public void generateSelectByForeignKey() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("manytoone_bid",
                PersistenceUnitProperties.getProperties());
        emf.createEntityManager();
        Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance().getEntityContext("manytoone_bid");
        if (!optional.isPresent())
            Assertions.fail("Meta entities not found");

        DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("manytoone_bid");
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
        List<QueryParameter> parameters = MetaEntityHelper.convertAVToQP(foreignKeyAttribute, department);
        List<String> columns = parameters.stream().map(p -> p.getColumnName()).collect(Collectors.toList());

        SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectByForeignKey(employeeEntity,
                foreignKeyAttribute, columns, optional.get().getAliasGenerator());
        Optional<List<Condition>> opt = sqlSelectData.getConditions();
        Assertions.assertTrue(opt.isPresent());
        List<Condition> conditions = opt.get();
        Assertions.assertEquals(1, conditions.size());
        Condition condition = conditions.get(0);
        Assertions.assertTrue(condition instanceof BinaryCondition);
        BinaryCondition equalColumnExprCondition = (BinaryCondition) condition;
        Assertions.assertEquals("department_id",
                ((TableColumn) equalColumnExprCondition.getLeft()).getColumn().getName());

        String sql = sqlStatementGenerator.export(sqlSelectData);
        Assertions.assertEquals(
                "select employee0.id, employee0.salary, employee0.name, employee0.department_id from Employee AS employee0 where employee0.department_id = ?",
                sql);

        emf.close();
    }

    @Test
    public void generateSelectByJoinTable() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetomany_uni",
                PersistenceUnitProperties.getProperties());
        final EntityManager em = emf.createEntityManager();

        Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance().getEntityContext("onetomany_uni");
        if (!optional.isPresent())
            Assertions.fail("Meta entities not found");

        DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("onetomany_uni");
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
        Pk pk = storeEntity.getId();

        SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
        MetaAttribute relationshipAttribute = storeEntity.getAttribute("items");
        RelationshipJoinTable relationshipJoinTable = relationshipAttribute.getRelationship().getJoinTable();
        ModelValueArray<AbstractAttribute> modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(pk,
                store.getId(), relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes());
        List<AbstractAttribute> attributes = modelValueArray.getModels();
        List<QueryParameter> parameters = MetaEntityHelper.convertAbstractAVToQP(modelValueArray);
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectByJoinTable(itemEntity, relationshipJoinTable,
                attributes, optional.get().getAliasGenerator());

        Optional<List<Condition>> opt = sqlSelectData.getConditions();
        Assertions.assertTrue(opt.isPresent());

        String sql = sqlStatementGenerator.export(sqlSelectData);
        Assertions.assertEquals(
                "select item0.id, item0.model, item0.name from Item AS item0 INNER JOIN store_items AS store_items0 ON item0.id = store_items0.items_id where store_items0.Store_id = ?",
                sql);

        em.close();
        emf.close();
    }

    @Test
    public void generateSelectStringByJoinTable() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetomany_uni",
                PersistenceUnitProperties.getProperties());
        final EntityManager em = emf.createEntityManager();

        Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance().getEntityContext("onetomany_uni");
        if (!optional.isPresent())
            Assertions.fail("Meta entities not found");

        DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("onetomany_uni");
        Map<String, MetaEntity> map = optional.get().getEntities();

        MetaEntity storeEntity = map.get(Store.class.getName());
        MetaEntity itemEntity = map.get(Item.class.getName());
        Pk pk = storeEntity.getId();

        SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
        MetaAttribute relationshipAttribute = storeEntity.getAttribute("items");
        RelationshipJoinTable relationshipJoinTable = relationshipAttribute.getRelationship().getJoinTable();
        ModelValueArray<AbstractAttribute> modelValueArray = sqlStatementFactory.expandJoinColumnAttributes(pk, 1L,
                relationshipJoinTable.getOwningJoinColumnMapping().getJoinColumnAttributes());
        List<AbstractAttribute> attributes = modelValueArray.getModels();
        List<QueryParameter> parameters = MetaEntityHelper.convertAbstractAVToQP(modelValueArray);
        SqlSelectData sqlSelectData = sqlStatementFactory.generateSelectByJoinTable(itemEntity, relationshipJoinTable,
                attributes, optional.get().getAliasGenerator());

        Optional<List<Condition>> opt = sqlSelectData.getConditions();
        Assertions.assertTrue(opt.isPresent());

        String sql = sqlStatementGenerator.export(sqlSelectData);
        Assertions.assertEquals(
                "select item0.id, item0.model, item0.name from Item AS item0 INNER JOIN store_items AS store_items0 ON item0.id = store_items0.items_id where store_items0.Store_id = ?",
                sql);

        em.close();
        emf.close();
    }

    @Test
    public void generateIsNullSelectByCriteria() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("citizens",
                PersistenceUnitProperties.getProperties());
        final EntityManager em = emf.createEntityManager();

        Optional<PersistenceUnitContext> optional = EntityDelegate.getInstance().getEntityContext("citizens");
        if (!optional.isPresent())
            Assertions.fail("Meta entities not found");

        DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("citizens");
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

        SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
        StatementParameters statementParameters = sqlStatementFactory.select(typedQuery,
                optional.get().getAliasGenerator());
        SqlSelectData sqlSelectData = (SqlSelectData) statementParameters.getSqlStatement();
        Assertions.assertNotNull(sqlSelectData.getValues());
        Optional<List<Condition>> opt = sqlSelectData.getConditions();
        Assertions.assertTrue(opt.isPresent());
        List<Condition> conditions = opt.get();
        Assertions.assertEquals(1, conditions.size());
        Condition condition = conditions.get(0);
        Assertions.assertTrue(condition instanceof UnaryCondition);
        Assertions.assertEquals(ConditionType.IS_NULL, condition.getConditionType());
        UnaryCondition unaryCondition = (UnaryCondition) condition;
        Assertions.assertNotNull(unaryCondition.getOperand());

        String sql = sqlStatementGenerator.export(sqlSelectData);
        Assertions.assertEquals(
                "select address0.id, address0.name, address0.postcode, address0.tt from Address AS address0 where address0.postcode is null",
                sql);

        em.close();
        emf.close();
    }
}
