/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.io.StringReader;

import org.minijpa.jdbc.model.SqlStatement;
import org.minijpa.jpa.jpql.ASTQLStatement;
import org.minijpa.jpa.jpql.JpqlParser;
import org.minijpa.jpa.jpql.JpqlParserVisitor;
import org.minijpa.jpa.jpql.JpqlParserVisitorImpl;
import org.minijpa.jpa.jpql.ParseException;
import org.minijpa.metadata.PersistenceUnitContext;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlModule {

	private final DbConfiguration dbConfiguration;
	private final SqlStatementFactory sqlStatementFactory;
	private final PersistenceUnitContext persistenceUnitContext;
	private JpqlParser parser;
	private JpqlParserVisitor visitor;

	public JpqlModule(DbConfiguration dbConfiguration,
			SqlStatementFactory sqlStatementFactory,
			PersistenceUnitContext persistenceUnitContext) {
		this.dbConfiguration = dbConfiguration;
		this.sqlStatementFactory = sqlStatementFactory;
		this.persistenceUnitContext = persistenceUnitContext;
	}

	public JpqlResult parse(String jpqlQuery) throws ParseException, Error, IllegalStateException {
		StringReader reader = new StringReader(jpqlQuery);
		if (parser == null) {
			parser = new JpqlParser(reader);
		} else
			parser.ReInit(reader);

		ASTQLStatement qlStatement = parser.QL_statement();
		if (visitor == null) {
			visitor = new JpqlParserVisitorImpl(persistenceUnitContext, sqlStatementFactory, dbConfiguration);
		}

		SqlStatement sqlStatement = (SqlStatement) qlStatement.jjtAccept(visitor, null);
		if (sqlStatement == null)
			throw new IllegalStateException("Jpql Parsing failed: '" + jpqlQuery + "'");

		String sql = dbConfiguration.getSqlStatementGenerator().export(sqlStatement);
		return new JpqlResult(sqlStatement, sql);
	}
}
