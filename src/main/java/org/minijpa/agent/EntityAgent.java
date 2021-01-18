package org.minijpa.agent;

import java.lang.instrument.Instrumentation;

public class EntityAgent {
	public static void premain(String agentArgs, Instrumentation inst) {
//		System.out.println("EntityAgent: premain - agentArgs=" + agentArgs);
		final EntityFileTransformer transformer = new EntityFileTransformer();
		inst.addTransformer(transformer);
	}
}
