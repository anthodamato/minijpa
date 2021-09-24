/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

import java.io.StringReader;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.model.SqlSelect;
import org.minijpa.jdbc.model.SqlStatement;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.db.DbConfigurationList;
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
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("simple_order", PersistenceUnitProperties.getProperties());
	DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("simple_order");

	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "simple_order");
	PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
	String query = "SELECT DISTINCT o FROM SimpleOrder AS o JOIN o.lineItems AS l WHERE l.shipped = FALSE";
	StringReader reader = new StringReader(query);
	JpqlParser parser = new JpqlParser(reader);

	try {
	    ASTQLStatement qlStatement = parser.QL_statement();
	    LOG.debug("qlStatement=" + qlStatement);
	    JpqlParserVisitor visitor = new JpqlParserVisitorImpl(persistenceUnitContext, new SqlStatementFactory());
	    SqlStatement sqlStatement = (SqlStatement) qlStatement.jjtAccept(visitor, null);
	    LOG.debug("sqlStatement=" + sqlStatement);
	    Assertions.assertNotNull(sqlStatement);

	    String sql = dbConfiguration.getSqlStatementGenerator().export(sqlStatement);
	    Assertions.assertEquals(
		    "select distinct so.id from simple_order AS so"
		    + " INNER JOIN simple_order_line_item AS soli ON soli.id = soli.SimpleOrder_id"
		    + " INNER JOIN line_item AS li ON soli.id = li.lineItems_id where li.shipped = FALSE",
		    sql);
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
