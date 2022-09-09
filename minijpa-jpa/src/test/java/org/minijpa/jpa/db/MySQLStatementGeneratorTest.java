/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jpa.db;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.model.MySQLSqlStatementGenerator;
import org.minijpa.jdbc.model.SqlDDLStatement;
import org.minijpa.jdbc.model.SqlStatementGenerator;
import org.minijpa.metadata.PersistenceUnitContext;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class MySQLStatementGeneratorTest {

	private final SqlStatementGenerator sqlStatementGenerator = new MySQLSqlStatementGenerator();
	private final DbConfiguration dbConfiguration = new MySQLConfiguration();

	@Test
	public void ddlCitizens() throws Exception {
		DbConfigurationList.getInstance().setDbConfiguration("citizens", dbConfiguration);
		PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("citizens");
		SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
		List<SqlDDLStatement> sqlStatements = dbConfiguration.getDbJdbc().buildDDLStatements(persistenceUnitContext);
		Assertions.assertEquals(2, sqlStatements.size());
//	List<String> ddlStatements = sqlStatements.stream()
//		.map(d -> dbConfiguration.getSqlStatementGenerator().export(d))
//		.flatMap(List::stream).collect(Collectors.toList());
		List<String> ddlStatements = dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
		Assertions.assertFalse(ddlStatements.isEmpty());
		Assertions.assertEquals(2, ddlStatements.size());
		String ddl = ddlStatements.get(0);
		Assertions.assertEquals(
				"create table citizen (id bigint not null AUTO_INCREMENT, first_name varchar(255), last_name varchar(255), version bigint, primary key (id))",
				ddl);
		ddl = ddlStatements.get(1);
		Assertions.assertEquals(
				"create table Address (id bigint not null AUTO_INCREMENT, name varchar(255), postcode varchar(255), tt boolean not null, primary key (id))",
				ddl);
	}

}
