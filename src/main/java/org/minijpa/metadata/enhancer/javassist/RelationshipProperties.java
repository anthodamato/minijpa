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
package org.minijpa.metadata.enhancer.javassist;

import java.util.Optional;
import javassist.CtClass;
import javassist.CtMethod;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class RelationshipProperties {

    private final String fieldName;
    private final CtClass type;
    private final boolean lazy;
    private final boolean joinColumn;
    private Optional<String> joinColumnFieldName = Optional.empty();
    private Optional<CtMethod> ctMethodGetter;
    private Optional<CtMethod> ctMethodSetter;

    public RelationshipProperties(String fieldName, CtClass type, boolean lazy, boolean joinColumn) {
	this.fieldName = fieldName;
	this.type = type;
	this.lazy = lazy;
	this.joinColumn = joinColumn;
    }

    public boolean isLazy() {
	return lazy;
    }

    public boolean hasJoinColumn() {
	return joinColumn;
    }

    public String getFieldName() {
	return fieldName;
    }

    public CtClass getType() {
	return type;
    }

    public Optional<String> getJoinColumnFieldName() {
	return joinColumnFieldName;
    }

    public void setJoinColumnFieldName(Optional<String> joinColumnFieldName) {
	this.joinColumnFieldName = joinColumnFieldName;
    }

    public Optional<CtMethod> getCtMethodGetter() {
	return ctMethodGetter;
    }

    public void setCtMethodGetter(Optional<CtMethod> ctMethodGetter) {
	this.ctMethodGetter = ctMethodGetter;
    }

    public Optional<CtMethod> getCtMethodSetter() {
	return ctMethodSetter;
    }

    public void setCtMethodSetter(Optional<CtMethod> ctMethodSetter) {
	this.ctMethodSetter = ctMethodSetter;
    }

}
