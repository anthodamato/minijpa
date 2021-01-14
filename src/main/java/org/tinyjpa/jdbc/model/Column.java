package org.tinyjpa.jdbc.model;

import java.util.Optional;

public class Column {
	private String name;
	private Optional<String> alias = Optional.empty();

	public Column(String name) {
		super();
		this.name = name;
	}

	public Column(String name, String alias) {
		this(name);
		if (alias != null)
			this.alias = Optional.of(alias);
	}

	public String getName() {
		return name;
	}

	public Optional<String> getAlias() {
		return alias;
	}

	@Override
	public String toString() {
		return "Column: " + name + (alias.isPresent() ? alias.get() : "");
	}

}
