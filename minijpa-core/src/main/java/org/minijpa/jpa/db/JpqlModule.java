/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import org.minijpa.jdbc.db.SqlSelectDataBuilder;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.jpql.*;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.SqlSelect;

import javax.persistence.Parameter;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlModule {

    private final DbConfiguration dbConfiguration;
    private final PersistenceUnitContext persistenceUnitContext;
    private JpqlParser parser;
    private JpqlParserVisitor visitor;

    public JpqlModule(DbConfiguration dbConfiguration, PersistenceUnitContext persistenceUnitContext) {
        this.dbConfiguration = dbConfiguration;
        this.persistenceUnitContext = persistenceUnitContext;
    }

    // TODO the 'parameterMap' should be removed
    public StatementParameters parse(
            String jpqlQuery,
            Map<String, Object> hints) throws ParseException, Error, IllegalStateException {
        StringReader reader = new StringReader(jpqlQuery);
        if (parser == null) {
            parser = new JpqlParser(reader);
        } else
            parser.ReInit(reader);

        ASTQLStatement qlStatement = parser.QL_statement();
        if (visitor == null) {
            visitor = new JpqlParserVisitorImpl(persistenceUnitContext, dbConfiguration);
        }

        JpqlParserInputData jpqlParserInputData = new JpqlParserInputData(hints);
        StatementParameters statementParameters = (StatementParameters) qlStatement.jjtAccept(visitor, jpqlParserInputData);
        if (statementParameters == null)
            throw new IllegalStateException("Jpql Parsing failed: '" + jpqlQuery + "'");

        return statementParameters;
    }
}
