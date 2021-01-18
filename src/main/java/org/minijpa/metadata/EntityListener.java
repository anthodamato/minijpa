package org.minijpa.metadata;

public interface EntityListener {
	public Object get(Object value, String attributeName, Object entityInstance);

	public void set(Object value, String attributeName, Object entityInstance);

	public void set(byte value, String attributeName, Object entityInstance);

	public void set(short value, String attributeName, Object entityInstance);

	public void set(int value, String attributeName, Object entityInstance);

	public void set(long value, String attributeName, Object entityInstance);

	public void set(float value, String attributeName, Object entityInstance);

	public void set(double value, String attributeName, Object entityInstance);

	public void set(char value, String attributeName, Object entityInstance);

	public void set(boolean value, String attributeName, Object entityInstance);
}
