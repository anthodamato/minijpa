package org.minijpa.jpa.model;

import java.time.LocalDate;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "checkInPeriod", query = "select o from Holiday o where o.checkIn between :dateStart and :dateEnd")
})
public class Holiday {

    @GeneratedValue
    @Id
    private Long id;

    private Integer travellers;
    @Column(name = "check_in")
    private LocalDate checkIn;
    private Integer nights;
    private String referenceName;

    public Long getId() {
        return id;
    }

    public Integer getTravellers() {
        return travellers;
    }

    public void setTravellers(Integer travellers) {
        this.travellers = travellers;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public Integer getNights() {
        return nights;
    }

    public void setNights(Integer nights) {
        this.nights = nights;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

}
