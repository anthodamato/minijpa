/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.model.relationship;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jpa.model.AbstractAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

public class JoinColumnAttribute extends AbstractAttribute {

    private RelationshipMetaAttribute attribute;
    private MetaAttribute foreignKeyAttribute;

    public RelationshipMetaAttribute getAttribute() {
        return attribute;
    }

    public MetaAttribute getForeignKeyAttribute() {
        return foreignKeyAttribute;
    }

    @Override
    public QueryParameter queryParameter(Object value) {
        MetaAttribute attribute = getForeignKeyAttribute();
        return new QueryParameter(getColumnName(),
                value, attribute.getSqlType(), attribute.getObjectConverter());
    }

    public static class Builder {

        private String columnName;
        private Class<?> type;
        private Class<?> databaseType;
        private Integer sqlType;
        private RelationshipMetaAttribute attribute;
        private MetaAttribute foreignKeyAttribute;

        public Builder withColumnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public Builder withType(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder withDatabaseType(Class<?> databaseType) {
            this.databaseType = databaseType;
            return this;
        }

        public Builder withSqlType(Integer sqlType) {
            this.sqlType = sqlType;
            return this;
        }

        public Builder withAttribute(RelationshipMetaAttribute attribute) {
            this.attribute = attribute;
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
            joinColumnAttribute.databaseType = databaseType;
            joinColumnAttribute.sqlType = sqlType;
            joinColumnAttribute.attribute = attribute;
            joinColumnAttribute.foreignKeyAttribute = foreignKeyAttribute;
            return joinColumnAttribute;
        }
    }
}
