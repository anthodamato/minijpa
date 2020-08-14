package org.tinyjpa.jdbc;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AttributeUtilTest {
	@Test
	public void isCollectionClass() {
		List<String> list = Arrays.asList("test1", "test2");
		boolean isCollection = AttributeUtil.isCollectionClass(list.getClass());
		Assertions.assertTrue(isCollection);
	}
}
