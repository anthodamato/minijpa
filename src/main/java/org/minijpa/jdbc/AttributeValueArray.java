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

/**
 *
 * @author adamato
 * @param <T>
 */
public class AttributeValueArray<T> {

    private final List<T> attributes = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    public AttributeValueArray(List<T> attributes, List<Object> values) {
	this.attributes.addAll(attributes);
	this.values.addAll(values);
    }

    public AttributeValueArray() {
    }

    public void add(T attribute, Object value) {
	attributes.add(attribute);
	values.add(value);
    }

    public List<T> getAttributes() {
	return attributes;
    }

    public List<Object> getValues() {
	return values;
    }

    public T getAttribute(int index) {
	return attributes.get(index);
    }

    public Object getValue(int index) {
	return values.get(index);
    }

    public Optional<Object> getValue(T attribute) {
	for (int i = 0; i < size(); ++i) {
	    if (getAttribute(i) == attribute)
		return Optional.of(getValue(i));
	}

	return Optional.empty();
    }

    public boolean isEmpty() {
	return attributes.isEmpty();
    }

    public int size() {
	return attributes.size();
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
