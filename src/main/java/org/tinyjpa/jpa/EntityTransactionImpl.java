package org.tinyjpa.jpa;

import javax.persistence.EntityTransaction;

import org.tinyjpa.metadata.EntityDelegate;

public class EntityTransactionImpl implements EntityTransaction {
	private PersistenceContext persistenceContext;

	public EntityTransactionImpl(PersistenceContext persistenceContext) {
		super();
		this.persistenceContext = persistenceContext;
	}

	@Override
	public void begin() {
//		abstractEntityManager.beginTransaction();
	}

	@Override
	public void commit() {
		boolean success = new PersistenceHelper().persist(EntityDelegate.getInstance().getChanges(),
				persistenceContext.getPersistenceUnitInfo());
	}

	@Override
	public void rollback() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRollbackOnly() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getRollbackOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

}
