package org.minijpa.jdbc;

import java.sql.ResultSet;

public interface JdbcRecordBuilder {
    void collectRecords(ResultSet rs) throws Exception;

}
