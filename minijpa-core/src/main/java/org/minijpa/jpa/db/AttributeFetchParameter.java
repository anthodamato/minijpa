package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jpa.model.MetaAttribute;

public interface AttributeFetchParameter extends FetchParameter {
	public MetaAttribute getAttribute();
}
