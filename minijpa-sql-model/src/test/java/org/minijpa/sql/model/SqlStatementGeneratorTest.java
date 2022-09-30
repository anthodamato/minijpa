package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.function.Count;
import org.minijpa.sql.model.function.Sum;
import org.minijpa.sql.model.join.FromJoin;
import org.minijpa.sql.model.join.FromJoinImpl;
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
		SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
		SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
				.build();
		Assertions.assertEquals("select c.id from citizen AS c where c.first_name = 'Sam'",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void distinct() {
		FromTable fromTable = new FromTableImpl("citizen", "c");
		Column nameColumn = new Column("first_name");

		List<Value> values = Arrays.asList(new TableColumn(fromTable, nameColumn));
		SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
		SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).distinct().build();
		Assertions.assertEquals("select distinct c.first_name from citizen AS c",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void sum() {
		FromTable fromTable = new FromTableImpl("region", "r");
		Column populationColumn = new Column("population");

		List<Value> values = Arrays.asList(new Sum(new TableColumn(fromTable, populationColumn)));
		SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
		SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
		Assertions.assertEquals("select SUM(r.population) from region AS r", sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void count() {
		FromTable fromTable = new FromTableImpl("product", "p");
		List<Value> values = Arrays.asList(new Count("*"));
		SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
		SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
		LOG.debug("SqlStatementGeneratorTest: count sqlStatementGenerator=" + sqlStatementGenerator);

		Assertions.assertEquals("select COUNT(*) from product AS p", sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void groupBy() {
		FromTable fromTable = new FromTableImpl("product", "p");
		Column categoryColumn = new Column("category");

		List<Value> values = Arrays.asList(new TableColumn(fromTable, categoryColumn), new Count("*"));
		GroupBy groupBy = new GroupBy(new TableColumn(fromTable, categoryColumn));
		SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
		SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withGroupBy(groupBy).build();
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
		SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
		SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(regionTable).withJoin(fromJoin).withValues(values)
				.withConditions(conditions).build();

		Assertions.assertEquals(
				"select r.name from region AS r INNER JOIN city AS c ON r.id = c.region_id where c.name = 'Nottingham'",
				sqlStatementGenerator.export(sqlSelect));
	}

}
