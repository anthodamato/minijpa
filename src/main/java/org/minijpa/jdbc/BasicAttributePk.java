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
package org.minijpa.jdbc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class BasicAttributePk implements Pk {

    private final MetaAttribute attribute;
    private final PkGeneration pkGeneration;
    private final List<MetaAttribute> attributes;

    public BasicAttributePk(MetaAttribute attribute, PkGeneration pkGeneration) {
	this.attribute = attribute;
	this.pkGeneration = pkGeneration;
	this.attributes = Arrays.asList(attribute);
    }

    @Override
    public PkGeneration getPkGeneration() {
	return pkGeneration;
    }

    @Override
    public boolean isEmbedded() {
	return false;
    }

    @Override
    public boolean isComposite() {
	return false;
    }

    @Override
    public MetaAttribute getAttribute() {
	return attribute;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
	return attributes;
    }

    @Override
    public Class<?> getType() {
	return attribute.getType();
    }

    @Override
    public String getName() {
	return attribute.getName();
    }

    @Override
    public Method getReadMethod() {
	return attribute.getReadMethod();
    }

    @Override
    public Method getWriteMethod() {
	return attribute.getWriteMethod();
    }

}
