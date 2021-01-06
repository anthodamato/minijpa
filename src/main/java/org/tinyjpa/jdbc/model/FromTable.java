package org.tinyjpa.jdbc.model;

import java.util.List;
import java.util.Optional;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.model.join.FromJoin;

public interface FromTable {
	public String getName();

	public Optional<String> getAlias();

	public Optional<List<FromJoin>> getJoins();

	public static FromTable of(MetaEntity entity) {
		return new FromTableImpl(entity.getTableName(), entity.getAlias());
	}
}
