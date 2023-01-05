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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javassist.CtClass;

public class ManagedData {

    public static final int ENTITY = 0;
    public static final int EMBEDDABLE = 1;
    public static final int MAPPEDSUPERCLASS = 2;
    private String className;
    private CtClass ctClass;
    private final List<AttributeData> attributeDatas = new ArrayList<>();
    public ManagedData mappedSuperclass;
    private final List<ManagedData> embeddables = new ArrayList<>();
    int type = ENTITY;
    private final List<BMTMethodInfo> methodInfos = new ArrayList<>();
    private String modificationAttribute;
    private Optional<String> lazyLoadedAttribute = Optional.empty();
    private Optional<String> joinColumnPostponedUpdateAttribute = Optional.empty();
    // the lock type attribute is created only in entity classes, neither mapped
    // superclass or embedded
    private Optional<String> lockTypeAttribute = Optional.empty();
    private Optional<String> entityStatusAttribute = Optional.empty();

    public ManagedData() {
        super();
    }

    public ManagedData(int type) {
        super();
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }

    void setClassName(String className) {
        this.className = className;
    }

    public CtClass getCtClass() {
        return ctClass;
    }

    void setCtClass(CtClass ctClass) {
        this.ctClass = ctClass;
    }

    public List<AttributeData> getAttributeDataList() {
        return attributeDatas;
    }

    void addAttributeDatas(List<AttributeData> datas) {
        attributeDatas.addAll(datas);
    }

    public List<ManagedData> getEmbeddables() {
        return embeddables;
    }

    public List<BMTMethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public String getModificationAttribute() {
        return modificationAttribute;
    }

    public void setModificationAttribute(String modificationAttribute) {
        this.modificationAttribute = modificationAttribute;
    }

    public Optional<String> getLazyLoadedAttribute() {
        return lazyLoadedAttribute;
    }

    public void setLazyLoadedAttribute(Optional<String> lazyLoadedAttribute) {
        this.lazyLoadedAttribute = lazyLoadedAttribute;
    }

    public Optional<String> getJoinColumnPostponedUpdateAttribute() {
        return joinColumnPostponedUpdateAttribute;
    }

    public void setJoinColumnPostponedUpdateAttribute(Optional<String> joinColumnPostponedUpdateAttribute) {
        this.joinColumnPostponedUpdateAttribute = joinColumnPostponedUpdateAttribute;
    }

    public Optional<String> getLockTypeAttribute() {
        return lockTypeAttribute;
    }

    public void setLockTypeAttribute(Optional<String> lockTypeAttribute) {
        this.lockTypeAttribute = lockTypeAttribute;
    }

    public Optional<String> getEntityStatusAttribute() {
        return entityStatusAttribute;
    }

    public void setEntityStatusAttribute(Optional<String> entityStatusAttribute) {
        this.entityStatusAttribute = entityStatusAttribute;
    }

    public Optional<AttributeData> findAttribute(String name) {
        Optional<AttributeData> optional = attributeDatas.stream()
                .filter(a -> a.getProperty().getCtField().getName().equals(name)).findFirst();
        if (optional.isPresent())
            return optional;

        if (mappedSuperclass != null) {
            Optional<AttributeData> opt = mappedSuperclass.findAttribute(name);
            if (opt.isPresent())
                return opt;
        }

        return Optional.empty();
    }

    public Optional<ManagedData> findParentManagedData(String attributeName) {
        Optional<AttributeData> optional = attributeDatas.stream()
                .filter(a -> a.getProperty().getCtField().getName().equals(attributeName)).findFirst();
        if (optional.isPresent())
            return Optional.of(this);

        // look inside embeddables
        for (AttributeData attributeData : attributeDatas) {
            if (attributeData.getProperty().isEmbedded() && !attributeData.getProperty().isId()) {
                Optional<ManagedData> o = attributeData.getEmbeddedData().findParentManagedData(attributeName);
                if (o.isPresent())
                    return o;
            }
        }

        if (mappedSuperclass != null) {
            Optional<ManagedData> opt = mappedSuperclass.findParentManagedData(attributeName);
            if (opt.isPresent())
                return opt;
        }

        return Optional.empty();
    }
}
