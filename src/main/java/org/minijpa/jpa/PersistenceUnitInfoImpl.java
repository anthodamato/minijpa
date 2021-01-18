package org.minijpa.jpa;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
	private String name;
	private Properties properties = new Properties();
	private List<String> managedClassNames;
	private DataSource jtaDataSource;
	private DataSource nonJtaDataSource;

	public PersistenceUnitInfoImpl() {
		super();
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPersistenceUnitName() {
		return name;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public String getPersistenceProviderClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSource getJtaDataSource() {
		return jtaDataSource;
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return nonJtaDataSource;
	}

	@Override
	public List<String> getMappingFileNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<URL> getJarFileUrls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getPersistenceUnitRootUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getManagedClassNames() {
		return managedClassNames;
	}

	@Override
	public boolean excludeUnlistedClasses() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValidationMode getValidationMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
		// TODO Auto-generated method stub

	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	public static class Builder {
		private String name;
		private List<String> managedClassNames;
		private DataSource jtaDataSource;
		private DataSource nonJtaDataSource;

		public Builder() {
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withManagedClassNames(List<String> managedClassNames) {
			this.managedClassNames = managedClassNames;
			return this;
		}

		public Builder withJtaDataSource(DataSource jtaDataSource) {
			this.jtaDataSource = jtaDataSource;
			return this;
		}

		public Builder withNonJtaDataSource(DataSource nonJtaDataSource) {
			this.nonJtaDataSource = nonJtaDataSource;
			return this;
		}

		public PersistenceUnitInfo build() {
			PersistenceUnitInfoImpl impl = new PersistenceUnitInfoImpl();
			impl.name = name;
			impl.managedClassNames = managedClassNames;
			impl.jtaDataSource = jtaDataSource;
			impl.nonJtaDataSource = nonJtaDataSource;
			return impl;
		}
	}
}
