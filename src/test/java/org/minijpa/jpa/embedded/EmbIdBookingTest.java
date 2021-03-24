package org.minijpa.jpa.embedded;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.HotelBooking;
import org.minijpa.jpa.model.RoomBookingId;

/**
 *
 * @author adamato
 *
 */
public class EmbIdBookingTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("emb_booking");
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void persist() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    HotelBooking hotelBooking = createHotelBooking();
	    em.persist(hotelBooking);

	    Assertions.assertNotNull(hotelBooking.getRoomBookingId());
	    tx.commit();

	    HotelBooking b = em.find(HotelBooking.class, hotelBooking.getRoomBookingId());
	    Assertions.assertTrue(b == hotelBooking);
	    Assertions.assertNotNull(b);
	    RoomBookingId bookingId = b.getRoomBookingId();
	    Assertions.assertNotNull(bookingId);
	    Assertions.assertEquals(Date.valueOf(LocalDate.of(2020, 10, 1)), bookingId.getDateof());

	    em.detach(hotelBooking);
	    b = em.find(HotelBooking.class, hotelBooking.getRoomBookingId());
	    Assertions.assertFalse(b == hotelBooking);
	    Assertions.assertNotNull(b);

	    HotelBooking b2 = em.find(HotelBooking.class, b.getRoomBookingId());
	    Assertions.assertTrue(b2 == b);

	    tx.begin();
	    em.remove(b);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

    private HotelBooking createHotelBooking() {
	RoomBookingId roomBookingId = new RoomBookingId();
	Date date = Date.valueOf(LocalDate.of(2020, 10, 1));
	roomBookingId.setDateof(date);
	roomBookingId.setRoomNumber(23);

	HotelBooking hotelBooking = new HotelBooking();
	hotelBooking.setRoomBookingId(roomBookingId);
	hotelBooking.setCustomerId(1);
	hotelBooking.setPrice(45.5f);
	return hotelBooking;
    }

    @Test
    public void count() throws Exception {
	final EntityManager em = emf.createEntityManager();
	try {
	    final EntityTransaction tx = em.getTransaction();
	    tx.begin();

	    HotelBooking hotelBooking = createHotelBooking();
	    em.persist(hotelBooking);

	    Assertions.assertNotNull(hotelBooking.getRoomBookingId());
	    tx.commit();

	    CriteriaBuilder cb = em.getCriteriaBuilder();
	    CriteriaQuery query = cb.createQuery();
	    Root<HotelBooking> root = query.from(HotelBooking.class);
	    query.select(cb.count(root));
	    TypedQuery<?> typedQuery = em.createQuery(query);
	    Object result = typedQuery.getSingleResult();
	    Assertions.assertEquals(1L, result);

	    query.select(cb.countDistinct(root.get("customerId")));
	    typedQuery = em.createQuery(query);
	    result = typedQuery.getSingleResult();
	    Assertions.assertEquals(1L, result);

	    tx.begin();
	    em.remove(hotelBooking);
	    tx.commit();
	} finally {
	    em.close();
	}
    }

}
