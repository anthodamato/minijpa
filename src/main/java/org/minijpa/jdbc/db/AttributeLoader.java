package org.minijpa.jdbc.db;

import org.minijpa.jdbc.MetaAttribute;

public interface AttributeLoader {
	public Object load(Object parentInstance, MetaAttribute a) throws Exception;

}
