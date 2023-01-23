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
    private Optional<String> lazyLoadedAttributeGetMethod = Optional.empty();
    private Optional<String> joinColumnPostponedUpdateAttributeGetMethod = Optional.empty();
    private Optional<String> lockTypeAttributeGetMethod = Optional.empty();
    private Optional<String> lockTypeAttributeSetMethod = Optional.empty();
    private Optional<String> entityStatusAttributeGetMethod = Optional.empty();
    private Optional<String> entityStatusAttributeSetMethod = Optional.empty();
//    private boolean embeddedId = false;

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

    public Optional<String> getLazyLoadedAttributeGetMethod() {
        return lazyLoadedAttributeGetMethod;
    }

    public void setLazyLoadedAttributeGetMethod(Optional<String> lazyLoadedAttributeGetMethod) {
        this.lazyLoadedAttributeGetMethod = lazyLoadedAttributeGetMethod;
    }

    public Optional<String> getJoinColumnPostponedUpdateAttributeGetMethod() {
        return joinColumnPostponedUpdateAttributeGetMethod;
    }

    public void setJoinColumnPostponedUpdateAttributeGetMethod(
            Optional<String> joinColumnPostponedUpdateAttributeGetMethod) {
        this.joinColumnPostponedUpdateAttributeGetMethod = joinColumnPostponedUpdateAttributeGetMethod;
    }

    public Optional<String> getLockTypeAttributeGetMethod() {
        return lockTypeAttributeGetMethod;
    }

    public void setLockTypeAttributeGetMethod(Optional<String> lockTypeAttributeGetMethod) {
        this.lockTypeAttributeGetMethod = lockTypeAttributeGetMethod;
    }

    public Optional<String> getLockTypeAttributeSetMethod() {
        return lockTypeAttributeSetMethod;
    }

    public void setLockTypeAttributeSetMethod(Optional<String> lockTypeAttributeSetMethod) {
        this.lockTypeAttributeSetMethod = lockTypeAttributeSetMethod;
    }

    public Optional<String> getEntityStatusAttributeGetMethod() {
        return entityStatusAttributeGetMethod;
    }

    public void setEntityStatusAttributeGetMethod(Optional<String> entityStatusAttributeGetMethod) {
        this.entityStatusAttributeGetMethod = entityStatusAttributeGetMethod;
    }

    public Optional<String> getEntityStatusAttributeSetMethod() {
        return entityStatusAttributeSetMethod;
    }

    public void setEntityStatusAttributeSetMethod(Optional<String> entityStatusAttributeSetMethod) {
        this.entityStatusAttributeSetMethod = entityStatusAttributeSetMethod;
    }

//    public boolean isEmbeddedId() {
//	return embeddedId;
//    }
//
//    public void setEmbeddedId(boolean embeddedId) {
//	this.embeddedId = embeddedId;
//    }
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
