package org.minijpa.jpa;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jpa.model.Doctor;
import org.minijpa.jpa.model.Patient;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.List;

public class PatientIdClassTest {
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void beforeAll() throws IOException {
        emf = Persistence.createEntityManagerFactory("idclass", PersistenceUnitProperties.getProperties());
    }

    @AfterAll
    public static void afterAll() {
        emf.close();
    }

    @Test
    public void persist() {
        final EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Doctor doctor = new Doctor();
        doctor.setId(4);
        doctor.setName("Robert Coley");

        Patient patient1 = new Patient();
        patient1.setId(1);
        patient1.setName("Natalie Li");
        patient1.setDoctor(doctor);

        Patient patient2 = new Patient();
        patient2.setId(2);
        patient2.setName("Anne Ma");
        patient2.setDoctor(doctor);

        doctor.setPatients(List.of(patient1, patient2));

        em.persist(patient1);
        em.persist(patient2);
        em.persist(doctor);

        tx.commit();

        tx.begin();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Patient> root = criteriaQuery.from(Patient.class);
        criteriaQuery.select(criteriaBuilder.count(root));
        Long count = em.createQuery(criteriaQuery).getSingleResult();
        Assertions.assertEquals(2, count);
        tx.commit();

        em.detach(doctor);
        em.detach(patient1);
        em.detach(patient2);

        tx.begin();
        Doctor doctor_1 = em.find(Doctor.class, Long.valueOf(4));

        List<Patient> patients = doctor_1.getPatients();
        Assertions.assertNotNull(patients);
        Assertions.assertEquals(2, patients.size());

        em.remove(patients.get(0));
        em.remove(patients.get(1));
        em.remove(doctor_1);
        tx.commit();

        em.close();
    }
}
