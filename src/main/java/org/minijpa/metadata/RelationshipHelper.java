/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.minijpa.jdbc.Cascade;

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
