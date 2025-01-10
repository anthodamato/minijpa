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

import java.util.Optional;
import java.util.Set;

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

public final class ManyToOneRelationship extends Relationship {

    public ManyToOneRelationship() {
        super();
    }

    @Override
    public boolean isOwner() {
        return true;
    }

    @Override
    public boolean toOne() {
        return true;
    }

    @Override
    public String toString() {
        return ManyToOneRelationship.class.getName() + ": fetchType=" + getFetchType();
    }

    public static class Builder {

        private String joinColumnTable;
        private FetchType fetchType = FetchType.EAGER;
        private Set<Cascade> cascades;
        private MetaEntity owningEntity;
        private RelationshipMetaAttribute owningAttribute;
        private MetaEntity attributeType;
        private Optional<JoinColumnDataList> joinColumnDataList = Optional.empty();
        private Optional<JoinColumnMapping> joinColumnMapping = Optional.empty();
        private boolean id;

        public Builder() {
        }

        public Builder withJoinColumnTable(String joinColumnTable) {
            this.joinColumnTable = joinColumnTable;
            return this;
        }

        public Builder withFetchType(FetchType fetchType) {
            this.fetchType = fetchType;
            return this;
        }

        public Builder withCascades(Set<Cascade> cascades) {
            this.cascades = cascades;
            return this;
        }

        public Builder withOwningEntity(MetaEntity owningEntity) {
            this.owningEntity = owningEntity;
            return this;
        }

        public Builder withOwningAttribute(RelationshipMetaAttribute attribute) {
            this.owningAttribute = attribute;
            return this;
        }

        public Builder withAttributeType(MetaEntity attributeType) {
            this.attributeType = attributeType;
            return this;
        }

        public ManyToOneRelationship.Builder withJoinColumnDataList(Optional<JoinColumnDataList> joinColumnDataList) {
            this.joinColumnDataList = joinColumnDataList;
            return this;
        }

        public ManyToOneRelationship.Builder withJoinColumnMapping(Optional<JoinColumnMapping> joinColumnMapping) {
            this.joinColumnMapping = joinColumnMapping;
            return this;
        }

        public ManyToOneRelationship.Builder withId(boolean id) {
            this.id = id;
            return this;
        }

        public Builder with(ManyToOneRelationship manyToOne) {
            this.joinColumnTable = manyToOne.joinColumnTable;
            this.fetchType = manyToOne.fetchType;
            this.cascades = manyToOne.cascades;
            this.owningEntity = manyToOne.owningEntity;
            this.owningAttribute = manyToOne.owningAttribute;
            this.attributeType = manyToOne.attributeType;
            this.joinColumnDataList = manyToOne.joinColumnDataList;
            this.joinColumnMapping = manyToOne.joinColumnMapping;
            this.id = id;
            return this;
        }

        public ManyToOneRelationship build() {
            ManyToOneRelationship r = new ManyToOneRelationship();
            r.joinColumnTable = joinColumnTable;
            r.fetchType = fetchType;
            r.cascades = cascades;
            r.owningEntity = owningEntity;
            r.owningAttribute = owningAttribute;
            r.attributeType = attributeType;
            r.joinColumnDataList = joinColumnDataList;
            r.joinColumnMapping = joinColumnMapping;
            r.id = id;
            return r;
        }
    }
}
