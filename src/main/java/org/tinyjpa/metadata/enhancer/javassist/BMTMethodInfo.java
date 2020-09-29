package org.tinyjpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.List;

import javassist.CtConstructor;
import javassist.CtMethod;

public class BMTMethodInfo {
	CtConstructor ctConstructor;
	CtMethod ctMethod;
	private List<BMTFieldInfo> bmtFieldInfos = new ArrayList<>();

	List<BMTFieldInfo> getBmtFieldInfos() {
		return bmtFieldInfos;
	}

	void addFieldInfos(List<BMTFieldInfo> fieldInfos) {
		bmtFieldInfos.addAll(fieldInfos);
	}
}
