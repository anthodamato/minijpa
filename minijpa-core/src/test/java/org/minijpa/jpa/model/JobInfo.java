/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.sql.Date;

/**
 * @author adamato
 */
@Embeddable
public class JobInfo {

    @Column(name = "jd")
    String jobDescription;
    @Column(name = "start_date")
    java.sql.Date startDate;
    @ManyToOne
    ProgramManager pm;

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ProgramManager getPm() {
        return pm;
    }

    public void setPm(ProgramManager pm) {
        this.pm = pm;
    }

}
