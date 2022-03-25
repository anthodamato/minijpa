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
package org.minijpa.jpa.criteria;

import java.util.List;

import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.Selection;

public class CompoundSelectionImpl<X> implements CompoundSelection<X> {

    private List<Selection<?>> selections;
    private Class<? extends X> javaType;

    public CompoundSelectionImpl(List<Selection<?>> selections, Class<? extends X> javaType) {
	super();
	this.selections = selections;
	this.javaType = javaType;
    }

    @Override
    public Selection<X> alias(String name) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isCompoundSelection() {
	return true;
    }

    @Override
    public List<Selection<?>> getCompoundSelectionItems() {
	return selections;
    }

    @Override
    public Class<? extends X> getJavaType() {
	return javaType;
    }

    @Override
    public String getAlias() {
	// TODO Auto-generated method stub
	return null;
    }

}
