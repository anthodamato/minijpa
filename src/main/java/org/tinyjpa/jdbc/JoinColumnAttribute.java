package org.tinyjpa.jdbc;

public class JoinColumnAttribute extends AbstractAttribute {
	private Attribute foreignKeyAttribute;

	public Attribute getForeignKeyAttribute() {
		return foreignKeyAttribute;
	}

	public void setForeignKeyAttribute(Attribute foreignKeyAttribute) {
		this.foreignKeyAttribute = foreignKeyAttribute;
	}

	public static class Builder {
		private String columnName;
		private Class<?> type;
		private Integer sqlType;
		private Attribute foreignKeyAttribute;

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

		public Builder withForeignKeyAttribute(Attribute foreignKeyAttribute) {
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
