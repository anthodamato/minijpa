package org.minijpa.metadata.enhancer;

import org.minijpa.jpa.metamodel.generator.EntityMetadata;

public interface EntityMetadataProvider {
    public EntityMetadata build(String className) throws Exception;
}
