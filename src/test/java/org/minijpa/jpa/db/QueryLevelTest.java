/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.db;

import java.util.Collection;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.EntityLoader;
import org.minijpa.jdbc.LockType;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.Capital;
import org.minijpa.jpa.model.Fingerprint;
import org.minijpa.jpa.model.JobEmployee;
import org.minijpa.jpa.model.JobInfo;
import org.minijpa.jpa.model.Person;
import org.minijpa.jpa.model.ProgramManager;
import org.minijpa.jpa.model.State;
import org.minijpa.metadata.MetaEntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class QueryLevelTest {

    private Logger LOG = LoggerFactory.getLogger(QueryLevelTest.class);

    @Test
    public void embedManyToOne() throws Exception {
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("embed_many_to_one", PersistenceUnitProperties.getProperties());
	DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("embed_many_to_one");

	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "embed_many_to_one");
	MetaEntity metaEntityJE = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.JobEmployee");
	MetaEntity metaEntityPM = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.ProgramManager");

	MetaEntityUtils.printMetaEntity(metaEntityJE);

	JdbcEntityManager jdbcEntityManager = persistenceUnitEnv.getJdbcEntityManager();
	EntityContainer entityContainer = persistenceUnitEnv.getEntityContainer();
	EntityLoader entityLoader = persistenceUnitEnv.getEntityLoader();

	ProgramManager programManager = new ProgramManager();
	programManager.setId(2);
	programManager.setName("Jennifer");
	jdbcEntityManager.persist(metaEntityPM, programManager, null);

	JobInfo jobInfo = new JobInfo();
	jobInfo.setJobDescription("Analyst");
	jobInfo.setPm(programManager);

	JobEmployee e1 = new JobEmployee();
	e1.setId(1);
	LOG.debug("embedManyToOne: e1.getId()=" + e1.getId());
	e1.setName("Abraham");
	e1.setJobInfo(jobInfo);
	jdbcEntityManager.persist(metaEntityJE, e1, null);
	jdbcEntityManager.flush();

	entityContainer.detach(e1);
	entityContainer.detach(programManager);

	Object entityInstance = entityLoader.findById(metaEntityJE, 1, LockType.NONE);
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
	jdbcEntityManager.remove(pm);
	jdbcEntityManager.flush();
	persistenceUnitEnv.getConnectionHolder().commit();
	persistenceUnitEnv.getConnectionHolder().closeConnection();
    }

    @Test
    public void oneToOneBidLazy() throws Exception {
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetoone_bid_lazy", PersistenceUnitProperties.getProperties());
	DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("onetoone_bid_lazy");

	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "onetoone_bid_lazy");
	MetaEntity metaEntityState = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.State");
	MetaEntity metaEntityCapital = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.Capital");
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

	State s = (State) entityLoader.findById(metaEntityState, state.getId(), LockType.NONE);

	Assertions.assertFalse(s == state);
	Assertions.assertEquals("England", state.getName());

	Capital c = s.getCapital();
	Assertions.assertNotNull(c);
	Assertions.assertNotNull(c.getState());
	Assertions.assertEquals("London", c.getName());
	jdbcEntityManager.remove(c);
	jdbcEntityManager.remove(s);
	jdbcEntityManager.flush();
	persistenceUnitEnv.getConnectionHolder().commit();
	persistenceUnitEnv.getConnectionHolder().closeConnection();
    }

    @Test
    public void oneToOneBid() throws Exception {
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("onetoone_bid", PersistenceUnitProperties.getProperties());
	DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration("onetoone_bid");

	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(dbConfiguration, "onetoone_bid");
	MetaEntity metaEntityPerson = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.Person");
	MetaEntity metaEntityFingerprint = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.Fingerprint");
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

	Assertions.assertNotNull(fingerprint.getId());
	entityContainer.detach(person);

	Person p = (Person) entityLoader.findById(metaEntityPerson, person.getId(), LockType.NONE);

	Assertions.assertNotNull(p);
	Assertions.assertFalse(p == person);
	Assertions.assertEquals(person.getId(), p.getId());
	Assertions.assertNotNull(p.getFingerprint());
	Assertions.assertEquals("John Smith", p.getName());
	Assertions.assertEquals("arch", p.getFingerprint().getType());
	jdbcEntityManager.remove(p);
	jdbcEntityManager.remove(fingerprint);
	jdbcEntityManager.flush();
	persistenceUnitEnv.getConnectionHolder().commit();
	persistenceUnitEnv.getConnectionHolder().closeConnection();
    }

}
