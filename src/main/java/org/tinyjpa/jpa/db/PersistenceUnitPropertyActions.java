package org.tinyjpa.jpa.db;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyjpa.jdbc.ConnectionProviderImpl;
import org.tinyjpa.jdbc.ScriptRunner;

public class PersistenceUnitPropertyActions {
	private Logger LOG = LoggerFactory.getLogger(PersistenceUnitPropertyActions.class);

	private void runScript(String scriptPath, PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		String filePath = scriptPath;
		if (!scriptPath.startsWith("/"))
			filePath = "/" + filePath;

		File file = Paths.get(getClass().getResource(filePath).toURI()).toFile();
		Connection connection = null;
		try {
			connection = new ConnectionProviderImpl(persistenceUnitInfo).getConnection();
			new ScriptRunner().run(file, connection);
		} catch (Exception e) {
			LOG.error(e.getMessage());
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (Exception e2) {
					LOG.error(e2.getMessage());
				}
		}
	}

	public void analyzeCreateScripts(PersistenceUnitInfo persistenceUnitInfo) throws Exception {
		Properties properties = persistenceUnitInfo.getProperties();
		LOG.info("properties=" + properties);
		String action = (String) properties.get("javax.persistence.schema-generation.database.action");
		String createSource = (String) properties.get("javax.persistence.schema-generation.create-source");
		String createScriptSource = (String) properties.get("javax.persistence.schema-generation.create-script-source");
		String dropSource = (String) properties.get("javax.persistence.schema-generation.drop-source");
		String dropScriptSource = (String) properties.get("javax.persistence.schema-generation.drop-script-source");
		LOG.info("action=" + action);
		if (action == null || action.isEmpty())
			return;

		if (action.equals("create")) {
			if (createSource != null && createSource.equals("script")) {
				if (createScriptSource != null) {
					runScript(createScriptSource, persistenceUnitInfo);
				}
			}
		} else if (action.equals("drop-and-create")) {
			if (dropSource != null && dropSource.equals("script")) {
				if (dropScriptSource != null) {
					runScript(dropScriptSource, persistenceUnitInfo);
				}
			}

			if (createSource != null && createSource.equals("script")) {
				if (createScriptSource != null) {
					runScript(createScriptSource, persistenceUnitInfo);
				}
			}
		}
	}

}
