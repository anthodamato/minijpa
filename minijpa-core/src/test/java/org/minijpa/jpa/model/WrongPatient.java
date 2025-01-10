package org.minijpa.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

@IdClass(WrongPatientPk.class)
@Entity
public class WrongPatient {
    @Id
    private long id;

    @Id
    @ManyToOne
    private WrongDoctor wrongDoctor;

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

    public WrongDoctor getWrongDoctor() {
        return wrongDoctor;
    }

    public void setWrongDoctor(WrongDoctor wrongDoctor) {
        this.wrongDoctor = wrongDoctor;
    }
}
