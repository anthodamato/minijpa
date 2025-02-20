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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EnhEntity {

    private String className;
    private List<EnhAttribute> enhAttributes = new ArrayList<>();
    private EnhEntity mappedSuperclass;
    private String modificationAttributeGetMethod;
    private String lazyLoadedAttributeGetMethod;
    private String joinColumnPostponedUpdateAttributeGetMethod;
    private String lockTypeAttributeGetMethod;
    private String lockTypeAttributeSetMethod;
    private String entityStatusAttributeGetMethod;
    private String entityStatusAttributeSetMethod;
    private IdClassPropertyData idClassPropertyData;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<EnhAttribute> getEnhAttributes() {
        return enhAttributes;
    }

    public void setEnhAttributes(List<EnhAttribute> enhAttributes) {
        this.enhAttributes = enhAttributes;
    }

    public EnhEntity getMappedSuperclass() {
        return mappedSuperclass;
    }

    public void setMappedSuperclass(EnhEntity mappedSuperclass) {
        this.mappedSuperclass = mappedSuperclass;
    }

    public String getModificationAttributeGetMethod() {
        return modificationAttributeGetMethod;
    }

    public void setModificationAttributeGetMethod(String modificationAttributeGetMethod) {
        this.modificationAttributeGetMethod = modificationAttributeGetMethod;
    }

    public String getLazyLoadedAttributeGetMethod() {
        return lazyLoadedAttributeGetMethod;
    }

    public void setLazyLoadedAttributeGetMethod(String lazyLoadedAttributeGetMethod) {
        this.lazyLoadedAttributeGetMethod = lazyLoadedAttributeGetMethod;
    }

    public String getJoinColumnPostponedUpdateAttributeGetMethod() {
        return joinColumnPostponedUpdateAttributeGetMethod;
    }

    public void setJoinColumnPostponedUpdateAttributeGetMethod(
            String joinColumnPostponedUpdateAttributeGetMethod) {
        this.joinColumnPostponedUpdateAttributeGetMethod = joinColumnPostponedUpdateAttributeGetMethod;
    }

    public String getLockTypeAttributeGetMethod() {
        return lockTypeAttributeGetMethod;
    }

    public void setLockTypeAttributeGetMethod(String lockTypeAttributeGetMethod) {
        this.lockTypeAttributeGetMethod = lockTypeAttributeGetMethod;
    }

    public String getLockTypeAttributeSetMethod() {
        return lockTypeAttributeSetMethod;
    }

    public void setLockTypeAttributeSetMethod(String lockTypeAttributeSetMethod) {
        this.lockTypeAttributeSetMethod = lockTypeAttributeSetMethod;
    }

    public String getEntityStatusAttributeGetMethod() {
        return entityStatusAttributeGetMethod;
    }

    public void setEntityStatusAttributeGetMethod(String entityStatusAttributeGetMethod) {
        this.entityStatusAttributeGetMethod = entityStatusAttributeGetMethod;
    }

    public String getEntityStatusAttributeSetMethod() {
        return entityStatusAttributeSetMethod;
    }

    public void setEntityStatusAttributeSetMethod(String entityStatusAttributeSetMethod) {
        this.entityStatusAttributeSetMethod = entityStatusAttributeSetMethod;
    }

    public IdClassPropertyData getIdClassPropertyData() {
        return idClassPropertyData;
    }

    public void setIdClassPropertyData(IdClassPropertyData idClassPropertyData) {
        this.idClassPropertyData = idClassPropertyData;
    }

    public void findEmbeddables(Set<EnhEntity> embeddables) {
        for (EnhAttribute enhAttribute : enhAttributes) {
            if (enhAttribute.isEmbedded()) {
                EnhEntity enhEntity = enhAttribute.getEmbeddedEnhEntity();
                embeddables.add(enhEntity);

                enhEntity.findEmbeddables(embeddables);
            }
        }
    }

    public Optional<EnhAttribute> getAttribute(String name) {
        return enhAttributes.stream().filter(a -> a.getName().equals(name)).findFirst();
    }
}
