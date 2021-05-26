package org.minijpa.jpa.manytomany;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Customer;
import org.minijpa.jpa.model.DeliveryType;
import org.minijpa.jpa.model.Order;
import org.minijpa.jpa.model.OrderStatus;
import org.minijpa.jpa.model.Product;

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
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Customer customer = new Customer();
	    customer.setName("Solar Ltd");
	    em.persist(customer);

	    Product product1 = new Product();
	    product1.setName("Medium Panel");
	    em.persist(product1);

	    Product product2 = new Product();
	    product2.setName("Large Panel");
	    em.persist(product2);

	    Order order1 = new Order();
	    order1.setCustomer(customer);
	    order1.setProducts(Arrays.asList(product1, product2));
	    order1.setDeliveryType(DeliveryType.STANDARD);
	    em.persist(order1);

	    Product product3 = new Product();
	    product3.setName("Small Panel");
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
	    Order o = em.find(Order.class, order1.getId());
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
	} catch (Exception e) {
	    if (e.getStackTrace() != null) {
		System.out.println("stacktrace: ");
		for (StackTraceElement element : e.getStackTrace()) {
		    System.out.println(element.getClassName() + "." + element.getMethodName() + " - "
			    + element.getLineNumber());
		}
	    }

	} finally {
	    em.close();
	}
    }

}
