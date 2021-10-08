/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.JpqlModule;
import org.minijpa.jpa.db.JpqlResult;
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
	public void simpleOrder() throws Exception {
		String persistenceUnitName = "simple_order";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT DISTINCT o FROM SimpleOrder AS o JOIN o.lineItems AS l WHERE l.shipped = FALSE";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			Assertions.assertEquals(
					"select distinct so.id from simple_order AS so"
					+ " INNER JOIN simple_order_line_item AS soli ON so.id = soli.SimpleOrder_id"
					+ " INNER JOIN line_item AS li ON soli.lineItems_id = li.id where li.shipped = FALSE",
					jpqlResult.getSql());
		} catch (ParseException ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			Assertions.fail();
		} catch (Error error) {
			LOG.debug(error.getMessage());
			Throwable t = error.getCause();
			LOG.debug("t=" + t);
			Assertions.fail();
		}

		emf.close();
	}

	@Test
	public void simpleOrderProductType() throws Exception {
		String persistenceUnitName = "simple_order";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT DISTINCT o\n"
				+ "  FROM SimpleOrder o JOIN o.lineItems l JOIN l.product p\n"
				+ "  WHERE p.productType = 'office_supplies'";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			Assertions.assertEquals(
					"select distinct so.id from simple_order AS so"
					+ " INNER JOIN simple_order_line_item AS soli ON so.id = soli.SimpleOrder_id"
					+ " INNER JOIN line_item AS li ON soli.lineItems_id = li.id"
					+ " INNER JOIN simple_product AS sp ON li.product_id = sp.id where sp.productType = 'office_supplies'",
					jpqlResult.getSql());
		} catch (ParseException ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			Assertions.fail();
		} catch (Error error) {
			LOG.debug(error.getMessage());
			Throwable t = error.getCause();
			LOG.debug("t=" + t);
			Assertions.fail();
		}

		emf.close();
	}

	@Test
	public void bookingSaleQuery() throws Exception {
		String persistenceUnitName = "booking_sale";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT bs FROM BookingSale AS bs JOIN bs.booking AS bk WHERE bk.customerId = 1";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			Assertions.assertEquals(
					"select bs.id from booking_sale AS bs INNER JOIN booking AS b"
					+ " ON bs.b_dateof = b.dateof AND bs.b_room_number = b.room_number where b.customer_id = 1",
					jpqlResult.getSql());
		} catch (ParseException ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			Assertions.fail();
		} catch (Error error) {
			LOG.debug(error.getMessage());
			Throwable t = error.getCause();
			LOG.debug("t=" + t);
			Assertions.fail();
		}

		emf.close();
	}
}
