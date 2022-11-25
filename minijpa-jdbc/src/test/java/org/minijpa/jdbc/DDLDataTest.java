package org.minijpa.jdbc;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DDLDataTest {
    @Test
    public void ddlDataColumnDefinition() {
        DDLData ddlData = new DDLData(Optional.of("number(14,1)"), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(true));

        Assertions.assertEquals("number(14,1)", ddlData.getColumnDefinition().get());
        Assertions.assertEquals(Optional.empty(), ddlData.getLength());
        Assertions.assertEquals(Optional.empty(), ddlData.getPrecision());
        Assertions.assertEquals(Optional.empty(), ddlData.getScale());
        Assertions.assertTrue(ddlData.getNullable().get());
    }

}
