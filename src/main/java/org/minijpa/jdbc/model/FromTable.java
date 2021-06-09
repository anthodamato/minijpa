package org.minijpa.jdbc.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.model.join.FromJoin;

public interface FromTable {

    public String getName();

    public Optional<String> getAlias();

    public Optional<List<FromJoin>> getJoins();

    public static FromTable of(MetaEntity entity) {
	return new FromTableImpl(entity.getTableName(), entity.getAlias());
    }

    public static FromTable of(MetaEntity entity, FromJoin fromJoin) {
	return new FromTableImpl(entity.getTableName(), entity.getAlias(), Arrays.asList(fromJoin));
    }

    public static FromTable of(String tableName) {
	return new FromTableImpl(tableName);
    }
}
