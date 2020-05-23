package org.tinyjpa.agent;

import java.lang.instrument.Instrumentation;

public class EntityAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
		final EntityFileTransformer transformer = new EntityFileTransformer();
		inst.addTransformer(transformer);
	}
}
