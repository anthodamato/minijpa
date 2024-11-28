/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	private final List<ManagedData> enhancedClasses = new ArrayList<>();
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

		Optional<ManagedData> optionalMD = enhancedClasses.stream()
				.filter(e -> e.getCtClass().getName().equals(className)).findFirst();
		if (optionalMD.isPresent()) {
			LOG.debug("toBytecode: className={} found in the registry", className);
			if (optionalMD.get().getCtClass().isFrozen())
				throw new IllegalStateException("Class '" + className + "' is frozen");

			return optionalMD.get().getCtClass().toBytecode();
		}

		LOG.debug("toBytecode: className={}; optionalMD={}", className, optionalMD);
		ManagedData managedData = classInspector.inspect(className);
		LOG.debug("toBytecode: managedData={}", managedData);
		EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
		parsedEntities.add(enhEntity);
		if (managedData.mappedSuperclass != null)
			enhancedClasses.add(managedData.mappedSuperclass);

		enhancedClasses.add(managedData);
		for (AttributeData attributeData : managedData.getAttributeDataList()) {
			if (attributeData.getEmbeddedData() != null)
				enhancedClasses.add(attributeData.getEmbeddedData());
		}

		if (managedData.getCtClass().isFrozen())
			throw new IllegalStateException("Class '" + className + "' is frozen");

		return managedData.getCtClass().toBytecode();
	}

	@Override
	public EnhEntity enhance(String className) throws Exception {
		Optional<EnhEntity> optionalEnhEntity = parsedEntities.stream().filter(e -> e.getClassName().equals(className))
				.findFirst();
		if (optionalEnhEntity.isPresent()) {
			LOG.debug("enhance: className={} found in registry", className);
			EnhEntity enhEntity = optionalEnhEntity.get();
			parsedEntities.add(enhEntity);
			return optionalEnhEntity.get();
		} else {
			ManagedData managedData;
			Optional<ManagedData> optionalMD = enhancedClasses.stream()
					.filter(e -> e.getCtClass().getName().equals(className)).findFirst();
			LOG.debug("enhance: className={}; optionalMD.isPresent()=", className, optionalMD.isPresent());
			if (optionalMD.isPresent())
				managedData = optionalMD.get();
			else
				managedData = classInspector.inspect(className);

			LOG.debug("enhance: className={}; managedData={}", className, managedData);
			EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
			LOG.debug("enhance: className={}; enhEntity={}", className, enhEntity);
			parsedEntities.add(enhEntity);

			enhancedClasses.add(managedData);
			if (managedData.mappedSuperclass != null)
				enhancedClasses.add(managedData.mappedSuperclass);

			for (AttributeData attributeData : managedData.getAttributeDataList()) {
				if (attributeData.getEmbeddedData() != null)
					enhancedClasses.add(attributeData.getEmbeddedData());
			}

			return enhEntity;
		}
	}

}
