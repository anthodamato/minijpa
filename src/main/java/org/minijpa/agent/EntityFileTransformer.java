package org.minijpa.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.minijpa.metadata.enhancer.BytecodeEnhancer;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.NotFoundException;

public class EntityFileTransformer implements ClassFileTransformer {

    private final Logger LOG = LoggerFactory.getLogger(EntityFileTransformer.class);
    private final BytecodeEnhancer bytecodeEnhancer = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer();
    private boolean log = false;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
	    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
	if (className == null || className.startsWith("java/") || className.startsWith("javax/")
		|| className.startsWith("jdk/") || className.startsWith("sun/") || className.startsWith("com/sun/")
		|| className.startsWith("org/xml/") || className.startsWith("org/junit/")
		|| className.startsWith("org/apache/") || className.startsWith("ch/qos/logback/")
		|| className.startsWith("org/slf4j/") || className.startsWith("javassist/")
		|| className.startsWith("org/apiguardian/") || className.startsWith("org/opentest4j/")
		|| className.startsWith("org/springframework/") || className.startsWith("net/bytebuddy/")
		|| className.startsWith("org/mockito/") || className.startsWith("org/aspectj/")
		|| className.startsWith("org/w3c/") || className.startsWith("com/zaxxer/"))
	    return null;

	if (log)
	    LOG.debug("transform: className=" + className);

	if (log)
	    LOG.debug("transform: Thread.currentThread()=" + Thread.currentThread());

	String fullClassName = className.replaceAll("/", ".");

	try {
	    return bytecodeEnhancer.toBytecode(fullClassName);
	} catch (Exception e) {
	    if (e instanceof NotFoundException)
		return null;

	    LOG.error(e.getMessage());
	}

	return null;
    }

}
