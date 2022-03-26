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
package org.minijpa.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRunner {

    private Logger LOG = LoggerFactory.getLogger(ScriptRunner.class);

    public void runDDLStatements(List<String> statements, Connection connection) {
	for (String s : statements) {
	    runDDLStatement(s, connection);
	}
    }

    private void runDDLStatement(String stmt, Connection connection) {
	Statement statement = null;
	LOG.info("Running `" + stmt + "`");
	try {
	    statement = connection.createStatement();
	    statement.execute(stmt);
	    statement.close();
	    connection.commit();
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	} finally {
	    try {
		if (statement != null)
		    statement.close();

		connection.rollback();
	    } catch (SQLException e1) {
		LOG.error(e1.getMessage());
	    }
	}
    }

    public List<String> readStatements(File file) throws IOException {
	LOG.debug("Reading '" + file.getAbsolutePath() + "' file...");
	Reader reader = null;
	BufferedReader bufferedReader = null;
	List<String> statements = new ArrayList<>();
	try {
	    reader = new FileReader(file);
	    bufferedReader = new BufferedReader(reader);
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    int number_of_lines = 0;
	    while ((line = bufferedReader.readLine()) != null) {
		if (line.trim().startsWith("--"))
		    continue;

		if (line.trim().isEmpty())
		    continue;

		if (number_of_lines > 0)
		    sb.append(" ");

		sb.append(line);
		LOG.debug("readStatements: line=" + line);
		++number_of_lines;
		if (line.trim().endsWith(";")) {
		    statements.add(sb.toString().substring(0, sb.length() - 1));
		    sb.setLength(0);
		    number_of_lines = 0;
		}
	    }
	} finally {
	    if (reader != null)
		reader.close();

	    if (bufferedReader != null)
		bufferedReader.close();
	}

	return statements;
    }
}
