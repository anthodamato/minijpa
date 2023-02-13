package org.minijpa.jpa.metamodel.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
        LOG.debug("export: file.getParent()={}", file.getParent());
        if (file.getParent() != null)
            Files.createDirectories(Path.of(file.getParent()));

        LOG.debug("export: file.getAbsolutePath()={}", file.getAbsolutePath());
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        if (entityMetadata.getPackagePath() != null && entityMetadata.getPackagePath().length() > 0) {
            writer.write("package " + entityMetadata.getPackagePath() + ";");
            writer.newLine();
            writer.newLine();
        }

        // non Jpa imports
        for (AttributeElement ae : entityMetadata.getAttributeElements()) {
            LOG.debug("export: ae.getName()={}", ae.getName());
            LOG.debug("export: ae.getType()={}", ae.getType());
            if (ae.getType() != null && !ae.isRelationship() && !ae.getType().isPrimitive()
                    && !ae.getType().getCanonicalName().startsWith("java.lang")) {
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

            LOG.debug("export: ae.getAttributeType()={}", ae.getAttributeType());
            if (ae.getAttributeType() == AttributeType.SINGULAR) {
                writer.write("import javax.persistence.metamodel.SingularAttribute;");
                attributeTypes.add(AttributeType.SINGULAR);
            } else if (ae.getAttributeType() == AttributeType.LIST) {
                writer.write("import javax.persistence.metamodel.ListAttribute;");
                attributeTypes.add(AttributeType.LIST);
            } else if (ae.getAttributeType() == AttributeType.COLLECTION) {
                writer.write("import javax.persistence.metamodel.CollectionAttribute;");
                attributeTypes.add(AttributeType.COLLECTION);
            } else if (ae.getAttributeType() == AttributeType.SET) {
                writer.write("import javax.persistence.metamodel.SetAttribute;");
                attributeTypes.add(AttributeType.SET);
            } else if (ae.getAttributeType() == AttributeType.MAP) {
                writer.write("import javax.persistence.metamodel.MapAttribute;");
                attributeTypes.add(AttributeType.MAP);
            }

            writer.newLine();
        }

        writer.write("import javax.persistence.metamodel.StaticMetamodel;");
        LOG.debug("export: imports done");
        writer.newLine();
        writer.newLine();
        writer.write("@StaticMetamodel(" + entityMetadata.getEntityClassName() + ".class" + ")");
        writer.newLine();
        writer.write("public class " + entityMetadata.getClassName());
        if (entityMetadata.getMappedSuperclass().isPresent()) {
            writer.write(" extends " + entityMetadata.getMappedSuperclass().get().getPackagePath() + "."
                    + entityMetadata.getMappedSuperclass().get().getClassName());
        }

        writer.write(" {");
        writer.newLine();
        for (AttributeElement ae : entityMetadata.getAttributeElements()) {
            if (ae.getAttributeType() == AttributeType.SINGULAR) {
                writer.write(buildSingularAttribute(ae, entityMetadata.getEntityClassName()));
            } else if (ae.getAttributeType() == AttributeType.COLLECTION) {
                writer.write(buildCollectionAttribute(ae, entityMetadata.getEntityClassName()));
            } else if (ae.getAttributeType() == AttributeType.SET) {
                writer.write(buildSetAttribute(ae, entityMetadata.getEntityClassName()));
            } else if (ae.getAttributeType() == AttributeType.MAP) {
                writer.write(buildMapAttribute(ae, entityMetadata.getEntityClassName()));
            } else if (ae.getAttributeType() == AttributeType.LIST) {
                writer.write(buildListAttribute(ae, entityMetadata.getEntityClassName()));
            }

            writer.newLine();
        }

        writer.write("}");
        writer.newLine();
        writer.newLine();

        writer.close();
    }

    private String buildTypeName(AttributeElement ae) throws Exception {
        if (ae.getType().isPrimitive()) {
            Class<?> wc = JavaTypeUtils.getWrapperClass(ae.getType().getName());
            String[] sp = wc.getName().split("\\.");
            return sp[sp.length - 1];
        }

        String[] sp = ae.getType().getName().split("\\.");
        return sp[sp.length - 1];
    }

    private String buildSingularAttribute(AttributeElement ae, String entityClassName) throws Exception {
        String typeName = buildTypeName(ae);
        return "    public static volatile SingularAttribute<" + entityClassName + ", " + typeName + "> " + ae.getName()
                + ";";
    }

    private String buildCollectionAttribute(AttributeElement ae, String entityClassName) throws Exception {
        String typeName = buildTypeName(ae);
        return "    public static volatile CollectionAttribute<" + entityClassName + ", " + typeName + "> "
                + ae.getName() + ";";
    }

    private String buildSetAttribute(AttributeElement ae, String entityClassName) throws Exception {
        String typeName = buildTypeName(ae);
        return "    public static volatile SetAttribute<" + entityClassName + ", " + typeName + "> " + ae.getName()
                + ";";
    }

    private String buildMapAttribute(AttributeElement ae, String entityClassName) throws Exception {
        String typeName = buildTypeName(ae);
        return "    public static volatile MapAttribute<" + entityClassName + ", " + typeName + "> " + ae.getName()
                + ";";
    }

    private String buildListAttribute(AttributeElement ae, String entityClassName) throws Exception {
        String typeName = buildTypeName(ae);
        return "    public static volatile ListAttribute<" + entityClassName + ", " + typeName + "> " + ae.getName()
                + ";";
    }
}
