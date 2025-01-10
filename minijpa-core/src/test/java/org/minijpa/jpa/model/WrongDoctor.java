package org.minijpa.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class WrongDoctor {
    @Id
    private long id;

    private String name;

    @OneToMany(mappedBy = "doctor")
    private List<WrongPatient> patients;

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

    public List<WrongPatient> getPatients() {
        return patients;
    }

    public void setPatients(List<WrongPatient> patients) {
        this.patients = patients;
    }
}
