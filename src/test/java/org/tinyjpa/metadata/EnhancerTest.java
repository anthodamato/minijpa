package org.tinyjpa.metadata;

import java.util.ArrayList;
import java.util.List;

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

}
