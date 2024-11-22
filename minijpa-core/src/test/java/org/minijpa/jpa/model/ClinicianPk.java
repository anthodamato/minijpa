package org.minijpa.jpa.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class ClinicianPk implements Serializable {
    private String name;
    private LocalDate dob;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClinicianPk)) return false;
        ClinicianPk that = (ClinicianPk) o;
        return Objects.equals(name, that.name) && Objects.equals(dob, that.dob);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dob);
    }
}
