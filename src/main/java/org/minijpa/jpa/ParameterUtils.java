/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import java.util.Map;
import java.util.Set;
import javax.persistence.Parameter;

/**
 *
 * @author adamato
 */
public class ParameterUtils {

    public static Object getParameterValue(String name, Map<Parameter<?>, Object> parameterValues) {
	Set<Parameter<?>> parameters = parameterValues.keySet();
	for (Parameter<?> p : parameters) {
	    if (p.getName() != null && p.getName().equals(name))
		return parameterValues.get(p);
	}

	return null;
    }
}
