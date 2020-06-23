package org.tinyjpa.jdbc.relationship;

public class OneToOne {
	private String joinColumn;
	private String mappedBy;
	private FetchType fetchType = FetchType.EAGER;

	public OneToOne() {
		super();
	}

	public String getJoinColumn() {
		return joinColumn;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public FetchType getFetchType() {
		return fetchType;
	}

	public boolean isOwner() {
		return mappedBy == null || mappedBy.isEmpty();
	}

	public OneToOne copyWithJoinColumn(String joinColumnName) {
		OneToOne oto = new OneToOne();
		oto.joinColumn = joinColumnName;
		oto.mappedBy = mappedBy;
		oto.fetchType = fetchType;
		return oto;
	}

	@Override
	public String toString() {
		return OneToOne.class.getName() + ": joinColumn=" + joinColumn + "; mappedBy=" + mappedBy;
	}

	public static class Builder {
		private String joinColumn;
		private String mappedBy;
		private FetchType fetchType = FetchType.EAGER;

		public Builder() {
		}

		public Builder withJoinColumn(String joinColumn) {
			this.joinColumn = joinColumn;
			return this;
		}

		public Builder withMappedby(String mappedBy) {
			this.mappedBy = mappedBy;
			return this;
		}

		public Builder withFetchType(FetchType fetchType) {
			this.fetchType = fetchType;
			return this;
		}

		public OneToOne build() {
			OneToOne oneToOne = new OneToOne();
			oneToOne.joinColumn = joinColumn;
			oneToOne.mappedBy = mappedBy;
			oneToOne.fetchType = fetchType;
			return oneToOne;
		}
	}
}
