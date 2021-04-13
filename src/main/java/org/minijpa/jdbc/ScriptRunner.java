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

    public void run(File file, Connection connection) {
	List<String> statements = null;
	try {
	    statements = readStatements(file);
	} catch (IOException e) {
	    LOG.error(e.getMessage());
	    return;
	}

	for (String s : statements) {
	    LOG.info("run: s=" + s);
	}

	Statement statement = null;
	try {
	    statement = connection.createStatement();
	    for (String st : statements) {
		statement.addBatch(st);
	    }

	    statement.executeBatch();
	    connection.commit();
	    statement.close();
	} catch (SQLException e) {
	    LOG.error(e.getMessage());
	    LOG.error(e.getClass().getName());
	    try {
		connection.rollback();
		if (statement != null)
		    statement.close();
	    } catch (SQLException e1) {
		LOG.error(e1.getMessage());
	    }
	}
    }

    private List<String> readStatements(File file) throws IOException {
	LOG.info("Reading '" + file.getAbsolutePath() + "' file...");
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
		LOG.info("readStatements: line=" + line);
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
