package org.tinyjpa.jdbc;

public interface DbTypeMapper {
	/**
	 * Maps the attribute type to the db type. For example, on Apache Derby if a
	 * column has the 'DATE' data type a LocalDate attribute type is mapped as Date.
	 * 
	 * @param attributeType
	 * @param jdbcType
	 * @return
	 */
	public Class<?> map(Class<?> attributeType, Integer jdbcType);

	/**
	 * Converts the 'value' read from a resultSet with type 'readWriteDbType' to an
	 * object with class 'attributeType'.
	 * 
	 * @param value
	 * @param readWriteDbType
	 * @param attributeType
	 * @return
	 */
	public Object convert(Object value, Class<?> readWriteDbType, Class<?> attributeType);
}
