/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.manytoone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.JobEmployee;
import org.minijpa.jpa.model.JobEmployeeDetails;
import org.minijpa.jpa.model.JobInfo;
import org.minijpa.jpa.model.ProgramManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;

/**
 * @author adamato
 */
public class EmbedManyToOneTest {

    private Logger LOG = LoggerFactory.getLogger(EmbedManyToOneTest.class);
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws Exception {
        emf = Persistence.createEntityManagerFactory("embed_many_to_one", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void persist() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        ProgramManager programManager = new ProgramManager();
        programManager.setId(2);
        programManager.setName("John");
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
        em.remove(e1);
        em.remove(pm);

        tx.commit();
        em.close();
    }


    private static class Entities {
        ProgramManager pm1;
        ProgramManager pm2;
        JobEmployee e1;
        JobEmployee e2;
        JobEmployee e3;
    }


    private Entities persistEntities(EntityManager em) {
        ProgramManager pm1 = new ProgramManager();
        pm1.setName("Wells");
        pm1.setId(1);
        em.persist(pm1);

        ProgramManager pm2 = new ProgramManager();
        pm2.setName("Hogan");
        pm2.setId(2);
        em.persist(pm2);

        JobInfo ji1 = new JobInfo();
        ji1.setJobDescription("Analyst");
        ji1.setPm(pm1);

        JobInfo ji2 = new JobInfo();
        ji2.setJobDescription("Developer");
        ji2.setPm(pm1);

        JobInfo ji3 = new JobInfo();
        ji3.setJobDescription("Developer");
        ji3.setPm(pm2);

        JobEmployee e1 = new JobEmployee();
        e1.setId(1);
        e1.setName("Abraham");
        e1.setJobInfo(ji1);
        em.persist(e1);

        JobEmployee e2 = new JobEmployee();
        e2.setId(2);
        e2.setName("Paul");
        e2.setJobInfo(ji2);
        em.persist(e2);

        JobEmployee e3 = new JobEmployee();
        e3.setId(3);
        e3.setName("Kate");
        e3.setJobInfo(ji3);
        em.persist(e3);

        Entities entities = new Entities();
        entities.pm1 = pm1;
        entities.pm2 = pm2;
        entities.e1 = e1;
        entities.e2 = e2;
        entities.e3 = e3;
        return entities;
    }


    @Test
    public void nativeQuery() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Entities entities = persistEntities(em);

        tx.commit();

        tx.begin();

        Query q = em.createNativeQuery("select e.id as e_id, p.id as p_id, e.jd, e.name "
                + "from job_employee e, program_manager p where e.pm_id=p.id and e.jd='Developer' order by e.name");
        List idList = q.getResultList();
        Assertions.assertEquals(2, idList.size());
        Object[] oo0 = (Object[]) idList.get(0);
        if (oo0[0] instanceof BigDecimal) // Oracle, native query
            Assertions.assertEquals(new BigDecimal(3), oo0[0]);
        else
            Assertions.assertEquals(3, oo0[0]);

        if (oo0[1] instanceof BigDecimal) // Oracle, native query
            Assertions.assertEquals(new BigDecimal(2), oo0[1]);
        else
            Assertions.assertEquals(2, oo0[1]);

        Assertions.assertEquals("Developer", oo0[2]);
        Assertions.assertEquals("Kate", oo0[3]);
        Object[] oo1 = (Object[]) idList.get(1);
        if (oo1[0] instanceof BigDecimal) // Oracle, native query
            Assertions.assertEquals(new BigDecimal(2), oo1[0]);
        else
            Assertions.assertEquals(2, oo1[0]);

        if (oo1[1] instanceof BigDecimal) // Oracle, native query
            Assertions.assertEquals(new BigDecimal(1), oo1[1]);
        else
            Assertions.assertEquals(1, oo1[1]);

        Assertions.assertEquals("Developer", oo1[2]);
        Assertions.assertEquals("Paul", oo1[3]);

        em.detach(entities.e1);
        em.detach(entities.e2);
        em.detach(entities.e3);
        em.detach(entities.pm1);
        em.detach(entities.pm2);

        Query query = em.createNativeQuery(
                "select e.id as e_id, e.name as e_name, e.jd as jd, e.pm_id, p.id as p_id, p.name as p_name"
                        + " from job_employee e, program_manager p where e.pm_id=p.id and e.jd='Developer' order by e.name",
                "JobEmployeeResult");
        List result = query.getResultList();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());

        Object[] r0 = (Object[]) result.get(0);
        Assertions.assertEquals(2, r0.length);
        Assertions.assertTrue(r0[0] instanceof JobEmployee);
        Assertions.assertEquals("Kate", ((JobEmployee) r0[0]).getName());
        Assertions.assertNotNull(((JobEmployee) r0[0]).getJobInfo());
        Assertions.assertNotNull(((JobEmployee) r0[0]).getJobInfo().getPm());
        Assertions.assertEquals("Developer", ((JobEmployee) r0[0]).getJobInfo().getJobDescription());
        Assertions.assertEquals("Hogan", ((JobEmployee) r0[0]).getJobInfo().getPm().getName());
        Assertions.assertTrue(r0[1] instanceof ProgramManager);
        Assertions.assertEquals("Hogan", ((ProgramManager) r0[1]).getName());

        Object[] r1 = (Object[]) result.get(1);
        Assertions.assertTrue(r1[0] instanceof JobEmployee);
        Assertions.assertEquals("Paul", ((JobEmployee) r1[0]).getName());
        Assertions.assertEquals("Developer", ((JobEmployee) r1[0]).getJobInfo().getJobDescription());
        Assertions.assertEquals("Wells", ((JobEmployee) r1[0]).getJobInfo().getPm().getName());
        Assertions.assertTrue(r1[1] instanceof ProgramManager);
        Assertions.assertEquals("Wells", ((ProgramManager) r1[1]).getName());

        JobEmployee e1 = em.find(JobEmployee.class, 1);
        JobEmployee e2 = em.find(JobEmployee.class, 2);
        JobEmployee e3 = em.find(JobEmployee.class, 3);
        ProgramManager pm1 = em.find(ProgramManager.class, 1);
        ProgramManager pm2 = em.find(ProgramManager.class, 2);

        em.remove(e1);
        em.remove(e2);
        em.remove(e3);
        em.remove(pm1);
        em.remove(pm2);

        tx.commit();
        em.close();
    }


    @Test
    public void nativeQueryConstructor() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Entities entities = persistEntities(em);

        tx.commit();

        tx.begin();

        Query q = em.createNativeQuery("select e.id as e_id, p.id as p_id, e.jd, e.name "
                + "from job_employee e, program_manager p where e.pm_id=p.id and e.jd='Developer' order by e.name");
        List idList = q.getResultList();
        Assertions.assertEquals(2, idList.size());

        em.detach(entities.e1);
        em.detach(entities.e2);
        em.detach(entities.e3);
        em.detach(entities.pm1);
        em.detach(entities.pm2);

        Query query = em.createNativeQuery(
                "select e.id as e_id, e.name as e_name, e.jd as jd, e.pm_id, p.id as p_id, p.name as p_name"
                        + " from job_employee e, program_manager p where e.pm_id=p.id order by e.name",
                "JobEmployeeResultConstructor");
        List result = query.getResultList();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());

        Object r0 = result.get(0);
        Assertions.assertTrue(r0 instanceof JobEmployeeDetails);
        Assertions.assertEquals("Abraham", ((JobEmployeeDetails) r0).getEmployeeName());
        Assertions.assertEquals("Wells", ((JobEmployeeDetails) r0).getManagerName());

        Object r1 = result.get(1);
        Assertions.assertTrue(r1 instanceof JobEmployeeDetails);
        Assertions.assertEquals("Kate", ((JobEmployeeDetails) r1).getEmployeeName());
        Assertions.assertEquals("Hogan", ((JobEmployeeDetails) r1).getManagerName());

        Object r2 = result.get(2);
        Assertions.assertTrue(r2 instanceof JobEmployeeDetails);
        Assertions.assertEquals("Paul", ((JobEmployeeDetails) r2).getEmployeeName());
        Assertions.assertEquals("Wells", ((JobEmployeeDetails) r2).getManagerName());

        JobEmployee e1 = em.find(JobEmployee.class, 1);
        JobEmployee e2 = em.find(JobEmployee.class, 2);
        JobEmployee e3 = em.find(JobEmployee.class, 3);
        ProgramManager pm1 = em.find(ProgramManager.class, 1);
        ProgramManager pm2 = em.find(ProgramManager.class, 2);

        em.remove(e1);
        em.remove(e2);
        em.remove(e3);
        em.remove(pm1);
        em.remove(pm2);

        tx.commit();
        em.close();
    }


    private Entities persistEntitiesWithDates(EntityManager em) {
        ProgramManager pm1 = new ProgramManager();
        pm1.setName("Wells");
        pm1.setId(1);
        em.persist(pm1);

        ProgramManager pm2 = new ProgramManager();
        pm2.setName("Hogan");
        pm2.setId(2);
        em.persist(pm2);

        JobInfo ji1 = new JobInfo();
        ji1.setJobDescription("Analyst");
        ji1.setPm(pm1);
        ji1.setStartDate(java.sql.Date.valueOf(LocalDate.of(2020, 4, 10)));

        JobInfo ji2 = new JobInfo();
        ji2.setJobDescription("Developer");
        ji2.setPm(pm1);
        ji2.setStartDate(java.sql.Date.valueOf(LocalDate.of(2020, 5, 10)));

        JobInfo ji3 = new JobInfo();
        ji3.setJobDescription("Developer");
        ji3.setPm(pm2);
        ji3.setStartDate(java.sql.Date.valueOf(LocalDate.of(2020, 6, 10)));

        JobEmployee e1 = new JobEmployee();
        e1.setId(1);
        e1.setName("Abraham");
        e1.setJobInfo(ji1);
        em.persist(e1);

        JobEmployee e2 = new JobEmployee();
        e2.setId(2);
        e2.setName("Paul");
        e2.setJobInfo(ji2);
        em.persist(e2);

        JobEmployee e3 = new JobEmployee();
        e3.setId(3);
        e3.setName("Kate");
        e3.setJobInfo(ji3);
        em.persist(e3);

        Entities entities = new Entities();
        entities.pm1 = pm1;
        entities.pm2 = pm2;
        entities.e1 = e1;
        entities.e2 = e2;
        entities.e3 = e3;
        return entities;
    }


    @Test
    public void nativeQueryConstructorAndScalars() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Entities entities = persistEntitiesWithDates(em);

        tx.commit();

        tx.begin();

        Query q = em.createNativeQuery("select e.id as e_id, p.id as p_id, e.jd, e.name "
                + "from job_employee e, program_manager p where e.pm_id=p.id and e.jd='Developer' order by e.name");
        List idList = q.getResultList();
        Assertions.assertEquals(2, idList.size());

        em.detach(entities.e1);
        em.detach(entities.e2);
        em.detach(entities.e3);
        em.detach(entities.pm1);
        em.detach(entities.pm2);

        Query query = em.createNativeQuery(
                "select e.id as e_id, e.name as e_name, e.jd as jd, e.pm_id, e.start_date as start_date, p.id as p_id, p.name as p_name"
                        + " from job_employee e, program_manager p where e.pm_id=p.id order by e.name",
                "JobEmployeeResultConstructorAndScalars");
        List result = query.getResultList();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());

        Object r0 = result.get(0);
        Assertions.assertNotNull(r0);
        Assertions.assertTrue(r0 instanceof Object[]);
        Object[] array = (Object[]) r0;
        Assertions.assertTrue(array[0] instanceof JobEmployeeDetails);
        Assertions.assertEquals("Abraham", ((JobEmployeeDetails) array[0]).getEmployeeName());
        Assertions.assertEquals("Wells", array[1]);
        java.util.Date date0 = java.util.Date.from(LocalDate.of(2020, 4, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(date0, array[2]);

        Object r1 = result.get(1);
        Object[] array1 = (Object[]) r1;
        Assertions.assertTrue(array1[0] instanceof JobEmployeeDetails);
        Assertions.assertEquals("Kate", ((JobEmployeeDetails) array1[0]).getEmployeeName());
        Assertions.assertEquals("Hogan", array1[1]);
        java.util.Date date1 = java.util.Date.from(LocalDate.of(2020, 6, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(date1, array1[2]);

        Object r2 = result.get(2);
        Object[] array2 = (Object[]) r2;
        Assertions.assertTrue(array2[0] instanceof JobEmployeeDetails);
        Assertions.assertEquals("Paul", ((JobEmployeeDetails) array2[0]).getEmployeeName());
        Assertions.assertEquals("Wells", array2[1]);
        java.util.Date date2 = java.util.Date.from(LocalDate.of(2020, 6, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(date2, array1[2]);

        JobEmployee e1 = em.find(JobEmployee.class, 1);
        JobEmployee e2 = em.find(JobEmployee.class, 2);
        JobEmployee e3 = em.find(JobEmployee.class, 3);
        ProgramManager pm1 = em.find(ProgramManager.class, 1);
        ProgramManager pm2 = em.find(ProgramManager.class, 2);

        em.remove(e1);
        em.remove(e2);
        em.remove(e3);
        em.remove(pm1);
        em.remove(pm2);

        tx.commit();
        em.close();
    }


    @Test
    public void namedNativeQuery() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Entities entities = persistEntities(em);

        tx.commit();

        tx.begin();

        em.detach(entities.e1);
        em.detach(entities.e2);
        em.detach(entities.e3);
        em.detach(entities.pm1);
        em.detach(entities.pm2);

        Query query = em.createNamedQuery("Named_JobEmployeeResult");
        List result = query.getResultList();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());

        Object[] r0 = (Object[]) result.get(0);
        Assertions.assertEquals(2, r0.length);
        Assertions.assertTrue(r0[0] instanceof JobEmployee);
        Assertions.assertEquals("Kate", ((JobEmployee) r0[0]).getName());
        Assertions.assertNotNull(((JobEmployee) r0[0]).getJobInfo());
        Assertions.assertNotNull(((JobEmployee) r0[0]).getJobInfo().getPm());
        Assertions.assertEquals("Developer", ((JobEmployee) r0[0]).getJobInfo().getJobDescription());
        Assertions.assertEquals("Hogan", ((JobEmployee) r0[0]).getJobInfo().getPm().getName());
        Assertions.assertTrue(r0[1] instanceof ProgramManager);
        Assertions.assertEquals("Hogan", ((ProgramManager) r0[1]).getName());

        Object[] r1 = (Object[]) result.get(1);
        Assertions.assertTrue(r1[0] instanceof JobEmployee);
        Assertions.assertEquals("Paul", ((JobEmployee) r1[0]).getName());
        Assertions.assertEquals("Developer", ((JobEmployee) r1[0]).getJobInfo().getJobDescription());
        Assertions.assertEquals("Wells", ((JobEmployee) r1[0]).getJobInfo().getPm().getName());
        Assertions.assertTrue(r1[1] instanceof ProgramManager);
        Assertions.assertEquals("Wells", ((ProgramManager) r1[1]).getName());

        JobEmployee e1 = em.find(JobEmployee.class, 1);
        JobEmployee e2 = em.find(JobEmployee.class, 2);
        JobEmployee e3 = em.find(JobEmployee.class, 3);
        ProgramManager pm1 = em.find(ProgramManager.class, 1);
        ProgramManager pm2 = em.find(ProgramManager.class, 2);

        em.remove(e1);
        em.remove(e2);
        em.remove(e3);
        em.remove(pm1);
        em.remove(pm2);

        tx.commit();
        em.close();
    }


    @Test
    public void namedNativeQueryConstructor() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Entities entities = persistEntities(em);

        tx.commit();

        tx.begin();

        em.detach(entities.e1);
        em.detach(entities.e2);
        em.detach(entities.e3);
        em.detach(entities.pm1);
        em.detach(entities.pm2);

        Query query = em.createNamedQuery("Named_JobEmployeeResultConstructor");
        List result = query.getResultList();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());

        Object r0 = (Object) result.get(0);
        Assertions.assertTrue(r0 instanceof JobEmployeeDetails);
        Assertions.assertEquals("Abraham", ((JobEmployeeDetails) r0).getEmployeeName());
        Assertions.assertEquals("Wells", ((JobEmployeeDetails) r0).getManagerName());

        Object r1 = (Object) result.get(1);
        Assertions.assertTrue(r1 instanceof JobEmployeeDetails);
        Assertions.assertEquals("Kate", ((JobEmployeeDetails) r1).getEmployeeName());
        Assertions.assertEquals("Hogan", ((JobEmployeeDetails) r1).getManagerName());

        Object r2 = (Object) result.get(2);
        Assertions.assertTrue(r2 instanceof JobEmployeeDetails);
        Assertions.assertEquals("Paul", ((JobEmployeeDetails) r2).getEmployeeName());
        Assertions.assertEquals("Wells", ((JobEmployeeDetails) r2).getManagerName());

        JobEmployee e1 = em.find(JobEmployee.class, 1);
        JobEmployee e2 = em.find(JobEmployee.class, 2);
        JobEmployee e3 = em.find(JobEmployee.class, 3);
        ProgramManager pm1 = em.find(ProgramManager.class, 1);
        ProgramManager pm2 = em.find(ProgramManager.class, 2);

        em.remove(e1);
        em.remove(e2);
        em.remove(e3);
        em.remove(pm1);
        em.remove(pm2);

        tx.commit();
        em.close();
    }


    @Test
    public void namedNativeQueryConstructorAndScalars() throws Exception {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        Entities entities = persistEntitiesWithDates(em);

        tx.commit();

        tx.begin();

        em.detach(entities.e1);
        em.detach(entities.e2);
        em.detach(entities.e3);
        em.detach(entities.pm1);
        em.detach(entities.pm2);

        Query query = em.createNamedQuery("Named_JobEmployeeResultConstructorAndScalars");
        List result = query.getResultList();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(3, result.size());

        Object r0 = result.get(0);
        Assertions.assertNotNull(r0);
        Assertions.assertTrue(r0 instanceof Object[]);
        Object[] array = (Object[]) r0;
        Assertions.assertTrue(array[0] instanceof JobEmployeeDetails);
        Assertions.assertEquals("Abraham", ((JobEmployeeDetails) array[0]).getEmployeeName());
        Assertions.assertEquals("Wells", array[1]);
        java.util.Date date0 = java.util.Date.from(LocalDate.of(2020, 4, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(date0, array[2]);

        Object r1 = result.get(1);
        Object[] array1 = (Object[]) r1;
        Assertions.assertTrue(array1[0] instanceof JobEmployeeDetails);
        Assertions.assertEquals("Kate", ((JobEmployeeDetails) array1[0]).getEmployeeName());
        Assertions.assertEquals("Hogan", array1[1]);
        java.util.Date date1 = java.util.Date.from(LocalDate.of(2020, 6, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(date1, array1[2]);

        Object r2 = result.get(2);
        Object[] array2 = (Object[]) r2;
        Assertions.assertTrue(array2[0] instanceof JobEmployeeDetails);
        Assertions.assertEquals("Paul", ((JobEmployeeDetails) array2[0]).getEmployeeName());
        Assertions.assertEquals("Wells", array2[1]);
        java.util.Date date2 = java.util.Date.from(LocalDate.of(2020, 6, 10).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(date2, array1[2]);

        JobEmployee e1 = em.find(JobEmployee.class, 1);
        JobEmployee e2 = em.find(JobEmployee.class, 2);
        JobEmployee e3 = em.find(JobEmployee.class, 3);
        ProgramManager pm1 = em.find(ProgramManager.class, 1);
        ProgramManager pm2 = em.find(ProgramManager.class, 2);

        em.remove(e1);
        em.remove(e2);
        em.remove(e3);
        em.remove(pm1);
        em.remove(pm2);

        tx.commit();
        em.close();
    }
}
