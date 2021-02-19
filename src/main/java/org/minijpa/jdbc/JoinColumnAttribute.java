package org.minijpa.jdbc;

import org.minijpa.jdbc.mapper.JdbcAttributeMapper;

public class JoinColumnAttribute extends AbstractAttribute {

    private MetaAttribute foreignKeyAttribute;

    public MetaAttribute getForeignKeyAttribute() {
	return foreignKeyAttribute;
    }

    public void setForeignKeyAttribute(MetaAttribute foreignKeyAttribute) {
	this.foreignKeyAttribute = foreignKeyAttribute;
    }

    public static class Builder {

	private String columnName;
	private Class<?> type;
	private Class<?> readWriteDbType;
	private DbTypeMapper dbTypeMapper;
	private Integer sqlType;
	private MetaAttribute foreignKeyAttribute;
	protected JdbcAttributeMapper jdbcAttributeMapper;

	public Builder withColumnName(String columnName) {
	    this.columnName = columnName;
	    return this;
	}

	public Builder withType(Class<?> type) {
	    this.type = type;
	    return this;
	}

	public Builder withReadWriteDbType(Class<?> readWriteDbType) {
	    this.readWriteDbType = readWriteDbType;
	    return this;
	}

	public Builder withDbTypeMapper(DbTypeMapper dbTypeMapper) {
	    this.dbTypeMapper = dbTypeMapper;
	    return this;
	}

	public Builder withSqlType(Integer sqlType) {
	    this.sqlType = sqlType;
	    return this;
	}

	public Builder withForeignKeyAttribute(MetaAttribute foreignKeyAttribute) {
	    this.foreignKeyAttribute = foreignKeyAttribute;
	    return this;
	}

	public Builder withJdbcAttributeMapper(JdbcAttributeMapper jdbcAttributeMapper) {
	    this.jdbcAttributeMapper = jdbcAttributeMapper;
	    return this;
	}

	public JoinColumnAttribute build() {
	    JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute();
	    joinColumnAttribute.columnName = columnName;
	    joinColumnAttribute.type = type;
	    joinColumnAttribute.readWriteDbType = readWriteDbType;
	    joinColumnAttribute.dbTypeMapper = dbTypeMapper;
	    joinColumnAttribute.sqlType = sqlType;
	    joinColumnAttribute.foreignKeyAttribute = foreignKeyAttribute;
	    joinColumnAttribute.jdbcAttributeMapper = jdbcAttributeMapper;
	    return joinColumnAttribute;
	}
    }
}
