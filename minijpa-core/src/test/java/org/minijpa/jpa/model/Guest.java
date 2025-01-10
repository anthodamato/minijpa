package org.minijpa.jpa.model;

import javax.persistence.*;

@IdClass(GuestPk.class)
@Entity
public class Guest {
    @Id
    private long id;

    @Id
    @JoinColumns({
            @JoinColumn(name = "date_of_fk", referencedColumnName = "dateOf"),
            @JoinColumn(name = "room_number_fk", referencedColumnName = "roomNumber")
    })
    @ManyToOne
    private GuestBooking guestBooking;

    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GuestBooking getGuestBooking() {
        return guestBooking;
    }

    public void setGuestBooking(GuestBooking guestBooking) {
        this.guestBooking = guestBooking;
    }
}
