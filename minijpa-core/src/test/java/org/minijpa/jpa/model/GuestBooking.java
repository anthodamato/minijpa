package org.minijpa.jpa.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "guest_booking")
@IdClass(GuestRoomBookingId.class)
public class GuestBooking {

    @Id
    private LocalDate dateOf;
    @Id
    @Column(name = "room_number")
    private Integer roomNumber;

    private String notes;
    @OneToMany(mappedBy = "guestBooking")
    private List<Guest> guests;

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }
}
