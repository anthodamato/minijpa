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
package org.minijpa.jpa.model;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.mapper.AttributeMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

public class MetaAttribute extends AbstractMetaAttribute {

    protected boolean id;
    protected Field javaMember;
    // it's a version attribute
    protected boolean version = false;
    // it's a basic attribute
    protected boolean basic;
    protected Optional<DDLData> ddlData = Optional.empty();
    protected AttributeMapper attributeMapper;

    public boolean isId() {
        return id;
    }

    public Field getJavaMember() {
        return javaMember;
    }

    public boolean isVersion() {
        return version;
    }

    public boolean isBasic() {
        return basic;
    }

    public Optional<DDLData> getDdlData() {
        return ddlData;
    }

    @Override
    public AttributeMapper getAttributeMapper() {
        return attributeMapper;
    }


    @Override
    public String toString() {
        return super.toString() + "; (Name=" + name + "; columnName=" + columnName + ")";
    }

    public static class Builder {

        protected final String name;
        protected String columnName;
        protected Class<?> type;
        protected Class<?> readWriteDbType;
        protected Method readMethod;
        protected Method writeMethod;
        protected boolean id;
        protected Integer sqlType;
        protected Field javaMember;
        protected AttributeMapper attributeMapper;
        protected boolean nullable = true;
        protected boolean version = false;
        protected boolean basic;
        protected String path;
        protected Optional<DDLData> ddlData = Optional.empty();

        public Builder(String name) {
            super();
            this.name = name;
            this.columnName = name;
        }

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

        public Builder withReadMethod(Method readMethod) {
            this.readMethod = readMethod;
            return this;
        }

        public Builder withWriteMethod(Method writeMethod) {
            this.writeMethod = writeMethod;
            return this;
        }

        public Builder isId(boolean id) {
            this.id = id;
            return this;
        }

        public Builder withSqlType(Integer sqlType) {
            this.sqlType = sqlType;
            return this;
        }

        public Builder withJavaMember(Field field) {
            this.javaMember = field;
            return this;
        }

        public Builder withAttributeMapper(AttributeMapper attributeMapper) {
            this.attributeMapper = attributeMapper;
            return this;
        }

        public Builder isNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public Builder isVersion(boolean version) {
            this.version = version;
            return this;
        }

        public Builder isBasic(boolean basic) {
            this.basic = basic;
            return this;
        }

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withDDLData(Optional<DDLData> ddlData) {
            this.ddlData = ddlData;
            return this;
        }

        public MetaAttribute build() {
            MetaAttribute attribute = new MetaAttribute();
            attribute.name = name;
            attribute.columnName = columnName;
            attribute.type = type;
            attribute.databaseType = readWriteDbType;
            attribute.readMethod = readMethod;
            attribute.writeMethod = writeMethod;
            attribute.id = id;
            attribute.sqlType = sqlType;
            attribute.javaMember = javaMember;
            attribute.attributeMapper = attributeMapper;
            attribute.nullable = nullable;
            attribute.version = version;
            attribute.basic = basic;
            attribute.path = path;
            attribute.ddlData = ddlData;
            return attribute;
        }
    }
}
