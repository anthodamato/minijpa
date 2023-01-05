package org.minijpa.jpa.metamodel.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.minijpa.metadata.JavaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetamodelExporterImpl implements MetamodelExporter {
    private Logger LOG = LoggerFactory.getLogger(MetamodelExporterImpl.class);

    @Override
    public void export(EntityMetadata entityMetadata, String sourceBasePath) throws Exception {
        LOG.debug("export: sourceBasePath={}", sourceBasePath);
        LOG.debug("export: entityMetadata.getPath()={}", entityMetadata.getPath());
        LOG.debug("export: entityMetadata.getClassName()={}", entityMetadata.getClassName());
        LOG.debug("export: entityMetadata.getPackagePath()={}", entityMetadata.getPackagePath());
        String path = sourceBasePath.charAt(sourceBasePath.length() - 1) == File.separatorChar
                ? sourceBasePath + entityMetadata.getPath()
                : sourceBasePath + File.separator + entityMetadata.getPath();
        File file = new File(path);
        LOG.debug("export: file.getAbsolutePath()={}", file.getAbsolutePath());
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        if (entityMetadata.getPackagePath() != null && entityMetadata.getPackagePath().length() > 0) {
            writer.write("package " + entityMetadata.getPackagePath() + ";");
            writer.newLine();
            writer.newLine();
        }

        // non Jpa imports
        for (AttributeElement ae : entityMetadata.getAttributeElements()) {
            if (!ae.getType().isPrimitive() && !ae.getType().getCanonicalName().startsWith("java.lang")) {
                writer.write("import " + ae.getType().getCanonicalName() + ";");
                writer.newLine();
                writer.newLine();
            }
        }

        // Jpa imports
        Set<AttributeType> attributeTypes = new HashSet<>();
        for (AttributeElement ae : entityMetadata.getAttributeElements()) {
            if (attributeTypes.contains(ae.getAttributeType()))
                continue;

            if (ae.getAttributeType() == AttributeType.SINGULAR) {
                writer.write("import javax.persistence.metamodel.SingularAttribute;");
                attributeTypes.add(AttributeType.SINGULAR);
            } else if (ae.getAttributeType() == AttributeType.LIST) {
                writer.write("import javax.persistence.metamodel.ListAttribute;");
                attributeTypes.add(AttributeType.LIST);
            }

            writer.newLine();
        }

        writer.write("import javax.persistence.metamodel.StaticMetamodel;");
        writer.newLine();
        writer.newLine();
        writer.write("@StaticMetamodel(" + entityMetadata.getEntityClassName() + ".class" + ")");
        writer.newLine();
        writer.write("public class " + entityMetadata.getClassName() + " {");
        writer.newLine();
        for (AttributeElement ae : entityMetadata.getAttributeElements()) {
            String typeName = "";
            if (ae.getType().isPrimitive()) {
                Class<?> wc = JavaTypes.getWrapperClass(ae.getType().getName());
                String[] sp = wc.getName().split("\\.");
                typeName = sp[sp.length - 1];
            } else {
                String[] sp = ae.getType().getName().split("\\.");
                typeName = sp[sp.length - 1];
            }

            writer.write("    public static volatile SingularAttribute<" + entityMetadata.getEntityClassName() + ", "
                    + typeName + "> " + ae.getName() + ";");

            writer.newLine();
        }

        writer.write("}");
        writer.newLine();
        writer.newLine();

        writer.close();
    }

}
