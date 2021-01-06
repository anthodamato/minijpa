package org.tinyjpa.jdbc.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.tinyjpa.jdbc.model.join.FromJoin;

public class FromTableImpl implements FromTable {
	private String name;
	private Optional<String> alias = Optional.empty();
	private Optional<List<FromJoin>> fromJoins = Optional.empty();

	public FromTableImpl(String name) {
		super();
		this.name = name;
	}

	public FromTableImpl(String name, String alias) {
		super();
		this.name = name;
		this.alias = Optional.of(alias);
	}

	public FromTableImpl(String name, String alias, List<FromJoin> fromJoins) {
		super();
		this.name = name;
		this.alias = Optional.of(alias);
		this.fromJoins = Optional.of(Collections.unmodifiableList(fromJoins));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Optional<String> getAlias() {
		return alias;
	}

	@Override
	public Optional<List<FromJoin>> getJoins() {
		return fromJoins;
	}

}
