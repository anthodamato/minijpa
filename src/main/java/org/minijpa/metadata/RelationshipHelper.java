/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.Optional;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

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
