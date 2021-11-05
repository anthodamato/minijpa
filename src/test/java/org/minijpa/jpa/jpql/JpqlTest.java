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
					"select bs.id, bs.perc, bs.b_dateof, bs.b_room_number from booking_sale AS bs INNER JOIN booking AS b ON bs.b_dateof = b.dateof AND bs.b_room_number = b.room_number where b.customer_id = 1",
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
	public void simpleOrderDates() throws Exception {
		String persistenceUnitName = "simple_order";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT o.id, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP FROM SimpleOrder o WHERE o.createdAt >= CURRENT_DATE";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			Assertions.assertEquals(
					"select so.id, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP "
					+ "from simple_order AS so where so.created_at >= CURRENT_DATE",
					jpqlResult.getSql());
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			ex.printStackTrace();
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
	public void orderSubquery() throws Exception {
		String persistenceUnitName = "order_many_to_many";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select o from Order o where (select AVG(p.price) from o.products p)>50";
		// select o.id from orders AS o where 
		// (select avg(p.price) from product AS p, orders_product AS op where op.orders_id = o.id
		//  and p.id = op.products_id) > 50
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			Assertions.assertEquals(
					"select o.id, o.date_of, o.status, o.deliveryType, o.customer_id"
					+ " from orders AS o where (select avg(p.price) from product AS p, orders_product AS op where op.orders_id = o.id AND op.products_id = p.id) > 50",
					jpqlResult.getSql());
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			ex.printStackTrace();
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
	public void groupByOrderBy() throws Exception {
		String persistenceUnitName = "item_sale_stats";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName, PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select i.category, count(i.count) from ItemSaleStats i group by i.category order by i.category";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			Assertions.assertEquals(
					"select i.category, count(i.count) from ItemSaleStats AS i group by i.category order by i.category",
					jpqlResult.getSql());
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			ex.printStackTrace();
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
