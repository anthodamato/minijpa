package org.minijpa.metadata.enhancer.javassist;

public class BMTFieldInfo {
	public static final int ASSIGNMENT = 0;
	int opType;
	// field name
	String name;
	// implementation class name
	String implementation;

	public BMTFieldInfo(int opType, String name, String implementation) {
		super();
		this.opType = opType;
		this.name = name;
		this.implementation = implementation;
	}

}
