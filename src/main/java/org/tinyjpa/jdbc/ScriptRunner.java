package org.tinyjpa.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRunner {
	private Logger LOG = LoggerFactory.getLogger(ScriptRunner.class);

	public void run(File file, Connection connection) {
		LOG.info("run: connection=" + connection);
		List<String> statements = null;
		try {
			statements = readStatements(file);
		} catch (Exception e) {
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
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error(e.getClass().getName());
			try {
				connection.rollback();
				statement.close();
			} catch (Exception e1) {
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
