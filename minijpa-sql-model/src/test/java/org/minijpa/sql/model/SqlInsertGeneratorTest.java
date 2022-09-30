package org.minijpa.sql.model;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlInsertGeneratorTest {
	private final Logger LOG = LoggerFactory.getLogger(SqlInsertGeneratorTest.class);

	private final SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();

	@BeforeEach
	void init() {
		sqlStatementGenerator.init();
	}

	@Test
	public void update() {
		FromTable fromTable = new FromTableImpl("product");
		Column categoryColumn = new Column("category");
		Column idColumn = new Column("id");

		SqlInsert sqlInsert = new SqlInsert(fromTable, Arrays.asList(idColumn, categoryColumn), false, false,
				Optional.empty());
		Assertions.assertEquals("insert into product (id,category) values (?,?)",
				sqlStatementGenerator.export(sqlInsert));
	}
}
