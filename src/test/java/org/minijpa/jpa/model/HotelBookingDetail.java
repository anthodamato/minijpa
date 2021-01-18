package org.minijpa.jpa.model;

import java.util.Collection;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class HotelBookingDetail {
	@EmbeddedId
	private RoomBookingId roomBookingId;

	@OneToMany
	private Collection<HotelCustomer> customers;

	private Float price;

	public RoomBookingId getRoomBookingId() {
		return roomBookingId;
	}

	public void setRoomBookingId(RoomBookingId roomBookingId) {
		this.roomBookingId = roomBookingId;
	}

	public Collection<HotelCustomer> getCustomers() {
		return customers;
	}

	public void setCustomers(Collection<HotelCustomer> customers) {
		this.customers = customers;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

}
