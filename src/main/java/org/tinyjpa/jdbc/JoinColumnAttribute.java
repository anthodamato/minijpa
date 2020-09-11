package org.tinyjpa.jdbc;

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
		private Integer sqlType;
		private MetaAttribute foreignKeyAttribute;

		public Builder withColumnName(String columnName) {
			this.columnName = columnName;
			return this;
		}

		public Builder withType(Class<?> type) {
			this.type = type;
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

		public JoinColumnAttribute build() {
			JoinColumnAttribute joinColumnAttribute = new JoinColumnAttribute();
			joinColumnAttribute.columnName = columnName;
			joinColumnAttribute.type = type;
			joinColumnAttribute.sqlType = sqlType;
			joinColumnAttribute.foreignKeyAttribute = foreignKeyAttribute;
			return joinColumnAttribute;
		}
	}
}
