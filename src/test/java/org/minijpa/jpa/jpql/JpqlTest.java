/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

import java.io.StringReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.db.PersistenceUnitEnv;
import org.minijpa.jpa.db.SqlStatementFactory;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class JpqlTest {

    private final Logger LOG = LoggerFactory.getLogger(JpqlTest.class);

    @Test
    public void simple() throws Exception {
	PersistenceUnitContext persistenceUnitContext = PersistenceUnitEnv.build("simple_order");

	String query = "SELECT DISTINCT o FROM Order AS o JOIN o.lineItems AS l WHERE l.shipped = FALSE";
	StringReader reader = new StringReader(query);
	JpqlParser parser = new JpqlParser(reader);

	try {
	    ASTQLStatement qlStatement = parser.QL_statement();
	    LOG.debug("qlStatement=" + qlStatement);
	    JpqlParserVisitor visitor = new JpqlParserVisitorImpl(persistenceUnitContext, new SqlStatementFactory());
	    Object result = qlStatement.jjtAccept(visitor, null);
	    LOG.debug("result=" + result);
//			Assertions.assertEquals("Select DISTINCT o FROM order AS o JOIN o.lineItems AS l WHERE l.shipped = FALSE",
//					qlStatement.jjtGetValue());
	} catch (ParseException ex) {
	    LOG.debug(ex.getMessage());
	    Throwable t = ex.getCause();
	    LOG.debug("t=" + t);
	    Assertions.fail();
//	    Logger.getLogger(JpqlTest.class.getName()).log(Level.SEVERE, null, ex);
	} catch (Error error) {
	    LOG.debug(error.getMessage());
	    Throwable t = error.getCause();
	    LOG.debug("t=" + t);
	    Assertions.fail();
	}
    }
}
