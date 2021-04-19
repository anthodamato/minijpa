/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 * @author adamato
 */
@Entity
@Table(name = "program_manager")
public class ProgramManager {

    @Id
    int id;

    @Column(nullable = false)
    @Basic(optional = false)
    String name;

    @OneToMany(mappedBy = "jobInfo.pm")
    Collection<JobEmployee> manages;

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public Collection<JobEmployee> getManages() {
	return manages;
    }

    public void setManages(Collection<JobEmployee> manages) {
	this.manages = manages;
    }

}
