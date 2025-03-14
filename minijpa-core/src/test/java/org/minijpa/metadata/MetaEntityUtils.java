/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.DbConfigurationList;
import org.minijpa.jpa.db.namedquery.MiniNamedNativeQueryMapping;
import org.minijpa.jpa.db.namedquery.MiniNamedQueryMapping;
import org.minijpa.jpa.db.querymapping.QueryResultMapping;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.metadata.enhancer.BytecodeEnhancer;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        Map<String, MetaEntity> map = parse(entities, jpaParser);
        Optional<Map<String, QueryResultMapping>> optional = jpaParser.parseSqlResultSetMappings(map);
        Optional<Map<String, MiniNamedQueryMapping>> optionalNamedQueries = jpaParser.parseNamedQueries(map);
        Optional<Map<String, MiniNamedNativeQueryMapping>> optionalNamedNativeQueries = jpaParser.parseNamedNativeQueries(map);
        return new PersistenceUnitContext(
                persistenceUnitName,
                map,
                optional.orElse(null),
                optionalNamedQueries.orElse(null),
                optionalNamedNativeQueries.orElse(null));
    }

}
