/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.metadata;

import org.minijpa.jdbc.db.EntityInstanceBuilderImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaAttributeFolder;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.db.EntityInstanceBuilder;
import org.minijpa.jpa.db.ApacheDerbyConfiguration;
import org.minijpa.jpa.db.PersistenceUnitEnv;
import org.minijpa.jpa.model.JobEmployee;
import org.minijpa.jpa.model.JobInfo;
import org.minijpa.jpa.model.ProgramManager;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EntityDelegateInstanceBuilderTest {

    @Test
    public void modifications() throws Exception {
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(new ApacheDerbyConfiguration(),
		"embed_many_to_one");
	MetaEntity metaEntityJE = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.JobEmployee");
	MetaEntity metaEntityPM = persistenceUnitEnv.getPersistenceUnitContext().getEntities().get("org.minijpa.jpa.model.ProgramManager");

	ProgramManager programManager = new ProgramManager();
	programManager.setId(2);

	JobInfo jobInfo = new JobInfo();
	jobInfo.setJobDescription("Analyst");
	jobInfo.setPm(programManager);

	JobEmployee e1 = new JobEmployee();
	e1.setId(1);
	e1.setName("Abraham");
	e1.setJobInfo(jobInfo);

	EntityInstanceBuilder entityInstanceBuilder = new EntityInstanceBuilderImpl();
	ModelValueArray<MetaAttribute> modelValueArray = entityInstanceBuilder.getModifications(
		metaEntityPM, programManager);
	Assertions.assertTrue(modelValueArray.isEmpty());

	modelValueArray = entityInstanceBuilder.getModifications(metaEntityJE, e1);
	Assertions.assertEquals(3, modelValueArray.size());
	MetaAttribute a0 = modelValueArray.getModel(0);
	MetaAttribute a1 = modelValueArray.getModel(1);
	MetaAttribute a2 = modelValueArray.getModel(2);

	MetaAttributeFolder metaAttributeFolder = new MetaAttributeFolder(a0, a1, a2);
	Assertions.assertTrue(metaAttributeFolder.findByName("name").isPresent());
	Assertions.assertTrue(metaAttributeFolder.findByName("jobDescription").isPresent());
	Assertions.assertTrue(metaAttributeFolder.findByName("pm").isPresent());

	int index = modelValueArray.indexOfModel(metaAttributeFolder.findByName("name").get());
	Assertions.assertTrue(index != -1);
	Assertions.assertEquals("Abraham", modelValueArray.getValue(index));

	index = modelValueArray.indexOfModel(metaAttributeFolder.findByName("jobDescription").get());
	Assertions.assertTrue(index != -1);
	Assertions.assertEquals("Analyst", modelValueArray.getValue(index));

	index = modelValueArray.indexOfModel(metaAttributeFolder.findByName("pm").get());
	Assertions.assertTrue(index != -1);
	Assertions.assertEquals(programManager, modelValueArray.getValue(index));

	entityInstanceBuilder.removeChanges(metaEntityJE, e1);
	modelValueArray = entityInstanceBuilder.getModifications(metaEntityJE, e1);
	Assertions.assertEquals(0, modelValueArray.size());
    }
}
