package org.minijpa.jdbc;

import java.sql.ResultSet;

public interface JdbcRecordBuilder {
    public void collectRecords(ResultSet rs) throws Exception;

}
