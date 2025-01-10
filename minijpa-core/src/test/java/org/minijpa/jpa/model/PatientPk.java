package org.minijpa.jpa.model;


import java.io.Serializable;
import java.util.Objects;

public class PatientPk implements Serializable {
    private long id;
    private long doctor;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
        if (!(o instanceof PatientPk)) return false;
        PatientPk patientPk = (PatientPk) o;
        return id == patientPk.id && doctor == patientPk.doctor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, doctor);
    }

    @Override
    public String toString() {
        return "PatientPk{" +
                "id=" + id +
                ", doctor=" + doctor +
                '}';
    }
}
