package org.minijpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.List;

import javassist.CtConstructor;
import javassist.CtMethod;

public class BMTMethodInfo {

    private CtConstructor ctConstructor;
    private CtMethod ctMethod;
    private final List<BMTFieldInfo> bmtFieldInfos = new ArrayList<>();

    public List<BMTFieldInfo> getBmtFieldInfos() {
	return bmtFieldInfos;
    }

    public void addFieldInfos(List<BMTFieldInfo> fieldInfos) {
	bmtFieldInfos.addAll(fieldInfos);
    }

    public CtConstructor getCtConstructor() {
	return ctConstructor;
    }

    public void setCtConstructor(CtConstructor ctConstructor) {
	this.ctConstructor = ctConstructor;
    }

    public CtMethod getCtMethod() {
	return ctMethod;
    }

    public void setCtMethod(CtMethod ctMethod) {
	this.ctMethod = ctMethod;
    }
    
    
}
