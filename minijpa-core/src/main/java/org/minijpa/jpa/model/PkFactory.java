package org.minijpa.jpa.model;

import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.PkGeneration;
import org.minijpa.jpa.db.PkGenerationType;
import org.minijpa.jpa.db.PkStrategy;
import org.minijpa.metadata.enhancer.IdClassPropertyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PkFactory {
    private final Logger log = LoggerFactory.getLogger(PkFactory.class);

    public Pk buildPk(
            DbConfiguration dbConfiguration,
            List<AbstractMetaAttribute> metaAttributes,
            IdClassPropertyData idClassPropertyData,
            List<MetaEntity> embeddables,
            String tableName,
            Class<?> c) throws Exception {
        metaAttributes.forEach(a -> log.debug("buildPk: a={}", a));
        List<AbstractMetaAttribute> idAttrs = metaAttributes.stream()
                .filter(a -> {
                    if (a instanceof MetaAttribute && ((MetaAttribute) a).isId())
                        return true;

                    if (a instanceof RelationshipMetaAttribute && ((RelationshipMetaAttribute) a).isId())
                        return true;

                    return false;
                })
                .collect(Collectors.toList());
        if (idAttrs.isEmpty()) {
            Optional<MetaEntity> optionalME = embeddables.stream().filter(MetaEntity::isEmbeddedId)
                    .findFirst();
            if (optionalME.isEmpty()) {
                throw new Exception("@Id or @EmbeddedId annotation not found: '" + c.getName() + "'");
            }

            return new EmbeddedPk(optionalME.get());
        }

        if (idAttrs.size() == 1) {
            Field field = c.getDeclaredField(idAttrs.get(0).getName());
            PkGeneration gv = buildPkGeneration(dbConfiguration, field);
            if (gv.getPkStrategy() == PkStrategy.SEQUENCE && gv.getPkSequenceGenerator() == null) {
                gv.setPkSequenceGenerator(generateDefaultSequenceGenerator(tableName));
            }

            return new BasicAttributePk((MetaAttribute) idAttrs.get(0), gv);
        }

        // IdClass case
        IdClass idClass = c.getAnnotation(IdClass.class);
        if (idClass == null)
            throw new Exception("More than one @Id annotation found: '" + c.getName() + "'");

        List<MetaAttribute> attributes = new ArrayList<>();
        RelationshipMetaAttribute relationshipMetaAttribute = null;
        for (AbstractMetaAttribute abstractMetaAttribute : idAttrs) {
            if (abstractMetaAttribute instanceof MetaAttribute)
                attributes.add((MetaAttribute) abstractMetaAttribute);
            else if (abstractMetaAttribute instanceof RelationshipMetaAttribute)
                relationshipMetaAttribute = (RelationshipMetaAttribute) abstractMetaAttribute;
        }

        Class<?> idEntityClass = idClassPropertyData.getClassType();
        return new IdClassPkImpl(attributes, idEntityClass, relationshipMetaAttribute, idClassPropertyData);
    }


    private PkGeneration buildPkGeneration(
            DbConfiguration dbConfiguration,
            Field field) {
        GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
        if (generatedValue == null) {
            PkStrategy pkStrategy = dbConfiguration.getDbJdbc().findPkStrategy(PkGenerationType.PLAIN);
            PkGeneration pkGeneration = new PkGeneration();
            pkGeneration.setPkStrategy(pkStrategy);
            return pkGeneration;
        }

        PkGenerationType pkGenerationType = decodePkGenerationType(generatedValue.strategy());
        PkStrategy pkStrategy = dbConfiguration.getDbJdbc().findPkStrategy(pkGenerationType);
        log.trace("Pk Generation Strategy {}", pkStrategy);
        PkGeneration pkGeneration = new PkGeneration();
        pkGeneration.setPkStrategy(pkStrategy);
        pkGeneration.setGenerator(generatedValue.generator());

        SequenceGenerator sequenceGenerator = field.getAnnotation(SequenceGenerator.class);
        if (pkStrategy == PkStrategy.SEQUENCE) {
            if (sequenceGenerator != null) {
                if (generatedValue.strategy() != GenerationType.SEQUENCE) {
                    throw new IllegalArgumentException("Generated Value Strategy must be 'SEQUENCE'");
                }

                if (sequenceGenerator.name() != null && generatedValue.generator() != null
                        && !sequenceGenerator.name().equals(generatedValue.generator())) {
                    throw new IllegalArgumentException(
                            "Generator '" + generatedValue.generator() + "' not found"); ///
                }

                PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
                pkSequenceGenerator.setSequenceName(sequenceGenerator.sequenceName());
                pkSequenceGenerator.setSchema(sequenceGenerator.schema());
                pkSequenceGenerator.setAllocationSize(sequenceGenerator.allocationSize());
                pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
                pkSequenceGenerator.setInitialValue(sequenceGenerator.initialValue());
                pkGeneration.setPkSequenceGenerator(pkSequenceGenerator);
            }
        }

        return pkGeneration;
    }

    private PkSequenceGenerator generateDefaultSequenceGenerator(String tableName) {
        PkSequenceGenerator pkSequenceGenerator = new PkSequenceGenerator();
        pkSequenceGenerator.setSequenceName(tableName.toUpperCase() + "_PK_SEQ");
//		pkSequenceGenerator.setSchema(sequenceGenerator.schema());
        pkSequenceGenerator.setAllocationSize(Integer.valueOf(1));
//		pkSequenceGenerator.setCatalog(sequenceGenerator.schema());
        pkSequenceGenerator.setInitialValue(1);
        return pkSequenceGenerator;
    }


    private PkGenerationType decodePkGenerationType(GenerationType generationType) {
        if (generationType == GenerationType.AUTO) {
            return PkGenerationType.AUTO;
        }

        if (generationType == GenerationType.IDENTITY) {
            return PkGenerationType.IDENTITY;
        }

        if (generationType == GenerationType.SEQUENCE) {
            return PkGenerationType.SEQUENCE;
        }

        if (generationType == GenerationType.TABLE) {
            return PkGenerationType.TABLE;
        }

        return null;
    }

}
