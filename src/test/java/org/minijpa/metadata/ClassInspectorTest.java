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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.minijpa.metadata.enhancer.javassist.AttributeData;
import org.minijpa.metadata.enhancer.javassist.ClassInspector;
import org.minijpa.metadata.enhancer.javassist.ManagedData;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.minijpa.metadata.enhancer.javassist.Property;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class ClassInspectorTest {

    @Test
    public void book() throws Exception {
	String className = "org.minijpa.jpa.model.Book";
	ClassInspector classInspector = new ClassInspector();
	ManagedData managedData = classInspector.inspect(className, new ArrayList<>());
	assertNotNull(managedData);
	assertNotNull(managedData.getCtClass());
	assertEquals(className, managedData.getCtClass().getName());
	assertEquals(ManagedData.ENTITY, managedData.getType());
	assertEquals("mds0", managedData.getModificationAttribute());
	assertEquals("lta0", managedData.getLockTypeAttribute().get());

	List<AttributeData> attributeDatas = managedData.getAttributeDataList();
	assertNotNull(attributeDatas);
	assertEquals(4, attributeDatas.size());

	Optional<AttributeData> optional = managedData.findAttribute("id");
	assertTrue(optional.isPresent());

	optional = managedData.findAttribute("title");
	assertTrue(optional.isPresent());

	optional = managedData.findAttribute("author");
	assertTrue(optional.isPresent());

	optional = managedData.findAttribute("bookFormat");
	assertTrue(optional.isPresent());
	AttributeData attributeData = optional.get();
	Property property = attributeData.getProperty();
	assertTrue(property.isEmbedded());

	assertNotNull(attributeData.getEmbeddedData());
	ManagedData embeddedData = attributeData.getEmbeddedData();
	assertNotNull(embeddedData.getCtClass());
	assertEquals("org.minijpa.jpa.model.BookFormat", embeddedData.getCtClass().getName());
	assertEquals(ManagedData.EMBEDDABLE, embeddedData.getType());
	assertEquals("mds0", embeddedData.getModificationAttribute());

	List<AttributeData> embeddedAttributeDatas = embeddedData.getAttributeDataList();
	assertNotNull(embeddedAttributeDatas);
	assertEquals(2, embeddedAttributeDatas.size());
	Optional<AttributeData> opt = embeddedData.findAttribute("format");
	assertTrue(opt.isPresent());
	attributeData = opt.get();
	assertFalse(attributeData.getProperty().isEmbedded());
	assertFalse(attributeData.isParentIsEmbeddedId());

	opt = embeddedData.findAttribute("pages");
	assertTrue(opt.isPresent());
    }
}
