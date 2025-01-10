package org.minijpa.jpa;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Guest;
import org.minijpa.jpa.model.GuestBooking;
import org.minijpa.jpa.model.GuestPk;
import org.minijpa.jpa.model.GuestRoomBookingId;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class GuestIdClassTest {
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws IOException {
        emf = Persistence.createEntityManagerFactory("guestidclass", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        if (emf != null)
            emf.close();
    }

    @Test
    public void persist() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        GuestBooking guestBooking = new GuestBooking();
        guestBooking.setDateOf(LocalDate.of(2024, 2, 12));
        guestBooking.setRoomNumber(3);

        Guest guest1 = new Guest();
        guest1.setId(4);
        guest1.setName("George Mallory");
        guest1.setGuestBooking(guestBooking);

        Guest guest2 = new Guest();
        guest2.setId(5);
        guest2.setName("Cherry Marlow");
        guest2.setGuestBooking(guestBooking);

        guestBooking.setNotes("Booking Notes 1");
        guestBooking.setGuests(List.of(guest1, guest2));

        em.persist(guest1);
        em.persist(guest2);
        em.persist(guestBooking);

        tx.commit();

        tx.begin();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Guest> root = criteriaQuery.from(Guest.class);
        criteriaQuery.select(criteriaBuilder.count(root));
        Long count = em.createQuery(criteriaQuery).getSingleResult();
        Assertions.assertEquals(2, count);
        tx.commit();

        em.detach(guestBooking);
        em.detach(guest1);
        em.detach(guest2);

        tx.begin();
        GuestRoomBookingId guestRoomBookingId = new GuestRoomBookingId();
        guestRoomBookingId.setRoomNumber(3);
        guestRoomBookingId.setDateOf(LocalDate.of(2024, 2, 12));

        GuestPk guestPk = new GuestPk();
        guestPk.setId(4);
        guestPk.setGuestBooking(guestRoomBookingId);
        Guest guest_1 = em.find(Guest.class, guestPk);
        Assertions.assertNotNull(guest_1);
        Assertions.assertEquals("George Mallory", guest_1.getName());
        Assertions.assertNotNull(guest_1.getGuestBooking());

        GuestBooking guestBooking_1 = em.find(GuestBooking.class, guestRoomBookingId);
        Assertions.assertNotNull(guestBooking_1);

        List<Guest> guests = guestBooking_1.getGuests();
        Assertions.assertNotNull(guests);
        Assertions.assertEquals(2, guests.size());
        Assertions.assertNotNull(guests.get(0).getGuestBooking());
        Assertions.assertNotNull(guests.get(1).getGuestBooking());

        em.remove(guests.get(0));
        em.remove(guests.get(1));
        em.remove(guestBooking_1);
        tx.commit();

        em.close();
    }
}
