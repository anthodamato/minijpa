package org.tinyjpa.jpa.model.embedded;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class HotelBooking {
	@EmbeddedId
	private RoomBookingId roomBookingId;

	@Column(name = "customer_id")
	private Integer customerId;

	private Float price;

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

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

}
