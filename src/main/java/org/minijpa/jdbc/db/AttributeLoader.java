package org.minijpa.jdbc.db;

import org.minijpa.jdbc.MetaAttribute;

/**
 * Lazy loading attribute interface.
 *
 * @author adamato
 */
public interface AttributeLoader {

    /**
     * Called by EntityDelegate on a lazy load attribute.
     *
     * @param parentInstance The attribute owning instance
     * @param a attribute to load
     * @param value current attribute value
     * @return
     * @throws Exception
     */
    public Object load(Object parentInstance, MetaAttribute a, Object value) throws Exception;

}
