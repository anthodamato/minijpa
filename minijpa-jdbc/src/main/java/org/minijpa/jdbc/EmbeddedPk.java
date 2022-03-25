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
import java.util.List;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class EmbeddedPk implements Pk {

    private final MetaEntity entity;
    private final PkGeneration pkGeneration = new PkGeneration();

    public EmbeddedPk(MetaEntity entity) {
	this.entity = entity;
    }

    @Override
    public PkGeneration getPkGeneration() {
	return pkGeneration;
    }

    @Override
    public boolean isEmbedded() {
	return true;
    }

    @Override
    public boolean isComposite() {
	return entity.getAttributes().size() > 1;
    }

    @Override
    public MetaAttribute getAttribute() {
	return null;
    }

    @Override
    public List<MetaAttribute> getAttributes() {
	return entity.getAttributes();
    }

    @Override
    public Class<?> getType() {
	return entity.getEntityClass();
    }

    @Override
    public String getName() {
	return entity.getName();
    }

    @Override
    public Method getReadMethod() {
	return entity.getReadMethod();
    }

    @Override
    public Method getWriteMethod() {
	return entity.getWriteMethod();
    }

}
