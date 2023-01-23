package org.minijpa.jpa.metamodel.generator;

public interface MetamodelExporter {
    public void export(EntityMetadata entityMetadata, String sourceBasePath) throws Exception;
}
