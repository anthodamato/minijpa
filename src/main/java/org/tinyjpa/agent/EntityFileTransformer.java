package org.tinyjpa.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.enhancer.BytecodeEnhancer;
import org.tinyjpa.metadata.enhancer.BytecodeEnhancerProvider;

public class EntityFileTransformer implements ClassFileTransformer {
	private Logger LOG = LoggerFactory.getLogger(EntityFileTransformer.class);
	private BytecodeEnhancer bytecodeEnhancer = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer();
	private boolean log = false;

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className == null)
			return null;

		if (className.startsWith("java/") || className.startsWith("javax/") || className.startsWith("jdk/")
				|| className.startsWith("sun/") || className.startsWith("com/sun/") || className.startsWith("org/xml/")
				|| className.startsWith("org/junit/") || className.startsWith("org/apache/")
				|| className.startsWith("ch/qos/logback/") || className.startsWith("org/slf4j/")
				|| className.startsWith("javassist/") || className.startsWith("org/apiguardian/")
				|| className.startsWith("org/opentest4j/"))
			return null;

		if (log)
			LOG.info("transform: className=" + className);

		String fullClassName = className.replaceAll("/", ".");

		try {
			return bytecodeEnhancer.toBytecode(fullClassName);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return null;
	}

}
