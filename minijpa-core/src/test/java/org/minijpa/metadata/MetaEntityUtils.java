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
import org.minijpa.jpa.db.QueryResultMapping;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
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

	public static MetaEntity parse(String className, Parser parser, Collection<MetaEntity> parsedEntities) throws Exception {
		EnhEntity enhEntity = bytecodeEnhancer.enhance(className);
		return parser.parse(enhEntity, parsedEntities);
	}

	private static Map<String, MetaEntity> parse(List<String> entities, Parser parser) throws Exception {
		List<MetaEntity> metaEntities = new ArrayList<>();
		for (String className : entities) {
			MetaEntity metaEntity = parse(className, parser, metaEntities);
			metaEntities.add(metaEntity);
		}

		Map<String, MetaEntity> map = new HashMap<>();
		metaEntities.forEach(e -> map.put(e.getEntityClass().getName(), e));

		parser.fillRelationships(map);
		return map;
	}

	public static PersistenceUnitContext parsePersistenceUnitContext(
			String persistenceUnitName, List<String> entities) throws Exception {
		DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitName);
		Parser parser = new Parser(dbConfiguration);
		LOG.debug("parsePersistenceUnitContext: parser=" + parser);
		Map<String, MetaEntity> map = parse(entities, parser);
		LOG.debug("parsePersistenceUnitContext: map=" + map);
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
		List<AbstractMetaAttribute> attributes = metaEntity.getAttributes();
		LOG.debug("printMetaEntity: Attributes");
		attributes.stream().forEach(m -> {
			LOG.debug("printMetaEntity: " + m.toString());
		});

		LOG.debug("printMetaEntity: Embeddables");
		for (MetaEntity embeddable : metaEntity.getEmbeddables()) {
			printMetaEntity(embeddable);
		}

		LOG.debug("printMetaEntity: Relationship Attributes");
		List<RelationshipMetaAttribute> ras = metaEntity.getRelationshipAttributes();
		ras.stream().forEach(m -> {
			LOG.debug("printMetaEntity: Relationship " + m.toString());
		});
	}
}
