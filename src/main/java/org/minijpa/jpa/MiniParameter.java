/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa;

import javax.persistence.Parameter;

/**
 *
 * @author adamato
 * @param <T>
 */
public class MiniParameter<T> implements Parameter<T> {

    private final String name;
    private final Integer position;
    private final Class<T> parameterType;

    public MiniParameter(String name, Integer position, Class<T> parameterType) {
	this.name = name;
	this.position = position;
	this.parameterType = parameterType;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public Integer getPosition() {
	return position;
    }

    @Override
    public Class<T> getParameterType() {
	return parameterType;
    }

}
