package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.JdbcRecordBuilder;
import org.minijpa.jdbc.JdbcRunner;
import org.minijpa.jdbc.db.SqlSelectData;
import org.minijpa.jpa.TupleImpl;

import javax.persistence.Tuple;
import javax.persistence.criteria.CompoundSelection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

public class JdbcTupleRecordBuilder implements JdbcRecordBuilder {

    private List<Tuple> objects;
    private SqlSelectData sqlSelectData;
    private CompoundSelection<?> compoundSelection;

    public void setObjects(List<Tuple> objects) {
        this.objects = objects;
    }

    public void setSqlSelectData(SqlSelectData sqlSelectData) {
        this.sqlSelectData = sqlSelectData;
    }

    public void setCompoundSelection(CompoundSelection<?> compoundSelection) {
        this.compoundSelection = compoundSelection;
    }

    protected Object[] createRecord(int nc, List<FetchParameter> fetchParameters, ResultSet rs,
                                    ResultSetMetaData metaData) throws Exception {
        Object[] values = new Object[nc];
        for (int i = 0; i < nc; ++i) {
            int columnType = metaData.getColumnType(i + 1);
            Object v = JdbcRunner.getValue(rs, i + 1, columnType);
            values[i] = v;
        }

        return values;
    }

    @Override
    public void collectRecords(ResultSet rs) throws Exception {
        int nc = sqlSelectData.getValues().size();
        List<FetchParameter> fetchParameters = sqlSelectData.getFetchParameters();
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            Object[] values = createRecord(nc, fetchParameters, rs, metaData);
            objects.add(new TupleImpl(values, compoundSelection));
        }

    }
}