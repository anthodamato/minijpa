package org.minijpa.jdbc.db;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.minijpa.jdbc.BasicFetchParameter;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.sql.model.FromTable;

public class SqlSelectDataTest {
    @Test
    public void sqlSelectData() {
        FetchParameter fetchParameter = new BasicFetchParameter("id", Types.BIGINT, Optional.empty());
        SqlSelectDataBuilder sqlSelectDataBuilder = new SqlSelectDataBuilder();

        List<FetchParameter> fetchParameters = Arrays.asList(fetchParameter);
        ((SqlSelectDataBuilder) sqlSelectDataBuilder.withFromTable(FromTable.of("citizen")))
                .withFetchParameters(fetchParameters);
        SqlSelectData sqlSelectData = (SqlSelectData) sqlSelectDataBuilder.build();
        Assertions.assertNotNull(sqlSelectData);
        Assertions.assertEquals(fetchParameters, sqlSelectData.getFetchParameters());
    }
}
