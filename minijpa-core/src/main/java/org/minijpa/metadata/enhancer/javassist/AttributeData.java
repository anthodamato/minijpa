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

public class AttributeData {

    private Property property;
    private boolean parentEmbeddedId = false;
    // only for embedded attributes
    private ManagedData embeddedData;

    public AttributeData(Property property, boolean parentIsEmbeddedId, ManagedData embeddedData) {
        super();
        this.property = property;
        this.parentEmbeddedId = parentIsEmbeddedId;
        this.embeddedData = embeddedData;
    }

    public ManagedData getEmbeddedData() {
        return embeddedData;
    }

    public Property getProperty() {
        return property;
    }

    public boolean isParentEmbeddedId() {
        return parentEmbeddedId;
    }

}
