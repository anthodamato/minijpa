package org.minijpa.jpa.metamodel.generator;

import java.util.List;

public interface EntityMetadataProvider {
    public List<EntityMetadata> build(List<String> classNames) throws Exception;

}
