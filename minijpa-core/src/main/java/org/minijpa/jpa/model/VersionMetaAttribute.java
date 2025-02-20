package org.minijpa.jpa.model;

import java.sql.Timestamp;
import java.time.Instant;

public class VersionMetaAttribute extends MetaAttribute {

    public Object getFirstValue() {
        Object value = null;
        Class<?> type = getType();
        if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
            value = 0;
        } else if (type == Short.class || (type.isPrimitive() && type.getName().equals("short"))) {
            value = Short.valueOf("0");
        } else if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
            value = 0L;
        } else if (type == Timestamp.class) {
            value = Timestamp.from(Instant.now());
        }

        return value;
    }

    public Object nextValue(Object currentValue) {
        Class<?> type = getType();
        if (type == Integer.class || (type.isPrimitive() && type.getName().equals("int"))) {
            Integer v = (Integer) currentValue;
            return v + 1;
        } else if (type == Short.class || (type.isPrimitive() && type.getName().equals("short"))) {
            Short v = (Short) currentValue;
            return v + 1;
        } else if (type == Long.class || (type.isPrimitive() && type.getName().equals("long"))) {
            Long v = (Long) currentValue;
            return v + 1;
        } else if (type == Timestamp.class) {
            Timestamp v = (Timestamp) currentValue;
            return new Timestamp(v.getTime() + 100);
        }

        return null;
    }

    public static class VersionMetaAttributeBuilder extends MetaAttribute.Builder {
        public VersionMetaAttributeBuilder(String name) {
            super(name);
        }

        public VersionMetaAttribute build() {
            VersionMetaAttribute attribute = new VersionMetaAttribute();
            attribute.name = super.name;
            attribute.columnName = columnName;
            attribute.type = type;
            attribute.databaseType = readWriteDbType;
            attribute.readMethod = readMethod;
            attribute.writeMethod = writeMethod;
            attribute.id = id;
            attribute.sqlType = sqlType;
            attribute.javaMember = javaMember;
            attribute.objectConverter = objectConverter;
            attribute.nullable = nullable;
            attribute.version = version;
            attribute.basic = basic;
            attribute.path = path;
            attribute.ddlData = ddlData;
            return attribute;
        }
    }
}
