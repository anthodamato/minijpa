/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author adamato
 */
@Entity
public class JobEmployee {

    @Id
    int id;

    private String name;

    @Embedded
    JobInfo jobInfo;

    public int getId() {
	return id;
    }

    public void setId(int id) {
	this.id = id;
    }

    public JobInfo getJobInfo() {
	return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
	this.jobInfo = jobInfo;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

}
