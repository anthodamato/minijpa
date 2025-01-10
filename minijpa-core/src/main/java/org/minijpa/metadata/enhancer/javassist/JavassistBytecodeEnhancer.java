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

import java.util.*;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import javassist.Loader;
import org.minijpa.metadata.enhancer.BytecodeEnhancer;
import org.minijpa.metadata.enhancer.EnhAttribute;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.minijpa.metadata.enhancer.IdClassPropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;

public class JavassistBytecodeEnhancer implements BytecodeEnhancer {

    private final Logger log = LoggerFactory.getLogger(JavassistBytecodeEnhancer.class);

    private final ClassInspector classInspector = new ClassInspector();
    private final List<ManagedData> enhancedClasses = new ArrayList<>();
    private final Set<EnhEntity> parsedEntities = new HashSet<>();
    private final EntityEnhancer entityEnhancer = new EntityEnhancer();

    @Override
    public byte[] toBytecode(String className) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(className);

//        log.debug("toBytecode: className={}", className);
//        if (className.equals("org.minijpa.jpa.model.Guest"))
//            log.info("toBytecode: className={}", className);
//
//        if (className.equals("org.minijpa.jpa.model.GuestPk"))
//            log.info("toBytecode: className={}", className);

        Object entity = ctClass.getAnnotation(Entity.class);
        Object mappedSuperclass = ctClass.getAnnotation(MappedSuperclass.class);
        Object embeddable = ctClass.getAnnotation(Embeddable.class);
        if (entity == null && mappedSuperclass == null && embeddable == null)
            return null;

        Optional<ManagedData> optionalMD = enhancedClasses.stream()
                .filter(e -> e.getCtClass().getName().equals(className)).findFirst();
        if (optionalMD.isPresent()) {
            log.debug("toBytecode: className={} found in the registry", className);
            if (optionalMD.get().getCtClass().isFrozen())
                throw new IllegalStateException("Class '" + className + "' is frozen");

            return optionalMD.get().getCtClass().toBytecode();
        }

        log.debug("toBytecode: optionalMD={}", optionalMD);
        ManagedData managedData = classInspector.inspect(className);
        log.debug("toBytecode: managedData={}", managedData);
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
        if (className.equals("org.minijpa.jpa.model.Guest"))
            log.info("enhance: className={}", className);

        if (className.equals("org.minijpa.jpa.model.GuestPk"))
            log.info("enhance: className={}", className);

        Optional<EnhEntity> optionalEnhEntity = parsedEntities.stream()
                .filter(e -> e.getClassName().equals(className))
                .findFirst();
        if (optionalEnhEntity.isPresent()) {
            log.debug("enhance: className={} found in registry", className);
            EnhEntity enhEntity = optionalEnhEntity.get();
            parsedEntities.add(enhEntity);
            return optionalEnhEntity.get();
        }

        ManagedData managedData;
        Optional<ManagedData> optionalMD = enhancedClasses.stream()
                .filter(e -> e.getCtClass().getName().equals(className)).findFirst();
        log.debug("enhance: className={}; optionalMD.isPresent()={}", className, optionalMD.isPresent());
        if (optionalMD.isPresent())
            managedData = optionalMD.get();
        else
            managedData = classInspector.inspect(className);

        log.debug("enhance: className={}; managedData={}", className, managedData);
        EnhEntity enhEntity = entityEnhancer.enhance(managedData, parsedEntities);
        log.debug("enhance: className={}; enhEntity={}", className, enhEntity);
        log.debug("enhance: enhEntity.getEnhAttributes().size()={}", enhEntity.getEnhAttributes().size());
        parsedEntities.add(enhEntity);

        enhancedClasses.add(managedData);
        if (managedData.mappedSuperclass != null)
            enhancedClasses.add(managedData.mappedSuperclass);

        for (AttributeData attributeData : managedData.getAttributeDataList()) {
            if (attributeData.getEmbeddedData() != null)
                enhancedClasses.add(attributeData.getEmbeddedData());
        }

        enhEntity.getEnhAttributes().forEach(a ->
                log.debug("enhance: enhAttribute={}", a)
        );
        return enhEntity;
    }


    @Override
    public void finalizeEnhancement() throws Exception {
        Map<String, CtClass> idCtClasses = new HashMap<>();
        Map<String, IdClassPropertyData> idClassPropertyDataMap = new HashMap<>();
        for (EnhEntity enhEntity : parsedEntities) {
            if (enhEntity.getIdClassPropertyData() != null) {
                CtClass idCtClass = enhEntity.getIdClassPropertyData().getIdCtClass();
                idCtClasses.put(idCtClass.getName(), idCtClass);
                idClassPropertyDataMap.put(enhEntity.getIdClassPropertyData().getClassName(), enhEntity.getIdClassPropertyData());
            }
        }

        ClassPool pool = ClassPool.getDefault();
        Loader cl = new Loader(pool);
        String tmpdir = System.getProperty("java.io.tmpdir");
//        log.info("finalizeEnhancement: tmpdir={}", tmpdir);
        pool.insertClassPath(tmpdir);
        for (EnhEntity enhEntity : parsedEntities) {
            if (enhEntity.getIdClassPropertyData() == null)
                continue;

            IdClassPropertyData idClassPropertyData = enhEntity.getIdClassPropertyData();
            ManagedData managedData = idClassPropertyData.getIdClassManagedData();
            CtClass idCtClass = idClassPropertyData.getIdCtClass();
            List<EnhAttribute> enhAttributes = entityEnhancer.enhanceIdClassAttributesGetSet(managedData, idCtClass, idCtClasses);

            for (EnhAttribute enhAttribute : enhAttributes) {
                if (idClassPropertyDataMap.containsKey(enhAttribute.getClassName()))
                    idClassPropertyData.setNested(idClassPropertyDataMap.get(enhAttribute.getClassName()));
            }

            idCtClass.writeFile(tmpdir);
            Class<?> c = cl.loadClass(idCtClass.getName());
//            log.debug("finalizeEnhancement: c={}", c);
            idClassPropertyData.setClassType(c);
            idClassPropertyData.setEnhAttributes(enhAttributes);
        }
    }
}
