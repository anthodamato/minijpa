/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.metadata.enhancer.BytecodeEnhancer;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adamato
 */
public class MetaEntityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MetaEntityUtils.class);

    private static final BytecodeEnhancer bytecodeEnhancer = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer();

    public static MetaEntity parse(String className, JpaParser jpaParser, Collection<MetaEntity> parsedEntities) throws Exception {
        EnhEntity enhEntity = bytecodeEnhancer.enhance(className);
        return jpaParser.parse(enhEntity, parsedEntities);
    }

    private static Map<String, MetaEntity> parse(List<String> entities, JpaParser jpaParser) throws Exception {
        List<MetaEntity> metaEntities = new ArrayList<>();
        for (String className : entities) {
            MetaEntity metaEntity = parse(className, jpaParser, metaEntities);
            metaEntities.add(metaEntity);
        }

        Map<String, MetaEntity> map = new HashMap<>();
        metaEntities.forEach(e -> map.put(e.getEntityClass().getName(), e));

        jpaParser.fillRelationships(map);
        return map;
    }

    public static PersistenceUnitContext parsePersistenceUnitContext(
            String persistenceUnitName, List<String> entities) throws Exception {
        DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);
        JpaParser jpaParser = new JpaParser(dbConfiguration);
        LOG.debug("parsePersistenceUnitContext: parser={}", jpaParser);
        Map<String, MetaEntity> map = parse(entities, jpaParser);
        LOG.debug("parsePersistenceUnitContext: map={}", map);
        Optional<Map<String, QueryResultMapping>> optional = jpaParser.parseSqlResultSetMappings(map);
        Optional<Map<String, MiniNamedQueryMapping>> optionalNamedQueries = jpaParser.parseNamedQueries(map);
        LOG.debug("parsePersistenceUnitContext: namedQueries={}", optionalNamedQueries.orElse(null));
        return new PersistenceUnitContext(
                persistenceUnitName,
                map,
                optional.orElse(null),
                optionalNamedQueries.orElse(null));
    }

    //    private static void printEmbeddedAttribute(MetaAttribute m) {
//	LOG.info("printMetaEntity: Embedded " + m.toString());
//	List<MetaAttribute> embeddeds = m.getEmbeddableMetaEntity().getAttributes();
//	for (MetaAttribute a : embeddeds) {
//	    LOG.info("printMetaEntity: Embedded child " + a.toString());
//	}
//    }
    public static void printMetaEntity(MetaEntity metaEntity) {
        List<AbstractMetaAttribute> attributes = metaEntity.getAttributes();
        LOG.debug("printMetaEntity: Attributes");
        attributes.forEach(m -> {
            LOG.debug("printMetaEntity: {}", m.toString());
        });

        LOG.debug("printMetaEntity: Embeddables");
        for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
            printMetaEntity(embeddable);
        }

        LOG.debug("printMetaEntity: Relationship Attributes");
        List<RelationshipMetaAttribute> ras = metaEntity.getRelationshipAttributes();
        ras.forEach(m -> {
            LOG.debug("printMetaEntity: Relationship {}", m.toString());
        });
    }
}
