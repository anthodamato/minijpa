package org.minijpa.sql.model;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.ConditionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDeleteGeneratorTest {
	private final Logger LOG = LoggerFactory.getLogger(SqlDeleteGeneratorTest.class);

	private final SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();

	@BeforeEach
	void init() {
		sqlStatementGenerator.init();
	}

	@Test
	public void update() {
		FromTable fromTable = new FromTableImpl("product");
		Column idColumn = new Column("id");
		BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
				.withLeft(new TableColumn(fromTable, idColumn)).withRight("1").build();

		SqlDelete sqlDelete = new SqlDelete(fromTable, Optional.of(binaryCondition));
		Assertions.assertEquals("delete from product where id = 1", sqlStatementGenerator.export(sqlDelete));
	}
}
