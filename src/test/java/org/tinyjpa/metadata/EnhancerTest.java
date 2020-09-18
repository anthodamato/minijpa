package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnhancerTest {
	@Test
	public void enhance() throws Exception {
		List<String> classNames = new ArrayList<>();
		classNames.add("org.tinyjpa.jpa.model.Citizen");
		EntityEnhancer entityEnhancer = new EntityEnhancer(classNames);
		List<EnhEntity> enhEntities = entityEnhancer.enhance();
		Assertions.assertNotNull(enhEntities);
		Assertions.assertEquals(1, enhEntities.size());
		Assertions.assertNotNull(enhEntities.get(0));
		EnhEntity enhEntity = enhEntities.get(0);
		Assertions.assertNotNull(enhEntity.getClassName());
	}

	@Test
	public void embeddedExample() throws Exception {
		List<String> classNames = new ArrayList<>();
		classNames.add("org.tinyjpa.jpa.model.embedded.Book");
		EntityEnhancer entityEnhancer = new EntityEnhancer(classNames);
		List<EnhEntity> enhEntities = entityEnhancer.enhance();
		Assertions.assertNotNull(enhEntities);
		Assertions.assertEquals(1, enhEntities.size());
		Assertions.assertNotNull(enhEntities.get(0));
		EnhEntity enhEntity = enhEntities.get(0);
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
