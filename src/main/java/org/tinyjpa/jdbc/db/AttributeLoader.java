package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.Attribute;

public interface AttributeLoader {
	public Object load(Object parentInstance, Attribute a) throws Exception;

}
