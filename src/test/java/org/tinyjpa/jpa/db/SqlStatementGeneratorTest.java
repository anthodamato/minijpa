package org.tinyjpa.jpa.db;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.jdbc.model.Column;
import org.tinyjpa.jdbc.model.FromTable;
import org.tinyjpa.jdbc.model.FromTableImpl;
import org.tinyjpa.jdbc.model.SqlSelect;
import org.tinyjpa.jdbc.model.TableColumn;
import org.tinyjpa.jdbc.model.Value;
import org.tinyjpa.jdbc.model.aggregate.Count;
import org.tinyjpa.jdbc.model.aggregate.Distinct;
import org.tinyjpa.jdbc.model.aggregate.GroupBy;
import org.tinyjpa.jdbc.model.aggregate.Sum;
import org.tinyjpa.jdbc.model.condition.Condition;
import org.tinyjpa.jdbc.model.condition.EqualColumnExprCondition;
import org.tinyjpa.jdbc.model.join.FromJoin;
import org.tinyjpa.jdbc.model.join.FromJoinImpl;

public class SqlStatementGeneratorTest {
	private SqlStatementGenerator sqlStatementGenerator = new SqlStatementGenerator(new ApacheDerbyJdbc());

	@Test
	public void simpleCondition() {
		FromTable fromTable = new FromTableImpl("citizen", "c");
		Column idColumn = new Column("id");
		Column nameColumn = new Column("first_name");

		List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
		List<Condition> conditions = Arrays
				.asList(new EqualColumnExprCondition(new TableColumn(fromTable, nameColumn), "'Sam'"));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).withConditions(conditions)
				.build();
		Assertions.assertEquals("select c.id from citizen AS c where c.first_name='Sam'",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void distinct() {
		FromTable fromTable = new FromTableImpl("citizen", "c");
		Column nameColumn = new Column("first_name");

		List<Value> values = Arrays.asList(new Distinct(new TableColumn(fromTable, nameColumn)));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(fromTable).withValues(values).build();
		Assertions.assertEquals("select distinct c.first_name from citizen AS c",
				sqlStatementGenerator.export(sqlSelect));
	}

	@Test
	public void sum() {
		FromTable fromTable = new FromTableImpl("region", "r");
		Column populationColumn = new Column("population");

		List<Value> values = Arrays.asList(new Sum(new TableColumn(fromTable, populationColumn)));
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
		List<Condition> conditions = Arrays
				.asList(new EqualColumnExprCondition(new TableColumn(cityTable, nameColumn), "'Nottingham'"));
		SqlSelect sqlSelect = new SqlSelect.SqlSelectBuilder(regionTable).withValues(values).withConditions(conditions)
				.build();

		Assertions.assertEquals(
				"select r.name from region AS r INNER JOIN city AS c ON r.id=c.region_id where c.name='Nottingham'",
				sqlStatementGenerator.export(sqlSelect));
	}

}
