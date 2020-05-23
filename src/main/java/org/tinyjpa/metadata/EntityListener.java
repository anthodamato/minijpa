package org.tinyjpa.metadata;

public interface EntityListener {
	public Object get(Object value, String attributeName, Object entityInstance);

	public void set(Object value, String attributeName, Object entityInstance);
}
