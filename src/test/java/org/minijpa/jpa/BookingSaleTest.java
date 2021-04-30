package org.minijpa.jpa;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.minijpa.jpa.model.Booking;
import org.minijpa.jpa.model.BookingSale;
import org.minijpa.jpa.model.RoomBookingId;

/**
 * java -jar $DERBY_HOME/lib/derbyrun.jar server start
 *
 * connect 'jdbc:derby://localhost:1527/test';
 *
 * @author adamato
 *
 */
public class BookingSaleTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("booking_sale");
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Disabled
    @Test
    public void persist() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    Booking booking = createBooking();
	    em.persist(booking);

	    BookingSale bookingSale = createBookingSale(booking);
	    em.persist(bookingSale);

	    Assertions.assertNotNull(bookingSale.getBooking());
	    tx.commit();

	    tx.begin();
	    BookingSale b = em.find(BookingSale.class, bookingSale.getId());
	    Assertions.assertTrue(b == bookingSale);
	    Assertions.assertNotNull(b);
	    booking = b.getBooking();
	    Assertions.assertNotNull(booking);
	    Assertions.assertEquals(Date.valueOf(LocalDate.of(2020, 10, 1)), booking.getRoomBookingId().getDateof());

	    em.detach(bookingSale);
	    b = em.find(BookingSale.class, bookingSale.getId());
	    Assertions.assertFalse(b == bookingSale);
	    Assertions.assertNotNull(b);

	    BookingSale b2 = em.find(BookingSale.class, b.getId());
	    Assertions.assertTrue(b2 == b);

	    em.remove(b);
	    em.remove(b.getBooking());
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    private Booking createBooking() {
	RoomBookingId roomBookingId = new RoomBookingId();
	Date date = Date.valueOf(LocalDate.of(2020, 10, 1));
	roomBookingId.setDateof(date);
	roomBookingId.setRoomNumber(23);

	Booking booking = new Booking();
	booking.setCustomerId(1);
	booking.setRoomBookingId(roomBookingId);

	return booking;
    }

    private BookingSale createBookingSale(Booking booking) {
	BookingSale bookingSale = new BookingSale();
	bookingSale.setBooking(booking);
	bookingSale.setPerc(10);
	return bookingSale;
    }
}
