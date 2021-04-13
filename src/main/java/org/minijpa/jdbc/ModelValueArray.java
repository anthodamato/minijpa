/*
 * Copyright (C) 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.minijpa.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 * @author adamato
 * @param <T>
 */
public class ModelValueArray<T> {

    private final List<T> models = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    public ModelValueArray(List<T> models, List<Object> values) {
	this.models.addAll(models);
	this.values.addAll(values);
    }

    public ModelValueArray() {
    }

    public void add(T model, Object value) {
	models.add(model);
	values.add(value);
    }

    public List<T> getModels() {
	return models;
    }

    public List<Object> getValues() {
	return values;
    }

    public T getModel(int index) {
	return models.get(index);
    }

    public Object getValue(int index) {
	return values.get(index);
    }

    public Optional<Object> getValue(T model) {
	for (int i = 0; i < size(); ++i) {
	    if (getModel(i) == model)
		return Optional.of(getValue(i));
	}

	return Optional.empty();
    }

    public Optional<Object> getValue(Function<T, ?> p, Object subModel) {
	for (int i = 0; i < size(); ++i) {
	    if (p.apply(getModel(i)) == subModel)
		return Optional.of(getValue(i));
	}

	return Optional.empty();
    }

    public boolean isEmpty() {
	return models.isEmpty();
    }

    public int size() {
	return models.size();
    }

//    public static AttributeValueArray get(List<?> ms, List<Object> values) {
//	AttributeValueArray attributeValueArray = new AttributeValueArray<>();
//	for (int i = 0; i < ms.size(); ++i) {
//	    attributeValueArray.add(ms.get(i), values.get(i));
//	}
//
//	return attributeValueArray;
//    }
}
