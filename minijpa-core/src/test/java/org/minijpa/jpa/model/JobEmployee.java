/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

/**
 *
 * @author adamato
 */
@SqlResultSetMapping(name = "JobEmployeeResult", entities = {
    @EntityResult(entityClass = JobEmployee.class, fields = {
	@FieldResult(name = "id", column = "e_id"),
	@FieldResult(name = "name", column = "e_name"),
	@FieldResult(name = "jobInfo.jobDescription", column = "jd")
    }),
    @EntityResult(entityClass = ProgramManager.class, fields = {
	@FieldResult(name = "id", column = "p_id"),
	@FieldResult(name = "name", column = "p_name")
    })
})
@SqlResultSetMapping(name = "JobEmployeeResultConstructor", classes = {
    @ConstructorResult(targetClass = JobEmployeeDetails.class, columns = {
	@ColumnResult(name = "e_id"),
	@ColumnResult(name = "e_name"),
	@ColumnResult(name = "p_name")
    })
})
@Entity
@Table(name = "job_employee")
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
