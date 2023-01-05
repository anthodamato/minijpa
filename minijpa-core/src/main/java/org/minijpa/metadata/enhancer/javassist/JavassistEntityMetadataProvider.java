package org.minijpa.metadata.enhancer.javassist;

import java.io.File;
import java.util.List;

import org.minijpa.jpa.metamodel.generator.AttributeElement;
import org.minijpa.jpa.metamodel.generator.AttributeType;
import org.minijpa.jpa.metamodel.generator.EntityMetadata;
import org.minijpa.metadata.JavaTypes;
import org.minijpa.metadata.enhancer.EntityMetadataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CtClass;

public class JavassistEntityMetadataProvider implements EntityMetadataProvider {
    private Logger LOG = LoggerFactory.getLogger(JavassistEntityMetadataProvider.class);
    private final ClassInspector classInspector = new ClassInspector();

    @Override
    public EntityMetadata build(String className) throws Exception {
        LOG.info("build: className={}", className);
        ManagedData managedData = classInspector.inspect(className);
        EntityMetadata entityMetadata = new EntityMetadata();
        LOG.info("build: managedData.getCtClass().getURL()={}", managedData.getCtClass().getURL());
        String[] paths = buildPaths(className);
        entityMetadata.setPath(paths[0]);
        entityMetadata.setClassName(paths[1]);
        entityMetadata.setPackagePath(paths[2]);
        entityMetadata.setEntityClassName(paths[3]);
        List<AttributeData> attributeDatas = managedData.getAttributeDataList();
        for (AttributeData a : attributeDatas) {
            LOG.info("build: a.getProperty().getCtField().getName()={}", a.getProperty().getCtField().getName());
            LOG.info("build: a.getProperty().getCtField().getType().getName={}",
                    a.getProperty().getCtField().getType().getName());
            CtClass type = a.getProperty().getCtField().getType();
            Class<?> attributeClass = null;
            if (type.isPrimitive())
                attributeClass = JavaTypes.getClass(type.getName());
            else
                attributeClass = Class.forName(type.getName());

            LOG.info("build: attributeClass={}", attributeClass);
            AttributeElement attributeElement = new AttributeElement(a.getProperty().getCtField().getName(),
                    AttributeType.SINGULAR, attributeClass);
            entityMetadata.addAttribute(attributeElement);
        }

        return entityMetadata;
    }

    private String[] buildPaths(String classNamePath) {
        String[] sv = classNamePath.split("\\.");
        if (sv.length == 1) {
            String path = sv[0] + "_.java";
            String[] paths = { path, sv[0] + "_", "", sv[0] };
            return paths;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sv.length - 1; ++i) {
            sb.append(sv[i]);
            sb.append(File.separator);
        }

        sb.append(sv[sv.length - 1]);
        sb.append("_.java");
        String path = sb.toString();
        String className = sv[sv.length - 1] + "_";
        String packagePath = classNamePath.substring(0, classNamePath.lastIndexOf('.'));
        String[] paths = { path, className, packagePath, sv[sv.length - 1] };
        return paths;
    }

}
