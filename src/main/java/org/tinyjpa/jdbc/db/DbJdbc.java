package org.tinyjpa.jdbc.db;

import org.tinyjpa.jdbc.MetaEntity;
import org.tinyjpa.jdbc.NameTranslator;
import org.tinyjpa.jdbc.PkGeneration;
import org.tinyjpa.jdbc.PkStrategy;

public interface DbJdbc {

	public PkStrategy findPkStrategy(PkGeneration generatedValue);

	/**
	 * Returns the statement to generate the next sequence value.
	 * 
	 * @param entity the entity metamodel
	 * @return the statement to generate the next sequence value
	 */
	public String sequenceNextValueStatement(MetaEntity entity);

	public NameTranslator getNameTranslator();

	public default String notEqualOperator() {
		return "<>";
	}

	public default String equalOperator() {
		return "=";
	}

	public default String orOperator() {
		return "OR";
	}

	public default String andOperator() {
		return "AND";
	}

	public default String notOperator() {
		return "NOT";
	}

	public default String isNullOperator() {
		return "IS NULL";
	}

	public default String notNullOperator() {
		return "IS NOT NULL";
	}

	public default String trueOperator() {
		return "= TRUE";
	}

	public default String falseOperator() {
		return "= FALSE";
	}

	public default String emptyConjunctionOperator() {
		return "1=1";
	}

	public default String emptyDisjunctionOperator() {
		return "1=2";
	}

	public default String greaterThanOperator() {
		return ">";
	}

	public default String lessThanOperator() {
		return "<";
	}

	public default String betweenOperator() {
		return "BETWEEN";
	}

	public default String likeOperator() {
		return "LIKE";
	}

}
