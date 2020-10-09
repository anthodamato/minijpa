package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tinyjpa.metadata.enhancer.EnhAttribute;
import org.tinyjpa.metadata.enhancer.EnhEntity;
import org.tinyjpa.metadata.enhancer.javassist.EntityEnhancer;

public class EntityEnhancerTest {
//	@Test
//	public void mappedSuperclassEnhancement() throws Exception {
//		List<String> classNames = new ArrayList<>();
//		classNames.add("org.tinyjpa.metadata.MappedSuperclassEntity");
//		classNames.add("org.tinyjpa.metadata.MappedSuperclassSecondEntity");
//		EntityEnhancer entityEnhancer = new EntityEnhancer(classNames);
//		List<EnhEntity> enhEntities = entityEnhancer.enhance();
//		Assertions.assertEquals(2, enhEntities.size());
//
//		Optional<EnhEntity> optional = enhEntities.stream()
//				.filter(e -> e.getClassName().equals("org.tinyjpa.metadata.MappedSuperclassExample")).findFirst();
//		Assertions.assertTrue(!optional.isPresent());
//
//		optional = enhEntities.stream()
//				.filter(e -> e.getClassName().equals("org.tinyjpa.metadata.MappedSuperclassEntity")).findFirst();
//		Assertions.assertTrue(optional.isPresent());
//
//		List<EnhAttribute> enhAttributes = optional.get().getEnhAttributes();
//		Assertions.assertEquals(4, enhAttributes.size());
//		MatcherAssert.assertThat(enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList()),
//				Matchers.containsInAnyOrder("prop1", "eS1", "N", "Ns"));
//
//		checkMappedSuperclass(optional);
//
//		optional = enhEntities.stream()
//				.filter(e -> e.getClassName().equals("org.tinyjpa.metadata.MappedSuperclassSecondEntity")).findFirst();
//		Assertions.assertTrue(optional.isPresent());
//
//		enhAttributes = optional.get().getEnhAttributes();
//		Assertions.assertEquals(3, enhAttributes.size());
//		MatcherAssert.assertThat(enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList()),
//				Matchers.containsInAnyOrder("attribute", "eS", "URL"));
//
//		checkMappedSuperclass(optional);
//	}

	private void checkMappedSuperclass(Optional<EnhEntity> optional) {
		Assertions.assertNotNull(optional.get().getMappedSuperclass());
		EnhEntity mappedSuperclass = optional.get().getMappedSuperclass();
		List<EnhAttribute> enhAttributes = mappedSuperclass.getEnhAttributes();
		Assertions.assertEquals(2, enhAttributes.size());
		MatcherAssert.assertThat(enhAttributes.stream().map(a -> a.getName()).collect(Collectors.toList()),
				Matchers.containsInAnyOrder("id", "superProperty1"));
	}
}
