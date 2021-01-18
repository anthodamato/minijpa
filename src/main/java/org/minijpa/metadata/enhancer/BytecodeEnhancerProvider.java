package org.minijpa.metadata.enhancer;

import org.minijpa.metadata.enhancer.javassist.JavassistBytecodeEnhancer;

public class BytecodeEnhancerProvider {
	private static BytecodeEnhancerProvider bytecodeEnhancerProvider = new BytecodeEnhancerProvider();

	private BytecodeEnhancer bytecodeEnhancer = new JavassistBytecodeEnhancer();

	private BytecodeEnhancerProvider() {

	}

	public static BytecodeEnhancerProvider getInstance() {
		return bytecodeEnhancerProvider;
	}

	public BytecodeEnhancer getBytecodeEnhancer() {
		return bytecodeEnhancer;
	}

}
