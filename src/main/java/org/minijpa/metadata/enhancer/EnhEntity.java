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
    private Optional<String> lockTypeAttributeGetMethod = Optional.empty();
    private Optional<String> lockTypeAttributeSetMethod = Optional.empty();

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

    public void findEmbeddables(Set<EnhEntity> embeddables) {
	for (EnhAttribute enhAttribute : enhAttributes) {
	    if (enhAttribute.isEmbedded()) {
		EnhEntity enhEntity = enhAttribute.getEmbeddedEnhEntity();
		embeddables.add(enhEntity);

		enhEntity.findEmbeddables(embeddables);
	    }
	}
    }
}
