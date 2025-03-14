package org.minijpa.jpa.metamodel.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityMetadataProviderImpl implements EntityMetadataProvider {
    private static Logger LOG = LoggerFactory.getLogger(EntityMetadataProviderImpl.class);
    private EntityParser entityParser = new EntityParser();

    public EntityMetadataProviderImpl() {
        super();
    }

    @Override
    public List<EntityMetadata> build(List<String> classNames) throws Exception {
        Set<JpaEntity> parsedEntities = new HashSet<>();
        Map<String, JpaEntity> map = new HashMap<>();
        for (String className : classNames) {
            LOG.debug("Building Entity Metadata -> Parsing '{}'", className);
            JpaEntity jpaEntity = entityParser.parse(className, parsedEntities);
            parsedEntities.add(jpaEntity);
            map.put(jpaEntity.getClassName(), jpaEntity);
        }

        entityParser.finalizeRelationships(map);
        LOG.debug("Building Entity Metadata -> Parsing completed");

        Set<JpaEntity> allEntities = new HashSet<>();
        for (JpaEntity jpaEntity : map.values()) {
            allEntities.add(jpaEntity);
            allEntities.addAll(jpaEntity.getEmbeddables());
        }

        List<EntityMetadata> entityMetadatas = new ArrayList<>();
        for (JpaEntity jpaEntity : allEntities) {
            Optional<EntityMetadata> mappedSuperClass = Optional.empty();
            if (jpaEntity.getMappedSuperclass().isPresent()) {
                mappedSuperClass = Optional.of(build(jpaEntity.getMappedSuperclass().get()));
            }

            EntityMetadata entityMetadata = build(jpaEntity);
            entityMetadata.setMappedSuperclass(mappedSuperClass);
            entityMetadatas.add(entityMetadata);
        }

        return entityMetadatas;
    }

    private EntityMetadata build(JpaEntity jpaEntity) throws Exception {
        EntityMetadata entityMetadata = new EntityMetadata();
        String[] paths = PathUtils.buildPaths(jpaEntity.getClassName());
        entityMetadata.setPath(paths[0]);
        entityMetadata.setClassName(paths[1]);
        entityMetadata.setPackagePath(paths[2]);
        entityMetadata.setEntityClassName(paths[3]);

        for (JpaAttribute attribute : jpaEntity.getAttributes()) {
            if (attribute.getRelationship().isEmpty())
                entityMetadata.addAttribute(
                        new AttributeElement(attribute.getName(), AttributeType.SINGULAR, attribute.getType()));
            else {
                Optional<AttributeElement> optional = buildRelationshipAttributeElement(attribute);
                if (optional.isPresent())
                    entityMetadata.addAttribute(optional.get());
            }
        }

        for (JpaEntity entity : jpaEntity.getEmbeddables()) {
            entityMetadata.addAttribute(
                    new AttributeElement(entity.getName(), AttributeType.SINGULAR, entity.getType(), true));
        }

        return entityMetadata;
    }

    private Optional<AttributeElement> buildRelationshipAttributeElement(JpaAttribute attribute) {
        if (attribute.getRelationship().isEmpty())
            return Optional.empty();

        if (attribute.getRelationship().get() instanceof OneToOneJpaRelationship
                || attribute.getRelationship().get() instanceof ManyToOneJpaRelationship) {
            return Optional.of(new AttributeElement(attribute.getName(), AttributeType.SINGULAR,
                    attribute.getRelationship().get().getAttributeType().getType(), true));
        }

        if (attribute.getType() == Collection.class)
            return Optional.of(new AttributeElement(attribute.getName(), AttributeType.COLLECTION,
                    attribute.getRelationship().get().getTargetEntityClass(), true));

        if (attribute.getType() == List.class)
            return Optional.of(new AttributeElement(attribute.getName(), AttributeType.LIST,
                    attribute.getRelationship().get().getTargetEntityClass(), true));

        if (attribute.getType() == Set.class)
            return Optional.of(new AttributeElement(attribute.getName(), AttributeType.SET,
                    attribute.getRelationship().get().getTargetEntityClass(), true));

        if (attribute.getType() == Map.class)
            return Optional.of(new AttributeElement(attribute.getName(), AttributeType.MAP,
                    attribute.getRelationship().get().getTargetEntityClass(), true));

        return Optional.empty();
    }
}
