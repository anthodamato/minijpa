package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.ConditionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlUpdateGeneratorTest {
	private final Logger LOG = LoggerFactory.getLogger(SqlUpdateGeneratorTest.class);

	private final SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();

	@BeforeEach
	void init() {
		sqlStatementGenerator.init();
	}

	@Test
	public void update() {
		FromTable fromTable = new FromTableImpl("product");
		Column categoryColumn = new Column("category");
		TableColumn tableColumn = new TableColumn(fromTable, categoryColumn);
		Column idColumn = new Column("id");
		BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
				.withLeft(new TableColumn(fromTable, idColumn)).withRight("1").build();

		SqlUpdate sqlUpdate = new SqlUpdate(fromTable, Arrays.asList(tableColumn), Optional.of(binaryCondition));
		Assertions.assertEquals("update product set category = ? where id = 1",
				sqlStatementGenerator.export(sqlUpdate));
	}
}
