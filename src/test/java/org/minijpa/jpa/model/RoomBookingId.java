package org.minijpa.jpa.model;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RoomBookingId implements Serializable {
	private static final long serialVersionUID = 1453073604263556318L;
	private Date dateof;
	@Column(name = "room_number")
	private Integer roomNumber;

	public Date getDateof() {
		return dateof;
	}

	public void setDateof(Date dateof) {
		this.dateof = dateof;
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
		hash = 31 * hash + dateof.hashCode();
		hash = 31 * hash + roomNumber.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof RoomBookingId))
			return false;

		RoomBookingId roomBookingId = (RoomBookingId) obj;
		if (dateof.equals(roomBookingId.dateof) && roomNumber.equals(roomBookingId.roomNumber))
			return true;

		return false;
	}

}
