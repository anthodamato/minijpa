package org.minijpa.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.metadata.BeanUtil;

public class BeanUtilTest {
	@Test
	public void oneChar() {
		Assertions.assertEquals("N", BeanUtil.capitalize("n"));
		Assertions.assertEquals("P", BeanUtil.capitalize("P"));
	}

	@Test
	public void moreChars() {
		Assertions.assertEquals("nP", BeanUtil.capitalize("nP"));
		Assertions.assertEquals("PV", BeanUtil.capitalize("PV"));
		Assertions.assertEquals("URL", BeanUtil.capitalize("URL"));
		Assertions.assertEquals("Prop", BeanUtil.capitalize("prop"));
	}

}
