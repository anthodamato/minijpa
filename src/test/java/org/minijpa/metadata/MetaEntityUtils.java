/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.minijpa.jdbc.MetaAttribute;
import org.minijpa.jdbc.MetaEntity;
import org.minijpa.jdbc.QueryResultMapping;
import org.minijpa.jpa.db.ApacheDerbyConfiguration;
import org.minijpa.metadata.enhancer.BytecodeEnhancer;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.minijpa.metadata.enhancer.EnhEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author adamato
 */
public class MetaEntityUtils {

    private static final Logger LOG = LoggerFactory.getLogger(MetaEntityUtils.class);

    private static final BytecodeEnhancer bytecodeEnhancer = BytecodeEnhancerProvider.getInstance().getBytecodeEnhancer();
    private static final Parser parser = new Parser(new ApacheDerbyConfiguration());

    public static MetaEntity parse(String className) throws Exception {
	EnhEntity enhEntity = bytecodeEnhancer.enhance(className);
	List<MetaEntity> parsedEntities = new ArrayList<>();
	return parser.parse(enhEntity, parsedEntities);
    }

    private static Map<String, MetaEntity> parse(List<String> entities) throws Exception {
	Map<String, MetaEntity> map = new HashMap<>();
	for (String className : entities) {
	    map.put(className, parse(className));
	}

	parser.fillRelationships(map);
	return map;
    }

    public static PersistenceUnitContext parsePersistenceUnitContext(String persistenceUnitName, List<String> entities) throws Exception {
	Map<String, MetaEntity> map = parse(entities);
	Optional<Map<String, QueryResultMapping>> optional = parser.parseSqlResultSetMappings(map);
	return new PersistenceUnitContext(persistenceUnitName, map, optional);
    }

//    private static void printEmbeddedAttribute(MetaAttribute m) {
//	LOG.info("printMetaEntity: Embedded " + m.toString());
//	List<MetaAttribute> embeddeds = m.getEmbeddableMetaEntity().getAttributes();
//	for (MetaAttribute a : embeddeds) {
//	    LOG.info("printMetaEntity: Embedded child " + a.toString());
//	}
//    }
    public static void printMetaEntity(MetaEntity metaEntity) {
	List<MetaAttribute> attributes = metaEntity.getAttributes();
	LOG.info("printMetaEntity: Attributes");
	attributes.stream().forEach(m -> {
	    LOG.info("printMetaEntity: " + m.toString());
	});

	LOG.info("printMetaEntity: Embeddables");
	for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
	    printMetaEntity(embeddable);
	}

	LOG.info("printMetaEntity: Relationship Attributes");
	List<MetaAttribute> ras = metaEntity.getRelationshipAttributes();
	ras.stream().forEach(m -> {
	    LOG.info("printMetaEntity: Relationship " + m.toString());
	});
    }
}
