package org.minijpa.metadata.enhancer.javassist;

import java.util.Optional;

import javassist.CtMethod;

public class PropertyMethod {
	Optional<CtMethod> method = Optional.empty();
	boolean enhance = true;
	/**
	 * true if the method must be created.
	 */
	boolean add = false;

	public PropertyMethod() {
	}

	public PropertyMethod(Optional<CtMethod> method, boolean enhance) {
		this.method = method;
		this.enhance = enhance;
	}

}
