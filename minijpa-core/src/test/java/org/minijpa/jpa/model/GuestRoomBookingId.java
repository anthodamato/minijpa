package org.minijpa.jpa.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class GuestRoomBookingId implements Serializable {
    private final Logger log = LoggerFactory.getLogger(GuestRoomBookingId.class);

    private LocalDate dateOf;
    private Integer roomNumber;

    public LocalDate getDateOf() {
        return dateOf;
    }

    public void setDateOf(LocalDate dateOf) {
        this.dateOf = dateOf;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.dateOf);
        hash = 31 * hash + Objects.hashCode(this.roomNumber);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        log.debug("equals: (obj instanceof GuestRoomBookingId)={}",(obj instanceof GuestRoomBookingId));
        if (obj == null || !(obj instanceof GuestRoomBookingId))
            return false;

        GuestRoomBookingId roomBookingId = (GuestRoomBookingId) obj;
        if (dateOf.equals(roomBookingId.dateOf) && roomNumber.equals(roomBookingId.roomNumber))
            return true;

        return false;
    }

    @Override
    public String toString() {
        return "GuestRoomBookingId{" +
                "dateOf=" + dateOf +
                ", roomNumber=" + roomNumber +
                '}';
    }
}
