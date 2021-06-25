/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import org.minijpa.jpa.model.JobCandidate;
import org.minijpa.jpa.model.SkillSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class CascadePersistTest {

    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() {
	emf = Persistence.createEntityManagerFactory("cascade_persist", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
	emf.close();
    }

    @Test
    public void cascadePersist() throws Exception {
	SkillSet skillSet = new SkillSet();
	skillSet.setSkills("Skills");

	JobCandidate jobCandidate = new JobCandidate();
	jobCandidate.setName("Jack");
	jobCandidate.setLastName("Glass");
	jobCandidate.setSkillSet(skillSet);
	EntityManager em = emf.createEntityManager();

	// persist (insert)
	em.getTransaction().begin();
	em.persist(jobCandidate);
	em.flush();

	em.detach(jobCandidate);
	em.detach(skillSet);

	Assertions.assertNotNull(skillSet.getId());
	SkillSet sks = em.find(SkillSet.class, skillSet.getId());
	Assertions.assertNotNull(sks);
	Assertions.assertEquals("Skills", sks.getSkills());

	// persist (update)
	JobCandidate jc = em.find(JobCandidate.class, jobCandidate.getId());
	jc.getSkillSet().setSkills("Software skills");
	em.persist(jc);
	em.flush();

	em.detach(jc);
	em.detach(sks);
	sks = em.find(SkillSet.class, skillSet.getId());
	Assertions.assertNotNull(sks);
	Assertions.assertEquals("Software skills", sks.getSkills());

	em.remove(em.find(JobCandidate.class, jobCandidate.getId()));
	em.remove(sks);
	em.getTransaction().commit();
	em.close();
    }

}
