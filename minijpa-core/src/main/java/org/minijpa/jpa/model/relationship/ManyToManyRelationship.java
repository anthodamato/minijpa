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

import org.minijpa.jdbc.relationship.JoinColumnDataList;
import org.minijpa.jdbc.relationship.JoinTableAttributes;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

import java.util.Set;

public final class ManyToManyRelationship extends ToManyRelationship {

    public ManyToManyRelationship() {
        super();
    }

    @Override
    public String toString() {
        return ManyToManyRelationship.class.getName() + ": mappedBy=" + mappedBy + "; fetchType=" + fetchType;
    }

    public static class Builder {

        private String joinColumnTable;
        private String mappedBy;
        private FetchType fetchType = FetchType.LAZY;
        private Set<Cascade> cascades;
        private MetaEntity owningEntity;
        private RelationshipMetaAttribute owningAttribute;
        private Class<?> collectionClass;
        private Class<?> targetEntityClass;
        private RelationshipMetaAttribute targetAttribute;
        private RelationshipJoinTable joinTable;
        private MetaEntity attributeType;
        private JoinTableAttributes joinTableAttributes;
        private JoinColumnDataList joinColumnDataList;
        private JoinColumnMapping joinColumnMapping;
        private boolean id;

        public Builder() {
        }

        public Builder withJoinColumnTable(String joinColumnTable) {
            this.joinColumnTable = joinColumnTable;
            return this;
        }

        public Builder withMappedBy(String mappedBy) {
            this.mappedBy = mappedBy;
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

        public Builder withCollectionClass(Class<?> collectionClass) {
            this.collectionClass = collectionClass;
            return this;
        }

        public Builder withTargetEntityClass(Class<?> targetEntityClass) {
            this.targetEntityClass = targetEntityClass;
            return this;
        }

        public Builder withTargetAttribute(RelationshipMetaAttribute targetAttribute) {
            this.targetAttribute = targetAttribute;
            return this;
        }

        public Builder withJoinTable(RelationshipJoinTable joinTable) {
            this.joinTable = joinTable;
            return this;
        }

        public Builder withAttributeType(MetaEntity attributeType) {
            this.attributeType = attributeType;
            return this;
        }

        public ManyToManyRelationship.Builder withJoinTableAttributes(JoinTableAttributes joinTableAttributes) {
            this.joinTableAttributes = joinTableAttributes;
            return this;
        }

        public Builder withJoinColumnDataList(JoinColumnDataList joinColumnDataList) {
            this.joinColumnDataList = joinColumnDataList;
            return this;
        }

        public ManyToManyRelationship.Builder withJoinColumnMapping(JoinColumnMapping joinColumnMapping) {
            this.joinColumnMapping = joinColumnMapping;
            return this;
        }

        public Builder withId(boolean id) {
            this.id = id;
            return this;
        }

        public Builder with(ManyToManyRelationship r) {
            this.joinColumnTable = r.joinColumnTable;
            this.mappedBy = r.mappedBy;
            this.fetchType = r.fetchType;
            this.cascades = r.cascades;
            this.owningEntity = r.owningEntity;
            this.owningAttribute = r.owningAttribute;
            this.collectionClass = r.collectionClass;
            this.targetEntityClass = r.targetEntityClass;
            this.targetAttribute = r.targetAttribute;
            this.joinTable = r.joinTable;
            this.attributeType = r.attributeType;
            this.joinTableAttributes = r.joinTableAttributes;
            this.joinColumnDataList = r.joinColumnDataList;
            this.joinColumnMapping = r.joinColumnMapping;
            this.id = r.id;
            return this;
        }

        public ManyToManyRelationship build() {
            ManyToManyRelationship r = new ManyToManyRelationship();
            r.joinColumnTable = joinColumnTable;
            r.mappedBy = mappedBy;
            r.fetchType = fetchType;
            r.cascades = cascades;
            r.owningEntity = owningEntity;
            r.owningAttribute = owningAttribute;
            r.collectionClass = collectionClass;
            r.targetEntityClass = targetEntityClass;
            r.targetAttribute = targetAttribute;
            r.joinTable = joinTable;
            r.attributeType = attributeType;
            r.joinTableAttributes = joinTableAttributes;
            r.joinColumnDataList = joinColumnDataList;
            r.joinColumnMapping = joinColumnMapping;
            r.id = id;
            return r;
        }
    }
}
