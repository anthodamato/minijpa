/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 *
 * @author adamato
 */
@Embeddable
public class JobInfo {

    @Column(name = "jd")
    String jobDescription;
    @ManyToOne
    ProgramManager pm;

    public String getJobDescription() {
	return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
	this.jobDescription = jobDescription;
    }

    public ProgramManager getPm() {
	return pm;
    }

    public void setPm(ProgramManager pm) {
	this.pm = pm;
    }

}
