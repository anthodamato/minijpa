/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.model;

import javax.persistence.*;

/**
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
@SqlResultSetMapping(name = "JobEmployeeResultConstructorAndScalars", classes = {
        @ConstructorResult(targetClass = JobEmployeeDetails.class, columns = {
                @ColumnResult(name = "e_id"),
                @ColumnResult(name = "e_name")
        })
},
        columns = {
                @ColumnResult(name = "p_name"),
                @ColumnResult(name = "start_date", type = java.util.Date.class)
        }
)

@NamedNativeQueries(value = {
        @NamedNativeQuery(
                name = "Named_JobEmployeeResult",
                query = "select e.id as e_id, e.name as e_name, e.jd as jd, e.pm_id, p.id as p_id, p.name as p_name" +
                        " from job_employee e, program_manager p where e.pm_id=p.id and e.jd='Developer' order by e.name",
                resultSetMapping = "JobEmployeeResult"),
        @NamedNativeQuery(
                name = "Named_JobEmployeeResultConstructor",
                query = "select e.id as e_id, e.name as e_name, e.jd as jd, e.pm_id, p.id as p_id, p.name as p_name" +
                        " from job_employee e, program_manager p where e.pm_id=p.id order by e.name",
                resultSetMapping = "JobEmployeeResultConstructor"),
        @NamedNativeQuery(
                name = "Named_JobEmployeeResultConstructorAndScalars",
                query = "select e.id as e_id, e.name as e_name, e.jd as jd, e.pm_id, e.start_date as start_date, p.id as p_id, p.name as p_name" +
                        " from job_employee e, program_manager p where e.pm_id=p.id order by e.name",
                resultSetMapping = "JobEmployeeResultConstructorAndScalars")
}
)
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
