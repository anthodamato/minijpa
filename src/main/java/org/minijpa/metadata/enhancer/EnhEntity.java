package org.minijpa.metadata.enhancer;

import java.util.ArrayList;
import java.util.List;

public class EnhEntity {
	private String className;
	private List<EnhAttribute> enhAttributes = new ArrayList<>();
	private EnhEntity mappedSuperclass;
	private List<EnhEntity> embeddables = new ArrayList<>();

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<EnhAttribute> getEnhAttributes() {
		return enhAttributes;
	}

	public void setEnhAttributes(List<EnhAttribute> enhAttributes) {
		this.enhAttributes = enhAttributes;
	}

	public EnhEntity getMappedSuperclass() {
		return mappedSuperclass;
	}

	public void setMappedSuperclass(EnhEntity mappedSuperclass) {
		this.mappedSuperclass = mappedSuperclass;
	}

	public List<EnhEntity> getEmbeddables() {
		return embeddables;
	}

	public void addEmbeddables(List<EnhEntity> embeddables) {
		this.embeddables.addAll(embeddables);
	}
}
