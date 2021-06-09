package org.minijpa.jpa.db;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;

import org.minijpa.jdbc.ScriptRunner;
import org.minijpa.jdbc.db.DbConfiguration;
import org.minijpa.jdbc.model.SqlDDLStatement;
import org.minijpa.jpa.PersistenceUnitContextManager;
import org.minijpa.metadata.PersistenceUnitContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceUnitPropertyActions {

    private Logger LOG = LoggerFactory.getLogger(PersistenceUnitPropertyActions.class);

    private void runScript(String scriptPath, PersistenceUnitInfo persistenceUnitInfo) throws IOException, SQLException, URISyntaxException {
	String filePath = scriptPath;
	if (!scriptPath.startsWith("/"))
	    filePath = "/" + filePath;

	File file = Paths.get(getClass().getResource(filePath).toURI()).toFile();
	ScriptRunner scriptRunner = new ScriptRunner();
	List<String> statements = scriptRunner.readStatements(file);
	runScript(statements, persistenceUnitInfo);
    }

    public void runScript(List<String> statements, PersistenceUnitInfo persistenceUnitInfo) throws SQLException {
	Connection connection = null;
	try {
	    connection = new ConnectionProviderImpl(persistenceUnitInfo).getConnection();
	    ScriptRunner scriptRunner = new ScriptRunner();
	    scriptRunner.runDDLStatements(statements, connection);
	} finally {
	    if (connection != null)
	    try {
		connection.close();
	    } catch (Exception e2) {
		LOG.error(e2.getMessage());
	    }
	}
    }

    public List<String> generateScriptFromMetadata(PersistenceUnitInfo persistenceUnitInfo) {
	try {
	    PersistenceUnitContext persistenceUnitContext = PersistenceUnitContextManager.getInstance().get(persistenceUnitInfo);

	    SqlStatementFactory sqlStatementFactory = new SqlStatementFactory();
	    List<SqlDDLStatement> sqlStatements = sqlStatementFactory.buildDDLStatements(persistenceUnitContext);

	    DbConfiguration dbConfiguration = DbConfigurationList.getInstance().getDbConfiguration(persistenceUnitContext.getPersistenceUnitName());
	    return dbConfiguration.getSqlStatementGenerator().export(sqlStatements);
	} catch (Exception ex) {
	    throw new IllegalStateException(ex.getMessage());
	}
    }

    public void analyzeCreateScripts(PersistenceUnitInfo persistenceUnitInfo) throws IOException, SQLException, URISyntaxException {
	Properties properties = persistenceUnitInfo.getProperties();
	LOG.debug("properties=" + properties);
	String action = (String) properties.get("javax.persistence.schema-generation.database.action");
	String createSource = (String) properties.get("javax.persistence.schema-generation.create-source");
	String createScriptSource = (String) properties.get("javax.persistence.schema-generation.create-script-source");
	String dropSource = (String) properties.get("javax.persistence.schema-generation.drop-source");
	String dropScriptSource = (String) properties.get("javax.persistence.schema-generation.drop-script-source");
	LOG.debug("action=" + action);
	if (action == null || action.isEmpty())
	    return;

	if (action.equals("create")) {
	    if (createSource != null) {
		if (createSource.equals("script")) {
		    if (createScriptSource != null)
			runScript(createScriptSource, persistenceUnitInfo);
		} else if (createSource.equals("metadata")) {
		    List<String> script = generateScriptFromMetadata(persistenceUnitInfo);
		    LOG.debug("script=" + script);
		    runScript(script, persistenceUnitInfo);
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
