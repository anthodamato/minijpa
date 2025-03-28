package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

public interface AttributeFetchParameter extends FetchParameter {

    AbstractMetaAttribute getAttribute();

    static AttributeFetchParameter build(MetaAttribute attribute) {
        return new AttributeFetchParameterImpl(attribute.getColumnName(), attribute.getSqlType(),
                attribute, attribute.getObjectConverter());
    }

    static AttributeFetchParameter build(RelationshipMetaAttribute attribute) {
        return new AttributeFetchParameterImpl(attribute.getColumnName(), attribute.getSqlType(),
                attribute);
    }

    static AttributeFetchParameter build(AbstractMetaAttribute attribute) {
        if (attribute instanceof MetaAttribute) {
            return build((MetaAttribute) attribute);
        }

        return build((RelationshipMetaAttribute) attribute);
    }
}
