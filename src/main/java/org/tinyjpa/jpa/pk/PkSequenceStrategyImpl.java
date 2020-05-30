package org.tinyjpa.jpa.pk;

public class PkSequenceStrategyImpl implements PkSequenceStrategy {
	private String sequenceName;

	public PkSequenceStrategyImpl(String sequenceName) {
		super();
		this.sequenceName = sequenceName;
	}

//	@Override
//	public String getSequenceName() {
//		return sequenceName;
//	}

}
