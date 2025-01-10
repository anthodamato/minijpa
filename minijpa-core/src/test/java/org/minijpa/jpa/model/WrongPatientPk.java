package org.minijpa.jpa.model;


import java.io.Serializable;
import java.util.Objects;

public class WrongPatientPk implements Serializable {
    private int id;
    private long doctor;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDoctor() {
        return doctor;
    }

    public void setDoctor(long doctor) {
        this.doctor = doctor;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WrongPatientPk)) return false;
        WrongPatientPk patientPk = (WrongPatientPk) o;
        return id == patientPk.id && doctor == patientPk.doctor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, doctor);
    }
}
