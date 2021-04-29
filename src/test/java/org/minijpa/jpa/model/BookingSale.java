/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
@Entity
@Table(name = "booking_sale")
public class BookingSale {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumns({
	@JoinColumn(name = "b_dateof", referencedColumnName = "DATEOF"),
	@JoinColumn(name = "b_room_number", referencedColumnName = "ROOM_NUMBER")
    })
    private Booking booking;

    private int perc;

    public Long getId() {
	return id;
    }

    public Booking getBooking() {
	return booking;
    }

    public void setBooking(Booking booking) {
	this.booking = booking;
    }

    public int getPerc() {
	return perc;
    }

    public void setPerc(int perc) {
	this.perc = perc;
    }

}
