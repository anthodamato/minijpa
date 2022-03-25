package org.minijpa.jpa.manytomany;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Customer;
import org.minijpa.jpa.model.DeliveryType;
import org.minijpa.jpa.model.Order;
import org.minijpa.jpa.model.OrderStatus;
import org.minijpa.jpa.model.Product;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.Query;
import org.junit.jupiter.api.Assertions;

/**
 *
 * @author adamato
 *
 */
public class OrderTest {

	private static EntityManagerFactory emf;

	@BeforeAll
	public static void beforeAll() {
		emf = Persistence.createEntityManagerFactory("order_many_to_many", PersistenceUnitProperties.getProperties());
	}

	@AfterAll
	public static void afterAll() {
		emf.close();
	}

	@Test
	public void orders() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		Customer customer = new Customer();
		customer.setName("Solar Ltd");
		em.persist(customer);

		Product product1 = new Product();
		product1.setName("Medium Panel");
		product1.setPrice(50.3f);
		em.persist(product1);

		Product product2 = new Product();
		product2.setName("Large Panel");
		product2.setPrice(60.0f);
		em.persist(product2);

		Order order1 = new Order();
		order1.setCustomer(customer);
		order1.setProducts(Arrays.asList(product1, product2));
		order1.setDeliveryType(DeliveryType.STANDARD);
		em.persist(order1);

		Product product3 = new Product();
		product3.setName("Small Panel");
		product3.setPrice(60.6f);
		em.persist(product3);

		Order order2 = new Order();
		order2.setCustomer(customer);
		order2.setProducts(Arrays.asList(product1, product3));
		order2.setDeliveryType(DeliveryType.EXPRESS);
		em.persist(order2);

		Set<Order> orders = product1.getOrders();
		Assertions.assertNull(orders);
		tx.commit();

		em.detach(product1);
		Product p = em.find(Product.class, product1.getId());
		orders = p.getOrders();
		Assertions.assertNotNull(orders);
		Assertions.assertEquals(2, orders.size());

		em.detach(order1);
		tx.begin();
		Order o = em.find(Order.class, order1.getId());
		tx.commit();
		Assertions.assertNotNull(o.getStatus());
		Assertions.assertEquals(OrderStatus.NEW, o.getStatus());

		tx.begin();
		o.setStatus(OrderStatus.APPROVED);
		em.persist(o);
		tx.commit();

		em.detach(o);
		o = em.find(Order.class, order1.getId());
		Assertions.assertNotNull(o.getStatus());
		Assertions.assertEquals(OrderStatus.APPROVED, o.getStatus());

		tx.begin();
		em.remove(o);
		em.remove(order2);
		em.remove(customer);
		em.remove(product1);
		em.remove(product2);
		em.remove(product3);
		tx.commit();

		em.close();
	}

	@Test
	public void subquery() throws Exception {
		final EntityManager em = emf.createEntityManager();
		final EntityTransaction tx = em.getTransaction();
		tx.begin();

		Customer customer = new Customer();
		customer.setName("Solar Ltd");
		em.persist(customer);

		Product product1 = new Product();
		product1.setName("Medium Panel");
		product1.setPrice(50.3f);
		em.persist(product1);

		Product product2 = new Product();
		product2.setName("Large Panel");
		product2.setPrice(60.0f);
		em.persist(product2);

		Order order1 = new Order();
		order1.setCustomer(customer);
		order1.setProducts(Arrays.asList(product1, product2));
		order1.setDeliveryType(DeliveryType.STANDARD);
		em.persist(order1);

		Product product3 = new Product();
		product3.setName("Small Panel");
		product3.setPrice(60.6f);
		em.persist(product3);

		Product product4 = new Product();
		product4.setName("Cheap Panel");
		product4.setPrice(20.0f);
		em.persist(product4);

		Order order2 = new Order();
		order2.setCustomer(customer);
		order2.setProducts(Arrays.asList(product3, product4));
		order2.setDeliveryType(DeliveryType.EXPRESS);
		em.persist(order2);

		Set<Order> orders = product1.getOrders();
		Assertions.assertNull(orders);
		tx.commit();

		tx.begin();
		Query query = em.createQuery("select o from Order o where (select AVG(p.price) from o.products p)>50");
		List<Order> list = query.getResultList();
		Assertions.assertNotNull(list);
		Assertions.assertEquals(1, list.size());
		Order o = list.get(0);
		Optional<Product> optional = o.getProducts().stream().filter(p -> p.getName().equals("Medium Panel"))
				.findFirst();
		Assertions.assertTrue(optional.isPresent());
		Assertions.assertEquals(50.3f, optional.get().getPrice());

		optional = o.getProducts().stream().filter(p -> p.getName().equals("Large Panel")).findFirst();
		Assertions.assertTrue(optional.isPresent());
		Assertions.assertEquals(60.0f, optional.get().getPrice());
		tx.commit();

		tx.begin();
		em.remove(o);
		em.remove(order2);
		em.remove(customer);
		em.remove(product1);
		em.remove(product2);
		em.remove(product3);
		em.remove(product4);
		tx.commit();

		em.close();
	}
}
