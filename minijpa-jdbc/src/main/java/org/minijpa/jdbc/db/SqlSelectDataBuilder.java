package org.minijpa.jdbc.db;

import java.util.List;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.sql.model.SqlSelect;
import org.minijpa.sql.model.SqlSelectBuilder;

public class SqlSelectDataBuilder extends SqlSelectBuilder {
    private List<FetchParameter> fetchParameters;

    public SqlSelectBuilder withFetchParameters(List<FetchParameter> fetchParameters) {
        this.fetchParameters = fetchParameters;
        return this;
    }

    @Override
    public SqlSelect build() {
        SqlSelectData sqlSelect = new SqlSelectData();
        build(sqlSelect);
        sqlSelect.setFetchParameters(fetchParameters);
        return sqlSelect;
    }

}
