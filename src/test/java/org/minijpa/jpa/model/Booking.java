package org.minijpa.jpa.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "booking")
public class Booking {

    @EmbeddedId
    private RoomBookingId roomBookingId;

    @Column(name = "customer_id")
    private Integer customerId;

    public RoomBookingId getRoomBookingId() {
	return roomBookingId;
    }

    public void setRoomBookingId(RoomBookingId roomBookingId) {
	this.roomBookingId = roomBookingId;
    }

    public Integer getCustomerId() {
	return customerId;
    }

    public void setCustomerId(Integer customerId) {
	this.customerId = customerId;
    }

}
