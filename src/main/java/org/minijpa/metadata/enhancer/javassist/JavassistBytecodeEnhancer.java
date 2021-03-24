package org.minijpa.metadata.enhancer.javassist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.minijpa.metadata.enhancer.BytecodeEnhancer;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;

public class JavassistBytecodeEnhancer implements BytecodeEnhancer {

    private final Logger LOG = LoggerFactory.getLogger(JavassistBytecodeEnhancer.class);

    private final ClassInspector classInspector = new ClassInspector();
    private final List<ManagedData> inspectedClasses = new ArrayList<>();
    private final Set<EnhEntity> parsedEntities = new HashSet<>();
    private final EntityEnhancer entityEnhancer = new EntityEnhancer();

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
	    LOG.debug("enhance: className=" + className + " found in the registry");
	    return optionalMD.get().getCtClass().toBytecode();
	}

	ManagedData managedData = classInspector.inspect(className, inspectedClasses);
	inspectedClasses.add(managedData);

	EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
	parsedEntities.add(enhEntity);
	if (managedData.mappedSuperclass != null)
	    inspectedClasses.add(managedData.mappedSuperclass);

	for (AttributeData attributeData : managedData.getAttributeDataList()) {
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
	    LOG.debug("enhance: className=" + className + " found in registry");
	    EnhEntity enhEntity = optionalEnhEntity.get();
	    parsedEntities.add(enhEntity);
	    return optionalEnhEntity.get();
	} else {
	    ManagedData managedData = null;
	    Optional<ManagedData> optionalMD = inspectedClasses.stream()
		    .filter(e -> e.getCtClass().getName().equals(className)).findFirst();
	    LOG.debug("enhance: className=" + className + "; optionalMD.isPresent()=" + optionalMD.isPresent());
	    if (optionalMD.isPresent())
		managedData = optionalMD.get();
	    else
		managedData = classInspector.inspect(className, inspectedClasses);

	    LOG.debug("enhance: className=" + className + "; managedData=" + managedData);
	    EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
	    LOG.debug("enhance: className=" + className + "; enhEntity=" + enhEntity);
	    parsedEntities.add(enhEntity);

	    inspectedClasses.add(managedData);
	    if (managedData.mappedSuperclass != null)
		inspectedClasses.add(managedData.mappedSuperclass);

	    for (AttributeData attributeData : managedData.getAttributeDataList()) {
		if (attributeData.getEmbeddedData() != null)
		    inspectedClasses.add(attributeData.getEmbeddedData());
	    }

	    return enhEntity;
	}
    }

}
