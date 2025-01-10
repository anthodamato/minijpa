package org.minijpa.jpa.model;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RoomBookingId implements Serializable {

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
		if (dateof != null)
			hash = 31 * hash + dateof.hashCode();

		if (roomNumber != null)
			hash = 31 * hash + roomNumber.hashCode();

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RoomBookingId))
			return false;

		RoomBookingId roomBookingId = (RoomBookingId) obj;
		if ((dateof != null && !dateof.equals(roomBookingId.dateof))
				|| (roomBookingId.dateof != null && !roomBookingId.dateof.equals(dateof)))
			return false;

		if ((roomNumber != null && roomNumber.equals(roomBookingId.roomNumber))
				|| (roomBookingId.roomNumber != null && roomBookingId.roomNumber.equals(roomNumber)))
			return false;

		return true;
	}

}
