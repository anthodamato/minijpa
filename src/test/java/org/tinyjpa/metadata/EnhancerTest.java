package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.metadata.enhancer.EnhAttribute;
import org.tinyjpa.metadata.enhancer.EnhEntity;
import org.tinyjpa.metadata.enhancer.javassist.ClassInspector;
import org.tinyjpa.metadata.enhancer.javassist.EntityEnhancer;
import org.tinyjpa.metadata.enhancer.javassist.ManagedData;

public class EnhancerTest {
	@Test
	public void enhance() throws Exception {
		String className = "org.tinyjpa.jpa.model.Citizen";
		ClassInspector classInspector = new ClassInspector();
		ManagedData managedData = classInspector.inspect(className, new ArrayList<ManagedData>());

		EntityEnhancer entityEnhancer = new EntityEnhancer();
		EnhEntity enhEntity = entityEnhancer.enhance(managedData, new HashSet<EnhEntity>());
		Assertions.assertNotNull(enhEntity);
		Assertions.assertNotNull(enhEntity.getClassName());
	}

	@Test
	public void embeddedExample() throws Exception {
		String className = "org.tinyjpa.jpa.model.Book";
		ClassInspector classInspector = new ClassInspector();
		ManagedData managedData = classInspector.inspect(className, new ArrayList<ManagedData>());

		EntityEnhancer entityEnhancer = new EntityEnhancer();
		EnhEntity enhEntity = entityEnhancer.enhance(managedData, new HashSet<EnhEntity>());
		Assertions.assertNotNull(enhEntity);
		Assertions.assertNotNull(enhEntity.getClassName());

		List<EnhEntity> embeddables = enhEntity.getEmbeddables();
		Assertions.assertNotNull(embeddables);
		Assertions.assertEquals(1, embeddables.size());
		Assertions.assertNotNull(embeddables.get(0));
		enhEntity = embeddables.get(0);
		List<EnhAttribute> enhAttributes = enhEntity.getEnhAttributes();
		Assertions.assertEquals(2, enhAttributes.size());
		List<String> names = enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList());
		Assertions.assertTrue(CollectionUtils.containsAll(Arrays.asList("format", "pages"), names));
	}

}
