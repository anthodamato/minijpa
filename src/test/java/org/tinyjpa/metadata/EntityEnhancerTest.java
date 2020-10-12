package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.metadata.enhancer.EnhAttribute;
import org.tinyjpa.metadata.enhancer.EnhEntity;
import org.tinyjpa.metadata.enhancer.javassist.ClassInspector;
import org.tinyjpa.metadata.enhancer.javassist.EntityEnhancer;
import org.tinyjpa.metadata.enhancer.javassist.ManagedData;

public class EntityEnhancerTest {
	@Test
	public void mappedSuperclassEnhancement() throws Exception {
		List<ManagedData> inspectedClasses = new ArrayList<>();
		Set<EnhEntity> parsedEntities = new HashSet<>();
		String className = "org.tinyjpa.metadata.MappedSuperclassEntity";
		ClassInspector classInspector = new ClassInspector();
		ManagedData managedData = classInspector.inspect(className, inspectedClasses);
		Assertions.assertNotNull(managedData);
		EntityEnhancer entityEnhancer = new EntityEnhancer();
		EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
		inspectedClasses.add(managedData);
		parsedEntities.add(enhEntity);

		List<EnhAttribute> enhAttributes = enhEntity.getEnhAttributes();
		Assertions.assertEquals(4, enhAttributes.size());
		MatcherAssert.assertThat(enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList()),
				Matchers.containsInAnyOrder("prop1", "eS1", "N", "Ns"));

		checkMappedSuperclass(enhEntity);

		className = "org.tinyjpa.metadata.MappedSuperclassSecondEntity";
		managedData = classInspector.inspect(className, inspectedClasses);
		Assertions.assertNotNull(managedData);
		enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
		inspectedClasses.add(managedData);
		parsedEntities.add(enhEntity);

		enhAttributes = enhEntity.getEnhAttributes();
		Assertions.assertEquals(3, enhAttributes.size());
		MatcherAssert.assertThat(enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList()),
				Matchers.containsInAnyOrder("attribute", "eS", "URL"));

		checkMappedSuperclass(enhEntity);
	}

	private void checkMappedSuperclass(EnhEntity enhEntity) {
		Assertions.assertNotNull(enhEntity.getMappedSuperclass());
		EnhEntity mappedSuperclass = enhEntity.getMappedSuperclass();
		List<EnhAttribute> enhAttributes = mappedSuperclass.getEnhAttributes();
		Assertions.assertEquals(2, enhAttributes.size());
		MatcherAssert.assertThat(enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList()),
				Matchers.containsInAnyOrder("id", "superProperty1"));
	}
}
