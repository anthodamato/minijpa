package org.tinyjpa.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.tinyjpa.metadata.enhancer.EnhEntity;
import org.tinyjpa.metadata.enhancer.EnhEntityRegistry;
import org.tinyjpa.metadata.enhancer.javassist.AttributeData;
import org.tinyjpa.metadata.enhancer.javassist.ClassInspector;
import org.tinyjpa.metadata.enhancer.javassist.EntityEnhancer;
import org.tinyjpa.metadata.enhancer.javassist.ManagedData;

import javassist.ClassPool;
import javassist.CtClass;

public class EntityFileTransformer implements ClassFileTransformer {
	private ClassInspector classInspector = new ClassInspector();
	private List<ManagedData> inspectedClasses = new ArrayList<>();
	private List<EnhEntity> enhEntities = new ArrayList<>();
	private EntityEnhancer entityEnhancer = new EntityEnhancer();

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		if (className == null)
			return null;

		if (className.startsWith("java/") || className.startsWith("javax/") || className.startsWith("jdk/")
				|| className.startsWith("sun/") || className.startsWith("com/sun/") || className.startsWith("org/xml/")
				|| className.startsWith("org/junit/") || className.startsWith("org/apache/")
				|| className.startsWith("ch/qos/logback/") || className.startsWith("org/slf4j/")
				|| className.startsWith("javassist/") || className.startsWith("org/apiguardian/")
				|| className.startsWith("org/opentest4j/"))
			return null;

		System.out.println("transform: className=" + className);

		String fullClassName = className.replaceAll("/", ".");

		ClassPool cp = ClassPool.getDefault();
		CtClass ctClass;
		try {
			ctClass = cp.get(fullClassName);
			Object entity = ctClass.getAnnotation(Entity.class);
			Object mappedSuperclass = ctClass.getAnnotation(MappedSuperclass.class);
			Object embeddable = ctClass.getAnnotation(Embeddable.class);
			if (entity != null || mappedSuperclass != null || embeddable != null) {
				System.out.println("transform: EnhEntityRegistry.getInstance()=" + EnhEntityRegistry.getInstance());
				Optional<ManagedData> optionalMD = EnhEntityRegistry.getInstance().getManagedData(fullClassName);
				if (optionalMD.isPresent()) {
					System.out.println("transform: className=" + className + " found in the registry");
					return optionalMD.get().getCtClass().toBytecode();
				}

				System.out.println("transform: inspecting className=" + className);
				ManagedData managedData = classInspector.inspect(fullClassName, inspectedClasses);
				if (managedData == null)
					return null;

				EnhEntity enhEntity = enhance(managedData);
				EnhEntityRegistry.getInstance().add(enhEntity);
				EnhEntityRegistry.getInstance().add(managedData);
				if (managedData.mappedSuperclass != null)
					EnhEntityRegistry.getInstance().add(managedData.mappedSuperclass);

				for (AttributeData attributeData : managedData.getDataAttributes()) {
					if (attributeData.getEmbeddedData() != null)
						EnhEntityRegistry.getInstance().add(attributeData.getEmbeddedData());
				}

				System.out.println("transform: className=" + className + "; toByteCode");
				return managedData.getCtClass().toBytecode();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//		ClassPool cp = ClassPool.getDefault();
//		CtClass ctClass;
//		try {
//			ctClass = cp.get(curClassName);
//			Object entity = ctClass.getAnnotation(Entity.class);
//			if (entity != null) {
//				return ctClass.toBytecode();
//			}
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		return null;
	}

	private EnhEntity enhance(ManagedData managedData) throws Exception {
		EnhEntity enhMappedSuperclassEntity = null;
		if (managedData.mappedSuperclass != null) {
			enhMappedSuperclassEntity = entityEnhancer.enhance(managedData.mappedSuperclass, enhEntities);
		}

		EnhEntity enhEntity = entityEnhancer.enhance(managedData, enhEntities);
		enhEntity.setMappedSuperclass(enhMappedSuperclassEntity);
		enhEntities.add(enhEntity);

		return enhEntity;
	}

}
