package org.minijpa.jpa.db;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.model.Column;
import org.minijpa.jdbc.model.FromTable;
import org.minijpa.jdbc.model.FromTableImpl;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.DefaultSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlDDLStatement;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.jdbc.model.TableColumn;
import org.minijpa.jdbc.model.Value;
import org.minijpa.jdbc.model.aggregate.AggregateFunctionBasicType;
import org.minijpa.jdbc.model.aggregate.BasicAggregateFunction;
import org.minijpa.jdbc.model.aggregate.Count;
import org.minijpa.jdbc.model.aggregate.GroupBy;
import org.minijpa.jdbc.model.condition.BinaryCondition;
import org.minijpa.jdbc.model.condition.Condition;
import org.minijpa.jdbc.model.condition.ConditionType;
import org.minijpa.jdbc.model.join.FromJoin;
import org.minijpa.jdbc.model.join.FromJoinImpl;
import org.minijpa.metadata.PersistenceUnitContext;

public class SqlStatementGeneratorTest {

    private final SqlStatementGenerator sqlStatementGenerator = new DefaultSqlStatementGenerator(new ApacheDerbyJdbc());

    @Test
    public void simpleCondition() {
	FromTable fromTable = new FromTableImpl("citizen", "c");
	Column idColumn = new Column("id");
	Column nameColumn = new Column("first_name");

	List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
	BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
		.withLeftColumn(new TableColumn(fromTable, nameColumn)).withRightExpression("'Sam'").build();
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

	List<Value> values = Arrays.asList(new BasicAggregateFunction(AggregateFunctionBasicType.SUM, new TableColumn(fromTable, populationColumn), false));
	SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).build();
	Assertions.assertEquals("select sum(r.population) from region AS r", sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void count() {
	FromTable fromTable = new FromTableImpl("product", "p");
	List<Value> values = Arrays.asList(Count.countStar());
	SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).build();
	Assertions.assertEquals("select count(*) from product AS p", sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void groupBy() {
	FromTable fromTable = new FromTableImpl("product", "p");
	Column categoryColumn = new Column("category");

	List<Value> values = Arrays.asList(new TableColumn(fromTable, categoryColumn), Count.countStar());
	GroupBy groupBy = new GroupBy(new TableColumn(fromTable, categoryColumn));
	SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).withGroupBy(groupBy).build();
	Assertions.assertEquals("select p.category, count(*) from product AS p group by p.category",
		sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void innerJoin() {
	Column regionIdColumn = new Column("id");

	Column nameColumn = new Column("name");
	Column regionColumn = new Column("region_id");
	FromTable cityTable = new FromTableImpl("city", "c");
	FromJoin fromJoin = new FromJoinImpl(cityTable, Arrays.asList(regionIdColumn), Arrays.asList(regionColumn));

	FromTable regionTable = new FromTableImpl("region", "r", Arrays.asList(fromJoin));
	Column regionNameColumn = new Column("name");

	List<Value> values = Arrays.asList(new TableColumn(regionTable, regionNameColumn));
	BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
		.withLeftColumn(new TableColumn(cityTable, nameColumn)).withRightExpression("'Nottingham'").build();
	List<Condition> conditions = Arrays.asList(binaryCondition);
	SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(regionTable).withValues(values).withConditions(conditions)
		.build();

	Assertions.assertEquals(
		"select r.name from region AS r INNER JOIN city AS c ON r.id = c.region_id where c.name = 'Nottingham'",
		sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void ddlBookingSale() throws Exception {
	DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "booking_sale");
	SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	List<SqlDDLStatement> sqlStatements = sqlStatementFactory.buildDDLStatements(persistenceUnitEnv.getPersistenceUnitContext());
	Assertions.assertEquals(3, sqlStatements.size());
	List<String> ddlStatements = sqlStatements.stream().map(d -> dbConfiguration.getSqlStatementGenerator().export(d)).collect(Collectors.toList());
	Assertions.assertFalse(ddlStatements.isEmpty());
	String d0 = ddlStatements.get(0);
	Assertions.assertEquals("create table booking (dateof date not null, room_number integer not null, customer_id integer, primary key (dateof, room_number))", d0);
	String d1 = ddlStatements.get(1);
	Assertions.assertEquals("create table booking_sale (id bigint not null, perc integer not null, b_dateof date, b_room_number integer, primary key (id), foreign key (b_dateof, b_room_number) references booking)", d1);
	String d2 = ddlStatements.get(2);
	Assertions.assertEquals("create sequence BOOKING_SALE_PK_SEQ start with 1 increment by 1", d2);
    }

    @Test
    public void ddlCitizens() throws Exception {
	DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "citizens");
	SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	List<SqlDDLStatement> sqlStatements = sqlStatementFactory.buildDDLStatements(persistenceUnitEnv.getPersistenceUnitContext());
	Assertions.assertEquals(3, sqlStatements.size());
	List<String> ddlStatements = sqlStatements.stream().map(d -> dbConfiguration.getSqlStatementGenerator().export(d)).collect(Collectors.toList());
	Assertions.assertFalse(ddlStatements.isEmpty());
	String d0 = ddlStatements.get(0);
	Assertions.assertEquals("create table citizen (id bigint not null, first_name varchar(255), last_name varchar(255), version bigint, primary key (id))", d0);
	String d1 = ddlStatements.get(1);
	Assertions.assertEquals("create table Address (id bigint generated by default as identity, name varchar(255), postcode varchar(255), tt boolean not null, primary key (id))", d1);
	String d2 = ddlStatements.get(2);
	Assertions.assertEquals("create sequence SEQ_GEN_SEQUENCE start with 1 increment by 1", d2);
    }

    @Test
    public void ddlEmbBooking() throws Exception {
	DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "emb_booking");
	SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	List<SqlDDLStatement> sqlStatements = sqlStatementFactory.buildDDLStatements(persistenceUnitEnv.getPersistenceUnitContext());
	Assertions.assertEquals(1, sqlStatements.size());
	List<String> ddlStatements = sqlStatements.stream().map(d -> dbConfiguration.getSqlStatementGenerator().export(d)).collect(Collectors.toList());
	Assertions.assertFalse(ddlStatements.isEmpty());
	String d0 = ddlStatements.get(0);
	Assertions.assertEquals("create table HotelBooking (dateof date not null, room_number integer not null, customer_id integer, price float, primary key (dateof, room_number))", d0);
    }

    @Test
    public void ddlEmbedManyToOne() throws Exception {
	DbConfiguration dbConfiguration = new ApacheDerbyConfiguration();
	DbConfigurationList.getInstance().setDbConfiguration("embed_many_to_one", dbConfiguration);
	PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("embed_many_to_one");

	SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	List<SqlDDLStatement> sqlStatements = sqlStatementFactory.buildDDLStatements(persistenceUnitContext);
	Assertions.assertEquals(2, sqlStatements.size());
	List<String> ddlStatements = sqlStatements.stream().map(d -> dbConfiguration.getSqlStatementGenerator().export(d)).collect(Collectors.toList());
	Assertions.assertFalse(ddlStatements.isEmpty());
	String d0 = ddlStatements.get(0);
	Assertions.assertEquals("create table program_manager (id integer not null, name varchar(255) not null, primary key (id))", d0);
	String d1 = ddlStatements.get(1);
	Assertions.assertEquals("create table job_employee (id integer not null, name varchar(255), jd varchar(255), pm_id integer, primary key (id), foreign key (pm_id) references program_manager)", d1);
    }
}
