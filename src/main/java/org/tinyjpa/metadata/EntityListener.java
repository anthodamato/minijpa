package org.tinyjpa.metadata;

public interface EntityListener {
	public Object get(Object value, String attributeName, Object entityInstance);

	public byte get(byte value, String attributeName, Object entityInstance);

	public short get(short value, String attributeName, Object entityInstance);

	public int get(int value, String attributeName, Object entityInstance);

	public long get(long value, String attributeName, Object entityInstance);

	public float get(float value, String attributeName, Object entityInstance);

	public double get(double value, String attributeName, Object entityInstance);

	public char get(char value, String attributeName, Object entityInstance);

	public boolean get(boolean value, String attributeName, Object entityInstance);

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
