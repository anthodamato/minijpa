/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jpa.model.Capital;
import org.minijpa.jpa.model.Fingerprint;
import org.minijpa.jpa.model.JobEmployee;
import org.minijpa.jpa.model.JobInfo;
import org.minijpa.jpa.model.Person;
import org.minijpa.jpa.model.ProgramManager;
import org.minijpa.jpa.model.State;
import org.minijpa.metadata.MetaEntityUtils;

/**
 *
 * @author adamato
 */
public class QueryLevelTest {

    @Test
    public void embedManyToOne() throws Exception {
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(new ApacheDerbyConfiguration(), "embed_many_to_one");
	MetaEntity metaEntityJE = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.JobEmployee");
	MetaEntity metaEntityPM = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.ProgramManager");

	MetaEntityUtils.printMetaEntity(metaEntityJE);

	JdbcEntityManager jdbcEntityManager = persistenceUnitEnv.getJdbcEntityManager();
	EntityContainer entityContainer = persistenceUnitEnv.getEntityContainer();
	EntityLoader entityLoader = persistenceUnitEnv.getEntityLoader();

	ProgramManager programManager = new ProgramManager();
	programManager.setId(2);
	jdbcEntityManager.persist(metaEntityPM, programManager, null);

	JobInfo jobInfo = new JobInfo();
	jobInfo.setJobDescription("Analyst");
	jobInfo.setPm(programManager);

	JobEmployee e1 = new JobEmployee();
	e1.setId(1);
	e1.setName("Abraham");
	e1.setJobInfo(jobInfo);
	jdbcEntityManager.persist(metaEntityJE, e1, null);
	jdbcEntityManager.flush();

	entityContainer.detach(e1);
	entityContainer.detach(programManager);

	Object entityInstance = entityLoader.findById(metaEntityJE, 1);
	Assertions.assertNotNull(entityInstance);
	Assertions.assertTrue(entityInstance instanceof JobEmployee);

	e1 = (JobEmployee) entityInstance;
	JobInfo info = e1.getJobInfo();
	Assertions.assertNotNull(info);
	String jd = info.getJobDescription();
	Assertions.assertEquals("Analyst", jd);

	ProgramManager pm = info.getPm();
	Assertions.assertNotNull(pm);
	Collection<JobEmployee> employees = pm.getManages();
	Assertions.assertNotNull(employees);
	Assertions.assertEquals(1, employees.size());

	jdbcEntityManager.remove(e1);
	jdbcEntityManager.remove(programManager);
	jdbcEntityManager.flush();
	persistenceUnitEnv.getConnectionHolder().commit();
	persistenceUnitEnv.getConnectionHolder().closeConnection();
    }

    @Test
    public void oneToOneBidLazy() throws Exception {
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(new ApacheDerbyConfiguration(), "onetoone_bid_lazy");
	MetaEntity metaEntityState = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.State");
	MetaEntity metaEntityCapital = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.Capital");
	JdbcEntityManager jdbcEntityManager = persistenceUnitEnv.getJdbcEntityManager();
	EntityContainer entityContainer = persistenceUnitEnv.getEntityContainer();
	EntityLoader entityLoader = persistenceUnitEnv.getEntityLoader();

	State state = new State();
	state.setName("England");

	Capital capital = new Capital();
	capital.setName("London");

	state.setCapital(capital);

	jdbcEntityManager.persist(metaEntityCapital, capital, null);
	jdbcEntityManager.persist(metaEntityState, state, null);
	jdbcEntityManager.flush();

	entityContainer.detach(capital);
	entityContainer.detach(state);

	State s = (State) entityLoader.findById(metaEntityState, state.getId());

	Assertions.assertFalse(s == state);
	Assertions.assertEquals("England", state.getName());

	Capital c = s.getCapital();
	Assertions.assertNotNull(c);
	Assertions.assertEquals("London", c.getName());
	jdbcEntityManager.remove(c);
	jdbcEntityManager.remove(s);
	jdbcEntityManager.flush();
	persistenceUnitEnv.getConnectionHolder().commit();
	persistenceUnitEnv.getConnectionHolder().closeConnection();
    }

    @Test
    public void oneToOneBid() throws Exception {
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(new ApacheDerbyConfiguration(), "onetoone_bid");
	MetaEntity metaEntityPerson = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.Person");
	MetaEntity metaEntityFingerprint = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.Fingerprint");
	JdbcEntityManager jdbcEntityManager = persistenceUnitEnv.getJdbcEntityManager();
	EntityContainer entityContainer = persistenceUnitEnv.getEntityContainer();
	EntityLoader entityLoader = persistenceUnitEnv.getEntityLoader();

	Person person = new Person();
	person.setName("John Smith");

	Fingerprint fingerprint = new Fingerprint();
	fingerprint.setType("arch");
	fingerprint.setPerson(person);
	person.setFingerprint(fingerprint);

	jdbcEntityManager.persist(metaEntityPerson, person, null);
	jdbcEntityManager.persist(metaEntityFingerprint, fingerprint, null);
	jdbcEntityManager.flush();

	entityContainer.detach(person);

	Person p = (Person) entityLoader.findById(metaEntityPerson, person.getId());

	Assertions.assertNotNull(p);
	Assertions.assertFalse(p == person);
	Assertions.assertEquals(person.getId(), p.getId());
	Assertions.assertNotNull(p.getFingerprint());
	Assertions.assertEquals("John Smith", p.getName());
	Assertions.assertEquals("arch", p.getFingerprint().getType());
	jdbcEntityManager.remove(person);
	jdbcEntityManager.remove(fingerprint);
	jdbcEntityManager.flush();
	persistenceUnitEnv.getConnectionHolder().commit();
	persistenceUnitEnv.getConnectionHolder().closeConnection();
    }

}
