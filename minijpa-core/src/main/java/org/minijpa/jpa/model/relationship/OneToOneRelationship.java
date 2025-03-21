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
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;

import java.util.Set;

public final class OneToOneRelationship extends Relationship {

    public OneToOneRelationship() {
        super();
    }

    @Override
    public boolean toOne() {
        return true;
    }

    @Override
    public boolean fromOne() {
        return true;
    }

    @Override
    public String toString() {
        return OneToOneRelationship.class.getName()
                + ": joinColumnTable=" + joinColumnTable + "; mappedBy=" + mappedBy + "; fetchType="
                + fetchType;
    }

    public static class Builder {

        private String joinColumnTable;
        private String mappedBy;
        private FetchType fetchType = FetchType.EAGER;
        private Set<Cascade> cascades;
        private MetaEntity owningEntity;
        private RelationshipMetaAttribute targetAttribute;
        private RelationshipMetaAttribute owningAttribute;
        private MetaEntity attributeType;
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

        public Builder withTargetAttribute(RelationshipMetaAttribute targetAttribute) {
            this.targetAttribute = targetAttribute;
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

        public Builder withJoinColumnDataList(JoinColumnDataList joinColumnDataList) {
            this.joinColumnDataList = joinColumnDataList;
            return this;
        }

        public Builder withJoinColumnMapping(JoinColumnMapping joinColumnMapping) {
            this.joinColumnMapping = joinColumnMapping;
            return this;
        }

        public Builder withId(boolean id) {
            this.id = id;
            return this;
        }

        public Builder with(OneToOneRelationship oneToOne) {
            this.joinColumnTable = oneToOne.joinColumnTable;
            this.mappedBy = oneToOne.mappedBy;
            this.fetchType = oneToOne.fetchType;
            this.cascades = oneToOne.cascades;
            this.owningEntity = oneToOne.owningEntity;
            this.owningAttribute = oneToOne.owningAttribute;
            this.targetAttribute = oneToOne.targetAttribute;
            this.attributeType = oneToOne.attributeType;
            this.joinColumnDataList = oneToOne.joinColumnDataList;
            this.joinColumnMapping = oneToOne.getJoinColumnMapping();
            this.id = oneToOne.id;
            return this;
        }

        public OneToOneRelationship build() {
            OneToOneRelationship r = new OneToOneRelationship();
            r.joinColumnTable = joinColumnTable;
            r.mappedBy = mappedBy;
            r.fetchType = fetchType;
            r.cascades = cascades;
            r.owningEntity = owningEntity;
            r.targetAttribute = targetAttribute;
            r.owningAttribute = owningAttribute;
            r.attributeType = attributeType;
            r.joinColumnDataList = joinColumnDataList;
            r.joinColumnMapping = joinColumnMapping;
            r.id = id;
            return r;
        }
    }
}
