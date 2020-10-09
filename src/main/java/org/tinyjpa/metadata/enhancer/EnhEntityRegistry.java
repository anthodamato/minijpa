package org.tinyjpa.metadata.enhancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.tinyjpa.metadata.enhancer.javassist.ManagedData;

public class EnhEntityRegistry {
	private static final EnhEntityRegistry enhEntityRegistry = new EnhEntityRegistry();
	private List<EnhEntity> enhEntities = new ArrayList<>();
	private Set<ManagedData> inspectedClasses = new HashSet<>();

	private EnhEntityRegistry() {

	}

	public static EnhEntityRegistry getInstance() {
		return enhEntityRegistry;
	}

	public void add(EnhEntity enhEntity) {
		enhEntities.add(enhEntity);
	}

	public List<EnhEntity> getEnhEntities() {
		return Collections.unmodifiableList(enhEntities);
	}

	public Optional<EnhEntity> getEnhEntity(String className) {
		return enhEntities.stream().filter(e -> e.getClassName().equals(className)).findFirst();
	}

	public Optional<ManagedData> getManagedData(String className) {
		return inspectedClasses.stream().filter(e -> e.getCtClass().getName().equals(className)).findFirst();
	}

	public void add(ManagedData managedData) {
		inspectedClasses.add(managedData);
	}

	public Set<ManagedData> getInspectedClasses() {
		return Collections.unmodifiableSet(inspectedClasses);
	}

}
