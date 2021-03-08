/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.Map;
import java.util.Optional;

/**
 *
 * @author adamato
 */
public interface EntityModificationRepository {

    public void save(Object owningEntityInstance, String attributeName, Object value);

    public Optional<Map<String, Object>> get(Object entityInstance);

    public void remove(Object entityInstance);
}
