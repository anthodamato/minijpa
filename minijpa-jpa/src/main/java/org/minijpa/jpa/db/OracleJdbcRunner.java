/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.minijpa.jdbc.Pk;
import org.minijpa.jdbc.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class OracleJdbcRunner extends JpaJdbcRunner {

	private final Logger LOG = LoggerFactory.getLogger(OracleJdbcRunner.class);

	public OracleJdbcRunner() {
		super();
	}

	@Override
	public Object insertReturnGeneratedKeys(Connection connection, String sql, List<QueryParameter> parameters, Pk pk)
			throws SQLException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			LOG.info("Running `" + sql + "`");
			String[] generatedKeyColumns = {pk.getAttribute().getColumnName()};
			preparedStatement = connection.prepareStatement(sql, generatedKeyColumns);
			setPreparedStatementParameters(preparedStatement, parameters);
			preparedStatement.execute();

			Object id = null;
			resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet.next()) {
				id = resultSet.getObject(1);
			}

			return id;
		} finally {
			if (resultSet != null)
				resultSet.close();

			if (preparedStatement != null)
				preparedStatement.close();
		}
	}

}
