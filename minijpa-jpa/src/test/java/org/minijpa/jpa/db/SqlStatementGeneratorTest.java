package org.minijpa.jpa.db;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.model.ApacheDerbySqlStatementGenerator;
import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.FromTableImpl;
import org.minijpa.jdbc.model.SqlDDLStatement;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.function.Count;
import org.minijpa.jdbc.model.function.Sum;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.FromJoinImpl;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlStatementGeneratorTest {
	private final Logger LOG = LoggerFactory.getLogger(SqlStatementGeneratorTest.class);

	private final SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();

	@BeforeEach
	void init() {
		sqlStatementGenerator.init();
	}

	@Test
	public void simpleCondition() {
		FromTable fromTable = new FromTableImpl("citizen", "c");
		Column idColumn = new Column("id");
		Column nameColumn = new Column("first_name");

		List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
		BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
				.withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
		List<Condition> conditions = Arrays.asList(binaryCondition);
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).withConditions(conditions)
				.build();
		Assertions.assertEquals("select c.id from citizen AS c where c.first_name = 'Sam'",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void distinct() {
		FromTable fromTable = new FromTableImpl("citizen", "c");
		Column nameColumn = new Column("first_name");

		List<Value> values = Arrays.asList(new TableColumn(fromTable, nameColumn));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).distinct().build();
		Assertions.assertEquals("select distinct c.first_name from citizen AS c",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void sum() {
		FromTable fromTable = new FromTableImpl("region", "r");
		Column populationColumn = new Column("population");

		List<Value> values = Arrays.asList(new Sum(new TableColumn(fromTable, populationColumn)));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).build();
		Assertions.assertEquals("select SUM(r.population) from region AS r", sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void count() {
		FromTable fromTable = new FromTableImpl("product", "p");
		List<Value> values = Arrays.asList(new Count("*"));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).build();
		LOG.debug("SqlStatementGeneratorTest: count sqlStatementGenerator=" + sqlStatementGenerator);

		Assertions.assertEquals("select COUNT(*) from product AS p", sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void groupBy() {
		FromTable fromTable = new FromTableImpl("product", "p");
		Column categoryColumn = new Column("category");

		List<Value> values = Arrays.asList(new TableColumn(fromTable, categoryColumn), new Count("*"));
		GroupBy groupBy = new GroupBy(new TableColumn(fromTable, categoryColumn));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).withGroupBy(groupBy).build();
		Assertions.assertEquals("select p.category, COUNT(*) from product AS p group by p.category",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void innerJoin() {
		Column regionIdColumn = new Column("id");

		Column nameColumn = new Column("name");
		Column regionColumn = new Column("region_id");
		FromTable cityTable = new FromTableImpl("city", "c");
		FromTable regionTable = new FromTableImpl("region", "r");
		FromJoin fromJoin = new FromJoinImpl(cityTable, regionTable.getAlias().get(), Arrays.asList(regionIdColumn),
				Arrays.asList(regionColumn));

		Column regionNameColumn = new Column("name");

		List<Value> values = Arrays.asList(new TableColumn(regionTable, regionNameColumn));
		BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
				.withLeft(new TableColumn(cityTable, nameColumn)).withRight("'Nottingham'").build();
		List<Condition> conditions = Arrays.asList(binaryCondition);
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(regionTable).withJoin(fromJoin).withValues(values)
				.withConditions(conditions).build();

		Assertions.assertEquals(
				"select r.name from region AS r INNER JOIN city AS c ON r.id = c.region_id where c.name = 'Nottingham'",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void ddlBookingSale() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("booking_sale", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("booking_sale");
		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(3, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String d0 = ddlStatements.get(0);
		Assertions.assertEquals(
				"create table booking (dateof date not null, room_number integer not null, customer_id integer, primary key (dateof, room_number))",
				d0);
		String d1 = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table booking_sale (id bigint not null, perc integer not null, b_dateof date, b_room_number integer, primary key (id), foreign key (b_dateof, b_room_number) references booking)",
				d1);
		String d2 = ddlStatements.get(2);
		Assertions.assertEquals("create sequence BOOKING_SALE_PK_SEQ start with 1 increment by 1", d2);
	}

	@Test
	public void ddlCitizens() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("citizens", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("citizens");
		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(3, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String ddl = ddlStatements.get(0);
		Assertions.assertEquals(
				"create table citizen (id bigint not null, first_name varchar(255), last_name varchar(255), version bigint, primary key (id))",
				ddl);
		ddl = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table Address (id bigint generated by default as identity, name varchar(255), postcode varchar(255), tt boolean not null, primary key (id))",
				ddl);
		ddl = ddlStatements.get(2);
		Assertions.assertEquals("create sequence SEQ_GEN_SEQUENCE start with 1 increment by 1", ddl);
	}

	@Test
	public void ddlEmbBooking() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("emb_booking", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("emb_booking");
		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(1, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String d0 = ddlStatements.get(0);
		Assertions.assertEquals(
				"create table HotelBooking (dateof date not null, room_number integer not null, customer_id integer, price real, primary key (dateof, room_number))",
				d0);
	}

	@Test
	public void ddlEmbedManyToOne() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("embed_many_to_one", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("embed_many_to_one");

		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(2, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String d0 = ddlStatements.get(0);
		Assertions.assertEquals(
				"create table program_manager (id integer not null, name varchar(255) not null, primary key (id))", d0);
		String d1 = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table job_employee (id integer not null, name varchar(255), jd varchar(255), pm_id integer, primary key (id), foreign key (pm_id) references program_manager)",
				d1);
	}

	@Test
	public void ddlOrderManyToMany() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("order_many_to_many", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("order_many_to_many");

//		SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(7, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String ddl = ddlStatements.get(0);
		Assertions.assertEquals("create table customer (id bigint not null, name varchar(255), primary key (id))", ddl);
		ddl = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table product (id bigint not null, name varchar(255), price real not null, primary key (id))",
				ddl);
		ddl = ddlStatements.get(2);
		Assertions.assertEquals(
				"create table orders (id bigint not null, date_of timestamp, status varchar(255), deliveryType integer, customer_id bigint, primary key (id), foreign key (customer_id) references customer)",
				ddl);
		ddl = ddlStatements.get(3);
		Assertions.assertEquals("create sequence CUSTOMER_PK_SEQ start with 1 increment by 1", ddl);
		ddl = ddlStatements.get(4);
		Assertions.assertEquals("create sequence PRODUCT_PK_SEQ start with 1 increment by 1", ddl);
		ddl = ddlStatements.get(5);
		Assertions.assertEquals("create sequence ORDERS_PK_SEQ start with 1 increment by 1", ddl);
		ddl = ddlStatements.get(6);
		Assertions.assertEquals(
				"create table orders_product (orders_id bigint not null, products_id bigint not null, foreign key (orders_id) references orders, foreign key (products_id) references product)",
				ddl);
	}

	@Test
	public void ddlPurchaseStats() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("purchase_stats", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("purchase_stats");

		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(2, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String d0 = ddlStatements.get(0);
		Assertions.assertEquals(
				"create table purchase_stats (id bigint not null, start_date date, end_date date, debit_card double precision, credit_card double precision, cash double precision, primary key (id))",
				d0);
		String d1 = ddlStatements.get(1);
		Assertions.assertEquals("create sequence PURCHASE_STATS_PK_SEQ start with 1 increment by 1", d1);
	}

	@Test
	public void ddlManyToOneBid() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("manytoone_bid", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("manytoone_bid");

		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(4, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String ddl = ddlStatements.get(0);
		Assertions.assertEquals("create table Department (id bigint not null, name varchar(255), primary key (id))",
				ddl);
		ddl = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table Employee (id bigint not null, salary decimal(19,2), name varchar(255), department_id bigint, primary key (id), foreign key (department_id) references Department)",
				ddl);
		ddl = ddlStatements.get(2);
		Assertions.assertEquals("create sequence DEPARTMENT_PK_SEQ start with 1 increment by 1", ddl);
		ddl = ddlStatements.get(3);
		Assertions.assertEquals("create sequence EMPLOYEE_PK_SEQ start with 1 increment by 1", ddl);
	}

	@Test
	public void ddlOtmEmbBooking() throws Exception {
		DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
		DbConfigurationList.getInstance().setDbConfiguration("otm_emb_booking", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("otm_emb_booking");

		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(4, sqlStatements.size());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		String ddl = ddlStatements.get(0);
		Assertions.assertEquals("create table HotelCustomer (id bigint not null, name varchar(255), primary key (id))",
				ddl);
		ddl = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table HotelBookingDetail (dateof date not null, room_number integer not null, price real, primary key (dateof, room_number))",
				ddl);
		ddl = ddlStatements.get(2);
		Assertions.assertEquals("create sequence HOTELCUSTOMER_PK_SEQ start with 1 increment by 1", ddl);
		ddl = ddlStatements.get(3);
		Assertions.assertEquals(
				"create table HotelBookingDetail_HotelCustomer (HotelBookingDetail_dateof date not null, HotelBookingDetail_room_number integer not null, customers_id bigint not null, foreign key (HotelBookingDetail_dateof, HotelBookingDetail_room_number) references HotelBookingDetail, foreign key (customers_id) references HotelCustomer)",
				ddl);
	}
}
