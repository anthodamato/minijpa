package org.minijpa.jpa.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "data_types")
public class DataTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(name = "time_value")
    private Date timeValue;

    public Date getTimeValue() {
        return timeValue;
    }

    public void setTimeValue(Date timeValue) {
        this.timeValue = timeValue;
    }

    public long getId() {
        return id;
    }

}
