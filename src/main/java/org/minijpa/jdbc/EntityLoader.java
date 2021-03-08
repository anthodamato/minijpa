/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc;

/**
 *
 * @author adamato
 */
public interface EntityLoader {

    public Object findById(MetaEntity metaEntityJE, Object primaryKey) throws Exception;

    public void refresh(MetaEntity metaEntity, Object entityInstance, Object primaryKey) throws Exception;

    public Object build(QueryResultValues queryResultValues, MetaEntity entity) throws Exception;

    public Object loadAttribute(Object parentInstance, MetaAttribute a, Object value) throws Exception;

}
