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

import java.util.List;
import java.util.Optional;

import javassist.CtField;

public class Property {

    private boolean id;
    private PropertyMethod getPropertyMethod;
    private PropertyMethod setPropertyMethod;
    private CtField ctField;
    private boolean embedded;
    private List<Property> embeddedProperties;
    private final Optional<RelationshipProperties> relationshipProperties;
    private boolean embeddedIdParent = false;

    public Property(boolean id, PropertyMethod getPropertyMethod, PropertyMethod setPropertyMethod, CtField ctField,
            boolean embedded, List<Property> embeddedProperties,
            Optional<RelationshipProperties> relationshipProperties) {
        super();
        this.id = id;
        this.getPropertyMethod = getPropertyMethod;
        this.setPropertyMethod = setPropertyMethod;
        this.ctField = ctField;
        this.embedded = embedded;
        this.embeddedProperties = embeddedProperties;
        this.relationshipProperties = relationshipProperties;
    }

    public boolean isId() {
        return id;
    }

    public PropertyMethod getGetPropertyMethod() {
        return getPropertyMethod;
    }

    public PropertyMethod getSetPropertyMethod() {
        return setPropertyMethod;
    }

    public CtField getCtField() {
        return ctField;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public List<Property> getEmbeddedProperties() {
        return embeddedProperties;
    }

    public Optional<RelationshipProperties> getRelationshipProperties() {
        return relationshipProperties;
    }

    public boolean isEmbeddedIdParent() {
        return embeddedIdParent;
    }

    public void setEmbeddedIdParent(boolean embeddedIdParent) {
        this.embeddedIdParent = embeddedIdParent;
    }

}
