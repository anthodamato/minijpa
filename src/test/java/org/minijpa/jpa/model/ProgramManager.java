/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.util.Collection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author adamato
 */
@Entity
public class ProgramManager {

    @Id
    int id;
    @OneToMany(mappedBy = "jobInfo.pm")
    Collection<JobEmployee> manages;

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public Collection<JobEmployee> getManages() {
	return manages;
    }

    public void setManages(Collection<JobEmployee> manages) {
	this.manages = manages;
    }

}
