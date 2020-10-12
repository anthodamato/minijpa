package org.tinyjpa.metadata.enhancer;

public interface BytecodeEnhancer {
	public byte[] toBytecode(String className) throws Exception;

	public EnhEntity enhance(String className) throws Exception;
}
