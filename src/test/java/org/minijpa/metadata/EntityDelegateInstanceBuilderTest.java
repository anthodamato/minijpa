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

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.AttributeValueArray;
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

    @Disabled
    @Test
    public void modifications() throws Exception {
	PersistenceUnitEnv persistenceUnitEnv = PersistenceUnitEnv.build(new ApacheDerbyConfiguration(),
		"embed_many_to_one");
	MetaEntity metaEntityJE = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.JobEmployee");
	MetaEntity metaEntityPM = persistenceUnitEnv.getEntities().get("org.minijpa.jpa.model.ProgramManager");

	ProgramManager programManager = new ProgramManager();
	programManager.setId(2);

	JobInfo jobInfo = new JobInfo();
	jobInfo.setJobDescription("Analyst");
	jobInfo.setPm(programManager);

	JobEmployee e1 = new JobEmployee();
	e1.setId(1);
	e1.setName("Abraham");
	e1.setJobInfo(jobInfo);

	EntityInstanceBuilder entityInstanceBuilder = new EntityDelegateInstanceBuilder();
	AttributeValueArray attributeValueArray = entityInstanceBuilder.getModifications(metaEntityPM, programManager);
	Assertions.assertTrue(attributeValueArray.isEmpty());

	attributeValueArray = entityInstanceBuilder.getModifications(metaEntityJE, e1);
	Assertions.assertEquals(3, attributeValueArray.size());
	MetaAttribute a0 = attributeValueArray.getAttribute(0);
	MetaAttribute a1 = attributeValueArray.getAttribute(1);
	MetaAttribute a2 = attributeValueArray.getAttribute(2);

	MetaAttributeFolder metaAttributeFolder = new MetaAttributeFolder(a0, a1, a2);
	Assertions.assertTrue(metaAttributeFolder.findByName("name").isPresent());
	Assertions.assertTrue(metaAttributeFolder.findByName("jobDescription").isPresent());
	Assertions.assertTrue(metaAttributeFolder.findByName("pm").isPresent());

	Optional<Object> v = attributeValueArray.getValue(metaAttributeFolder.findByName("name").get());
	Assertions.assertTrue(v.isPresent());
	Assertions.assertEquals("Abraham", v.get());

	v = attributeValueArray.getValue(metaAttributeFolder.findByName("jobDescription").get());
	Assertions.assertTrue(v.isPresent());
	Assertions.assertEquals("Analyst", v.get());

	v = attributeValueArray.getValue(metaAttributeFolder.findByName("pm").get());
	Assertions.assertTrue(v.isPresent());
	Assertions.assertEquals(programManager, v.get());

	entityInstanceBuilder.removeChanges(metaEntityJE, e1);
	attributeValueArray = entityInstanceBuilder.getModifications(metaEntityJE, e1);
	Assertions.assertEquals(0, attributeValueArray.size());
    }
}
