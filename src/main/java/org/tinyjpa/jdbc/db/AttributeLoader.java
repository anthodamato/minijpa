package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.MetaAttribute;

public interface AttributeLoader {
	public Object load(Object parentInstance, MetaAttribute a) throws Exception;

}
