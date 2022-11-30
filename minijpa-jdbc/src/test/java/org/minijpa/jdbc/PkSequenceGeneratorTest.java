package org.minijpa.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PkSequenceGeneratorTest {
    @Test
    public void sequenceGenerator() {
        PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
        pkSequenceGenerator.setSequenceName("new_seq");
        pkSequenceGenerator.setSchema("s1");
        pkSequenceGenerator.setAllocationSize(1);
        pkSequenceGenerator.setInitialValue(0);

        Assertions.assertEquals("new_seq", pkSequenceGenerator.getSequenceName());
        Assertions.assertEquals("s1", pkSequenceGenerator.getSchema());
        Assertions.assertEquals(1, pkSequenceGenerator.getAllocationSize());
        Assertions.assertEquals(0, pkSequenceGenerator.getInitialValue());
    }
}
