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
package org.minijpa.metadata;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.minijpa.jpa.model.relationship.Cascade;

/**
 *
 * @author adamato
 */
public abstract class RelationshipHelper {

    private static Optional<String> evalMappedBy(String mappedBy) {
        if (mappedBy == null || mappedBy.isEmpty())
            return Optional.empty();

        return Optional.of(mappedBy);
    }

    public static Set<Cascade> getCascades(CascadeType[] cascadeTypes) {
        Set<Cascade> cascades = new HashSet<>();
        Optional<CascadeType> optional = Stream.of(cascadeTypes).filter(c -> c == CascadeType.ALL).findFirst();
        if (optional.isPresent()) {
            cascades.add(Cascade.ALL);
            return cascades;
        }

        for (CascadeType cascadeType : cascadeTypes) {
            if (cascadeType == CascadeType.DETACH)
                cascades.add(Cascade.DETACH);
            else if (cascadeType == CascadeType.MERGE)
                cascades.add(Cascade.MERGE);
            else if (cascadeType == CascadeType.PERSIST)
                cascades.add(Cascade.PERSIST);
            else if (cascadeType == CascadeType.REFRESH)
                cascades.add(Cascade.REFRESH);
            else if (cascadeType == CascadeType.REMOVE)
                cascades.add(Cascade.REMOVE);
        }

        return cascades;
    }

    public static Optional<String> getMappedBy(OneToOne oneToOne) {
        return evalMappedBy(oneToOne.mappedBy());
    }

    public static Optional<String> getMappedBy(OneToMany oneToMany) {
        return evalMappedBy(oneToMany.mappedBy());
    }

    public static Optional<String> getMappedBy(ManyToMany manyToMany) {
        return evalMappedBy(manyToMany.mappedBy());
    }

    public static Optional<String> getMappedBy(ManyToOne manyToOne) {
        return Optional.empty();
    }

}
