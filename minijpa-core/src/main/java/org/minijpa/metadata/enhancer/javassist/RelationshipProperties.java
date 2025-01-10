/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.metadata.enhancer.javassist;

import java.util.Optional;

import javassist.CtClass;
import javassist.CtMethod;

/**
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

    @Override
    public String toString() {
        return "RelationshipProperties{" +
                "fieldName='" + fieldName + '\'' +
                ", lazy=" + lazy +
                ", joinColumn=" + joinColumn +
                ", joinColumnFieldName=" + joinColumnFieldName +
                '}';
    }
}
