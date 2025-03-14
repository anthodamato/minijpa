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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.ModelValueArray;
import org.minijpa.jpa.MiniEntityManager;
import org.minijpa.jpa.PersistenceUnitProperties;
import org.minijpa.jpa.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EntityDelegateInstanceBuilderTest {

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
    public void modifications() throws Exception {
        EntityManager em = emf.createEntityManager();
        PersistenceUnitContext persistenceUnitContext = ((MiniEntityManager) em).getPersistenceUnitContext();
        MetaEntity metaEntityJE = persistenceUnitContext.getEntities().get("org.minijpa.jpa.model.JobEmployee");
        MetaEntity metaEntityPM = persistenceUnitContext.getEntities().get("org.minijpa.jpa.model.ProgramManager");
        em.close();

        ProgramManager programManager = new ProgramManager();
        programManager.setId(2);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobDescription("Analyst");
        jobInfo.setPm(programManager);

        JobEmployee e1 = new JobEmployee();
        e1.setId(1);
        e1.setName("Abraham");
        e1.setJobInfo(jobInfo);

        ModelValueArray<AbstractMetaAttribute> modelValueArray = metaEntityPM.getModifications(
                programManager);
        Assertions.assertTrue(modelValueArray.isEmpty());

        modelValueArray = metaEntityJE.getModifications(e1);
        Assertions.assertEquals(3, modelValueArray.size());
        AbstractMetaAttribute a0 = modelValueArray.getModel(0);
        AbstractMetaAttribute a1 = modelValueArray.getModel(1);
        AbstractMetaAttribute a2 = modelValueArray.getModel(2);

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

        metaEntityJE.clearModificationAttributes(e1);
        modelValueArray = metaEntityJE.getModifications(e1);
        Assertions.assertEquals(0, modelValueArray.size());
    }
}
