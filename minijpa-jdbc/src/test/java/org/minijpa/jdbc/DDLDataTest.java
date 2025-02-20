package org.minijpa.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DDLDataTest {

    @Test
    public void ddlDataColumnDefinition() {
        DDLData ddlData = new DDLData("number(14,1)", null, null,
                null,
                true, null);

        Assertions.assertEquals("number(14,1)", ddlData.getColumnDefinition());
        Assertions.assertEquals(null, ddlData.getLength());
        Assertions.assertEquals(null, ddlData.getPrecision());
        Assertions.assertEquals(null, ddlData.getScale());
        Assertions.assertTrue(ddlData.getNullable());
    }

}
