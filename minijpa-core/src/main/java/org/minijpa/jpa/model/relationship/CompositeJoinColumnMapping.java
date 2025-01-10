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
package org.minijpa.jpa.model.relationship;

import java.util.List;

import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.Pk;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

/**
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class CompositeJoinColumnMapping implements JoinColumnMapping {

    private final List<JoinColumnAttribute> joinColumnAttributes;
    private final RelationshipMetaAttribute attribute;
    private final Pk pk;

    public CompositeJoinColumnMapping(
            List<JoinColumnAttribute> joinColumnAttributes,
            RelationshipMetaAttribute attribute,
            Pk pk) {
        this.joinColumnAttributes = joinColumnAttributes;
        this.attribute = attribute;
        this.pk = pk;
    }

    @Override
    public RelationshipMetaAttribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public int size() {
        return joinColumnAttributes.size();
    }

    @Override
    public JoinColumnAttribute get(int index) {
        return joinColumnAttributes.get(index);
    }

    @Override
    public JoinColumnAttribute get() {
        return null;
    }

    @Override
    public Pk getForeignKey() {
        return pk;
    }

    @Override
    public boolean isLazy() {
        return joinColumnAttributes.get(0).getAttribute().isLazy();
    }

    @Override
    public List<JoinColumnAttribute> getJoinColumnAttributes() {
        return joinColumnAttributes;
    }

}
