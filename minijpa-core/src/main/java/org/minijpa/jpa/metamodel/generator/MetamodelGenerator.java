package org.minijpa.jpa.metamodel.generator;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jpa.PersistenceProviderHelper;
import org.minijpa.metadata.enhancer.BytecodeEnhancerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetamodelGenerator {
    private static Logger LOG = LoggerFactory.getLogger(MetamodelGenerator.class);
    private MetamodelExporter metamodelExporter = new MetamodelExporterImpl();

    /**
     * @param args
     *             <ul>
     *             <li>1st param: persistence unit name</li>
     *             <li>2nd param: persistence xml file path</li>
     *             <li>3rd param: entity source file base path</li>
     *             </ul>
     */
    public static void main(String[] args) {
        LOG.info("Persistence Unit Name: {}", args[0]);
        LOG.info("Persistence Xml file path: {}", args[1]);
        try {
            new MetamodelGenerator().generate(args[0], args[1], args[2]);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public void generate(String persistenceUnitName, String persistenceXmlPath, String sourceBasePath)
            throws Exception {
        PersistenceUnitInfo persistenceUnitInfo = findPersistenceUnitInfo(persistenceUnitName, persistenceXmlPath);
        LOG.info("Parsing entity classes...");
        List<EntityMetadata> entityMetadatas = new ArrayList<>();
        for (String className : persistenceUnitInfo.getManagedClassNames()) {
            EntityMetadata entityMetadata = BytecodeEnhancerProvider.getInstance().getEntityMetadataProvider()
                    .build(className);
            entityMetadatas.add(entityMetadata);
        }

        LOG.info("Exporting metamodel...");
        for (EntityMetadata em : entityMetadatas) {
            metamodelExporter.export(em, sourceBasePath);
        }
    }

    private PersistenceUnitInfo findPersistenceUnitInfo(String persistenceUnitName, String persistenceXmlPath)
            throws Exception {
        PersistenceUnitInfo persistenceUnitInfo = null;
        LOG.info("findPersistenceUnitInfo: emName={}, path={}", persistenceUnitName, persistenceXmlPath);
        persistenceUnitInfo = new PersistenceProviderHelper().parseXml(persistenceXmlPath, persistenceUnitName, null);
        if (persistenceUnitInfo == null) {
            LOG.error("Persistence Unit '{}' not found", persistenceUnitName);
            return null;
        }

        return persistenceUnitInfo;
    }
}
