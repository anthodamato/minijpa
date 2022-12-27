package org.minijpa.jpa.db;

import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jpa.model.MetaAttribute;

public interface AttributeFetchParameter extends FetchParameter {
    public MetaAttribute getAttribute();

    public static AttributeFetchParameter build(MetaAttribute attribute) {
        return new AttributeFetchParameterImpl(attribute.getColumnName(), attribute.getSqlType(), attribute);
    }

}
