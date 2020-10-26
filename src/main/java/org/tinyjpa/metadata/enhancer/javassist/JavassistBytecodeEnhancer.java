package org.tinyjpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.metadata.enhancer.BytecodeEnhancer;
import org.tinyjpa.metadata.enhancer.EnhEntity;

import javassist.ClassPool;
import javassist.CtClass;

public class JavassistBytecodeEnhancer implements BytecodeEnhancer {
	private Logger LOG = LoggerFactory.getLogger(JavassistBytecodeEnhancer.class);

	private ClassInspector classInspector = new ClassInspector();
	private List<ManagedData> inspectedClasses = new ArrayList<>();
	private Set<EnhEntity> parsedEntities = new HashSet<>();
	private EntityEnhancer entityEnhancer = new EntityEnhancer();

	@Override
	public byte[] toBytecode(String className) throws Exception {
		ClassPool cp = ClassPool.getDefault();
		CtClass ctClass = cp.get(className);
		Object entity = ctClass.getAnnotation(Entity.class);
		Object mappedSuperclass = ctClass.getAnnotation(MappedSuperclass.class);
		Object embeddable = ctClass.getAnnotation(Embeddable.class);
		if (entity == null && mappedSuperclass == null && embeddable == null)
			return null;

		Optional<ManagedData> optionalMD = inspectedClasses.stream()
				.filter(e -> e.getCtClass().getName().equals(className)).findFirst();
		if (optionalMD.isPresent()) {
			LOG.info("enhance: className=" + className + " found in the registry");
			return optionalMD.get().getCtClass().toBytecode();
		}

		ManagedData managedData = classInspector.inspect(className, inspectedClasses);
		inspectedClasses.add(managedData);

		EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
		parsedEntities.add(enhEntity);
		if (managedData.mappedSuperclass != null)
			inspectedClasses.add(managedData.mappedSuperclass);

		for (AttributeData attributeData : managedData.getDataAttributes()) {
			if (attributeData.getEmbeddedData() != null)
				inspectedClasses.add(attributeData.getEmbeddedData());
		}

		return managedData.getCtClass().toBytecode();
	}

	@Override
	public EnhEntity enhance(String className) throws Exception {
		Optional<EnhEntity> optionalEnhEntity = parsedEntities.stream().filter(e -> e.getClassName().equals(className))
				.findFirst();
		if (optionalEnhEntity.isPresent()) {
			LOG.info("enhance: className=" + className + " found in registry");
			EnhEntity enhEntity = optionalEnhEntity.get();
			parsedEntities.add(enhEntity);
			return optionalEnhEntity.get();
		} else {
			ManagedData managedData = classInspector.inspect(className, inspectedClasses);
			LOG.info("enhance: className=" + className + "; managedData=" + managedData);
			EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
			LOG.info("enhance: className=" + className + "; enhEntity=" + enhEntity);
			parsedEntities.add(enhEntity);

			inspectedClasses.add(managedData);
			if (managedData.mappedSuperclass != null)
				inspectedClasses.add(managedData.mappedSuperclass);

			for (AttributeData attributeData : managedData.getDataAttributes()) {
				if (attributeData.getEmbeddedData() != null)
					inspectedClasses.add(attributeData.getEmbeddedData());
			}

			return enhEntity;
		}
	}

}
