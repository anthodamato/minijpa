package org.minijpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javassist.CtClass;

public class ManagedData {

    public static final int ENTITY = 0;
    public static final int EMBEDDABLE = 1;
    public static final int MAPPEDSUPERCLASS = 2;
    private String className;
    private CtClass ctClass;
    private final List<AttributeData> attributeDatas = new ArrayList<>();
    public ManagedData mappedSuperclass;
    private final List<ManagedData> embeddables = new ArrayList<>();
    int type = ENTITY;
    private final List<BMTMethodInfo> methodInfos = new ArrayList<>();
    private String modificationAttribute;

    public ManagedData() {
	super();
    }

    public ManagedData(int type) {
	super();
	this.type = type;
    }

    public int getType() {
	return type;
    }

    String getClassName() {
	return className;
    }

    void setClassName(String className) {
	this.className = className;
    }

    public CtClass getCtClass() {
	return ctClass;
    }

    void setCtClass(CtClass ctClass) {
	this.ctClass = ctClass;
    }

    public List<AttributeData> getAttributeDataList() {
	return attributeDatas;
    }

    void addAttributeDatas(List<AttributeData> datas) {
	attributeDatas.addAll(datas);
    }

    public List<ManagedData> getEmbeddables() {
	return embeddables;
    }

    List<BMTMethodInfo> getMethodInfos() {
	return methodInfos;
    }

    public String getModificationAttribute() {
	return modificationAttribute;
    }

    public void setModificationAttribute(String modificationAttribute) {
	this.modificationAttribute = modificationAttribute;
    }

    public Optional<AttributeData> findAttribute(String name) {
	Optional<AttributeData> optional = attributeDatas.stream()
		.filter(a -> a.property.ctField.getName().equals(name)).findFirst();
	if (optional.isPresent())
	    return optional;

	if (mappedSuperclass != null) {
	    Optional<AttributeData> opt = mappedSuperclass.findAttribute(name);
	    if (opt.isPresent())
		return opt;
	}

	return Optional.empty();
    }
}
