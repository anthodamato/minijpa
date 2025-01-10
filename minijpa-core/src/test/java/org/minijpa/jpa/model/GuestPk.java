package org.minijpa.jpa.model;


import java.io.Serializable;
import java.util.Objects;

public class GuestPk implements Serializable {
    private long id;
    private GuestRoomBookingId guestBooking;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public GuestRoomBookingId getGuestBooking() {
        return guestBooking;
    }

    public void setGuestBooking(GuestRoomBookingId guestBooking) {
        this.guestBooking = guestBooking;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GuestPk)) return false;
        GuestPk guestPk = (GuestPk) o;
        return id == guestPk.id && Objects.equals(guestBooking, guestPk.guestBooking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, guestBooking);
    }
}
