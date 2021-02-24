/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.manytoone;

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.JobEmployee;
import org.minijpa.jpa.model.JobInfo;
import org.minijpa.jpa.model.ProgramManager;

/**
 *
 * @author adamato
 */
public class EmbedManyToOneTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("embed_many_to_one");
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void persist() throws Exception {
	final EntityManager em = emf.createEntityManager();
	EntityTransaction tx = em.getTransaction();
	try {
	    tx.begin();

	    ProgramManager programManager = new ProgramManager();
	    programManager.setId(2);
	    em.persist(programManager);

	    JobInfo jobInfo = new JobInfo();
	    jobInfo.setJobDescription("Analyst");
	    jobInfo.setPm(programManager);

	    JobEmployee e1 = new JobEmployee();
	    e1.setId(1);
	    e1.setName("Abraham");
	    e1.setJobInfo(jobInfo);
	    em.persist(e1);

	    tx.commit();

	    tx.begin();
	    em.detach(e1);
	    em.detach(programManager);

	    e1 = em.find(JobEmployee.class, e1.getId());
	    JobInfo info = e1.getJobInfo();
	    Assertions.assertNotNull(info);
	    ProgramManager pm = info.getPm();
	    Assertions.assertNotNull(pm);
	    Collection<JobEmployee> employees = pm.getManages();
	    Assertions.assertNotNull(employees);
	    Assertions.assertEquals(1, employees.size());
	} finally {
	    tx.commit();
	    em.close();
	}
    }
}
