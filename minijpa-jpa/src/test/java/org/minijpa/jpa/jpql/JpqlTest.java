/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.db.DbConfiguration;
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
	private static String testDb;

	@BeforeAll
	private static void beforeAll() {
		testDb = System.getProperty("minijpa.test");
	}

	@Test
	public void simpleOrder() throws Exception {
		String persistenceUnitName = "simple_order";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT DISTINCT o FROM SimpleOrder AS o JOIN o.lineItems AS l WHERE l.shipped = FALSE";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select distinct simple_order0.id from simple_order simple_order0 INNER JOIN simple_order_line_item simple_order_line_item0 ON simple_order0.id = simple_order_line_item0.SimpleOrder_id INNER JOIN line_item line_item0 ON simple_order_line_item0.lineItems_id = line_item0.id where line_item0.shipped = 0",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select distinct simple_order0.id from simple_order AS simple_order0 INNER JOIN simple_order_line_item AS simple_order_line_item0 ON simple_order0.id = simple_order_line_item0.SimpleOrder_id INNER JOIN line_item AS line_item0 ON simple_order_line_item0.lineItems_id = line_item0.id where line_item0.shipped = FALSE",
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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT DISTINCT o\n" + "  FROM SimpleOrder o JOIN o.lineItems l JOIN l.product p\n"
				+ "  WHERE p.productType = 'office_supplies'";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select distinct simple_order0.id from simple_order simple_order0 INNER JOIN simple_order_line_item simple_order_line_item0 ON simple_order0.id = simple_order_line_item0.SimpleOrder_id INNER JOIN line_item line_item0 ON simple_order_line_item0.lineItems_id = line_item0.id INNER JOIN simple_product simple_product0 ON line_item0.product_id = simple_product0.id where simple_product0.productType = 'office_supplies'",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select distinct simple_order0.id from simple_order AS simple_order0 INNER JOIN simple_order_line_item AS simple_order_line_item0 ON simple_order0.id = simple_order_line_item0.SimpleOrder_id INNER JOIN line_item AS line_item0 ON simple_order_line_item0.lineItems_id = line_item0.id INNER JOIN simple_product AS simple_product0 ON line_item0.product_id = simple_product0.id where simple_product0.productType = 'office_supplies'",
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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT bs FROM BookingSale AS bs JOIN bs.booking AS bk WHERE bk.customerId = 1";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select booking_sale0.id, booking_sale0.perc, booking_sale0.b_dateof, booking_sale0.b_room_number from booking_sale booking_sale0 INNER JOIN booking booking0 ON booking_sale0.b_dateof = booking0.dateof AND booking_sale0.b_room_number = booking0.room_number where booking0.customer_id = 1",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select booking_sale0.id, booking_sale0.perc, booking_sale0.b_dateof, booking_sale0.b_room_number from booking_sale AS booking_sale0 INNER JOIN booking AS booking0 ON booking_sale0.b_dateof = booking0.dateof AND booking_sale0.b_room_number = booking0.room_number where booking0.customer_id = 1",
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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "SELECT o.id, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP FROM SimpleOrder o WHERE o.createdAt >= CURRENT_DATE";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select simple_order0.id, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP from simple_order simple_order0 where simple_order0.created_at >= CURRENT_DATE",
						jpqlResult.getSql());
			else if (testDb != null && (testDb.equals("mysql") || testDb.equals("mariadb")))
				Assertions.assertEquals(
						"select simple_order0.id, CURRENT_DATE(), CURRENT_TIME(), CURRENT_TIMESTAMP() from simple_order AS simple_order0 where simple_order0.created_at >= CURRENT_DATE()",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select simple_order0.id, CURRENT_DATE, CURRENT_TIME, CURRENT_TIMESTAMP from simple_order AS simple_order0 where simple_order0.created_at >= CURRENT_DATE",
						jpqlResult.getSql());

		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			ex.printStackTrace();
			Assertions.fail();
		}
//		catch (Error error) {
//			LOG.debug(error.getMessage());
//			Throwable t = error.getCause();
//			LOG.debug("t=" + t);
//			Assertions.fail();
//		}

		emf.close();
	}

	@Test
	public void orderSubquery() throws Exception {
		String persistenceUnitName = "order_many_to_many";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select o from Order o where (select AVG(p.price) from o.products p)>50";
		// select o.id from orders AS o where
		// (select avg(p.price) from product AS p, orders_product AS op where
		// op.orders_id = o.id
		// and p.id = op.products_id) > 50
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select orders0.id, orders0.date_of, orders0.status, orders0.deliveryType, orders0.customer_id from orders orders0 where (select AVG(product0.price) from product product0, orders_product orders_product0 where orders_product0.orders_id = orders0.id and orders_product0.products_id = product0.id) > 50",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select orders0.id, orders0.date_of, orders0.status, orders0.deliveryType, orders0.customer_id from orders AS orders0 where (select AVG(product0.price) from product AS product0, orders_product AS orders_product0 where orders_product0.orders_id = orders0.id and orders_product0.products_id = product0.id) > 50",
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
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select i.category, count(i.count) from ItemSaleStats i group by i.category order by i.category";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select itemsalestats0.category, COUNT(itemsalestats0.count) from ItemSaleStats itemsalestats0 group by itemsalestats0.category order by itemsalestats0.category",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select itemsalestats0.category, COUNT(itemsalestats0.count) from ItemSaleStats AS itemsalestats0 group by itemsalestats0.category order by itemsalestats0.category",
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
	public void betweenHolidays() throws Exception {
		String persistenceUnitName = "holidays";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select hl from Holiday hl where hl.nights between 7 and 10";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select holiday0.id, holiday0.travellers, holiday0.checkIn, holiday0.nights, holiday0.referenceName from Holiday holiday0 where holiday0.nights between 7 and 10",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select holiday0.id, holiday0.travellers, holiday0.checkIn, holiday0.nights, holiday0.referenceName from Holiday AS holiday0 where holiday0.nights between 7 and 10",
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
	public void inRegions() throws Exception {
		String persistenceUnitName = "cities_uni";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select r.population from Region r where r.name is not null and r.name in ('North West','South West') order by r.name";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select region0.population from Region region0 where region0.name is not null and region0.name in ('North West', 'South West') order by region0.name",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select region0.population from Region AS region0 where region0.name is not null and region0.name in ('North West', 'South West') order by region0.name",
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
	public void concatRegions() throws Exception {
		String persistenceUnitName = "cities_uni";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select CONCAT('Region',' ',r.name), r.population from Region r order by r.name";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select 'Region'||' '||region0.name, region0.population from Region region0 order by region0.name",
						jpqlResult.getSql());
			else if (testDb != null && (testDb.equals("mysql") || testDb.equals("mariadb")))
				Assertions.assertEquals(
						"select CONCAT('Region',' ',region0.name), region0.population from Region AS region0 order by region0.name",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select 'Region'||' '||region0.name, region0.population from Region AS region0 order by region0.name",
						jpqlResult.getSql());

		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			ex.printStackTrace();
			Assertions.fail();
		}
//		catch (Error error) {
//			LOG.debug(error.getMessage());
//			Throwable t = error.getCause();
//			LOG.debug("t=" + t);
//			Assertions.fail();
//		}

		emf.close();
	}

	@Test
	public void lengthConcatRegions() throws Exception {
		String persistenceUnitName = "cities_uni";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select r from Region r where LENGTH(CONCAT('Region',' ',r.name)) = (select MAX(LENGTH(CONCAT('Region',' ',r2.name))) from Region r2)";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals(
						"select region0.id, region0.name, region0.population from Region region0 where LENGTH('Region'||' '||region0.name) = (select MAX(LENGTH('Region'||' '||region1.name)) from Region region1)",
						jpqlResult.getSql());
			else if (testDb != null && (testDb.equals("mysql") || testDb.equals("mariadb")))
				Assertions.assertEquals(
						"select region0.id, region0.name, region0.population from Region AS region0 where LENGTH(CONCAT('Region',' ',region0.name)) = (select MAX(LENGTH(CONCAT('Region',' ',region1.name))) from Region AS region1)",
						jpqlResult.getSql());
			else
				Assertions.assertEquals(
						"select region0.id, region0.name, region0.population from Region AS region0 where LENGTH('Region'||' '||region0.name) = (select MAX(LENGTH('Region'||' '||region1.name)) from Region AS region1)",
						jpqlResult.getSql());

		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			Throwable t = ex.getCause();
			LOG.debug("t=" + t);
			ex.printStackTrace();
			Assertions.fail();
		}
//		catch (Error error) {
//			LOG.debug(error.getMessage());
//			Throwable t = error.getCause();
//			LOG.debug("t=" + t);
//			Assertions.fail();
//		}

		emf.close();
	}

	@Test
	public void sumValues() throws Exception {
		String persistenceUnitName = "numeric_set";
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName,
				PersistenceUnitProperties.getProperties());
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);

		PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, persistenceUnitName);
		PersistenceUnitContext persistenceUnitContext = persistenceUnitEnv.getPersistenceUnitContext();
		String query = "select sum(ns.doubleValue)+0.2 from NumericSet ns";
		JpqlModule jpqlModule = new JpqlModule(dbConfiguration, new SqlStatementFactory(), persistenceUnitContext);

		try {
			JpqlResult jpqlResult = jpqlModule.parse(query);
			if (testDb != null && testDb.equals("oracle"))
				Assertions.assertEquals("select SUM(numeric_set0.double_value)+0.2 from numeric_set numeric_set0",
						jpqlResult.getSql());
			else if (testDb != null && (testDb.equals("mysql") || testDb.equals("mariadb")))
				Assertions.assertEquals("select SUM(numeric_set0.double_value)+0.2 from numeric_set AS numeric_set0",
						jpqlResult.getSql());
			else
				Assertions.assertEquals("select SUM(numeric_set0.double_value)+0.2 from numeric_set AS numeric_set0",
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
