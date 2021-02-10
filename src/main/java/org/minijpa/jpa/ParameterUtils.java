/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import javax.persistence.Parameter;
import javax.persistence.Query;

/**
 *
 * @author adamato
 */
public class ParameterUtils {

    private static BiPredicate<Parameter, Object> findByName = (p, v) -> (p.getName() != null && p.getName().compareTo((String) v) == 0);
    private static BiPredicate<Parameter, Object> findByPosition = (p, v) -> (p.getPosition() != null && p.getPosition().compareTo((Integer) v) == 0);

    private static Optional<Parameter<?>> findParameterBy(Object value, Map<Parameter<?>, Object> parameterValues, BiPredicate<Parameter, Object> findPredicate) {
	Set<Parameter<?>> parameters = parameterValues.keySet();
	for (Parameter<?> p : parameters) {
	    if (findPredicate.test(p, value))
		return Optional.of(p);
	}

	return Optional.empty();
    }

    public static Optional<Parameter<?>> findParameterByName(String name, Map<Parameter<?>, Object> parameterValues) {
	return findParameterBy(name, parameterValues, findByName);
    }

    public static Optional<Parameter<?>> findParameterByPosition(int position, Map<Parameter<?>, Object> parameterValues) {
	return findParameterBy(position, parameterValues, findByPosition);
    }

    private static List<IndexParameter> findIndexParameters(String sqlString, String ph, Parameter<?> p) {
	int index = 0;
	List<IndexParameter> indexParameters = new ArrayList<>();
	while (index != -1) {
	    index = sqlString.indexOf(ph, index);
	    if (index != -1) {
		IndexParameter indexParameter = new IndexParameter(index, p, ph);
		indexParameters.add(indexParameter);
		++index;
	    }
	}

	return indexParameters;
    }

    public static List<IndexParameter> findIndexParameters(Query query, String sqlString) {
	Set<Parameter<?>> parameters = query.getParameters();
	List<IndexParameter> indexParameters = new ArrayList<>();
	for (Parameter<?> p : parameters) {
	    if (p.getName() != null) {
		String s = ":" + p.getName();
		List<IndexParameter> ips = findIndexParameters(sqlString, s, p);
		if (ips.isEmpty())
		    throw new IllegalArgumentException("Named parameter '" + p.getName() + "' not bound");

		indexParameters.addAll(ips);
	    } else if (p.getPosition() != null) {
		String s = "?" + p.getPosition();
		List<IndexParameter> ips = findIndexParameters(sqlString, s, p);
		if (ips.isEmpty())
		    throw new IllegalArgumentException("Parameter at position '" + p.getPosition() + "' not bound");

		indexParameters.addAll(ips);
	    }
	}

	indexParameters.sort(Comparator.comparing(IndexParameter::getIndex));
	return indexParameters;
    }

    public static String replaceParameterPlaceholders(Query query, String sqlString, List<IndexParameter> indexParameters) {
	String sql = sqlString;
	for (IndexParameter ip : indexParameters) {
	    sql = sql.replace(ip.placeholder, "?");
	}

	return sql;
    }

    public static List<Object> sortParameterValues(Query query, List<IndexParameter> indexParameters) {
	List<Object> values = new ArrayList<>();
	for (IndexParameter ip : indexParameters) {
	    values.add(query.getParameterValue(ip.parameter));
	}

	return values;
    }

    public static class IndexParameter {

	private int index;
	private Parameter<?> parameter;
	private String placeholder;

	public IndexParameter(int index, Parameter<?> parameter, String placeholder) {
	    this.index = index;
	    this.parameter = parameter;
	    this.placeholder = placeholder;
	}

	public int getIndex() {
	    return index;
	}

    }
//    public static Object getParameterValue(String name, Map<Parameter<?>, Object> parameterValues) {
//	Set<Parameter<?>> parameters = parameterValues.keySet();
//	for (Parameter<?> p : parameters) {
//	    if (p.getName() != null && p.getName().equals(name))
//		return parameterValues.get(p);
//	}
//
//	return null;
//    }
}
