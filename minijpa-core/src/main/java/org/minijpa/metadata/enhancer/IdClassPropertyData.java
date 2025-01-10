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
package org.minijpa.metadata.enhancer;

import javassist.CtClass;
import org.minijpa.metadata.enhancer.javassist.ManagedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IdClassPropertyData {

    private String className;
    private Class<?> classType;
    private List<EnhAttribute> enhAttributes = new ArrayList<>();
    private IdClassPropertyData nested;
    // it's used to finalize the idclass value class
    private ManagedData idClassManagedData;
    private CtClass idCtClass;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }

    public List<EnhAttribute> getEnhAttributes() {
        return enhAttributes;
    }

    public void setEnhAttributes(List<EnhAttribute> enhAttributes) {
        this.enhAttributes = enhAttributes;
    }

    public IdClassPropertyData getNested() {
        return nested;
    }

    public void setNested(IdClassPropertyData nested) {
        this.nested = nested;
    }

    public ManagedData getIdClassManagedData() {
        return idClassManagedData;
    }

    public void setIdClassManagedData(ManagedData idClassManagedData) {
        this.idClassManagedData = idClassManagedData;
    }

    public CtClass getIdCtClass() {
        return idCtClass;
    }

    public void setIdCtClass(CtClass idCtClass) {
        this.idCtClass = idCtClass;
    }


    public Optional<EnhAttribute> getAttribute(String name) {
        return enhAttributes.stream().filter(a -> a.getName().equals(name)).findFirst();
    }
}
