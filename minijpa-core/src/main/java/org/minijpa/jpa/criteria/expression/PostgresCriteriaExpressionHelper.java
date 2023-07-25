package org.minijpa.jpa.criteria.expression;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.Parameter;
import javax.persistence.Query;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.expression.SqlBinaryExpression;
import org.minijpa.sql.model.expression.SqlBinaryExpressionImpl;
import org.minijpa.sql.model.expression.SqlExpressionOperator;
import org.minijpa.sql.model.function.Coalesce;
import org.minijpa.sql.model.function.Locate;
import org.minijpa.sql.model.function.Nullif;
import org.minijpa.sql.model.function.Substring;

public class PostgresCriteriaExpressionHelper extends CriteriaExpressionHelper {

    /**
     * Postgres doesn't support the position function with start index.
     */
    // Start index not supported by Postgres
    // select coalesce(nullif(position(searchdata0_.pattern in
    // substring(searchdata0_.name,
    // ?)),0)+?-1,0) as col_0_0_ from search_data searchdata0_ where
    // searchdata0_.average_value=7
    @Override
    protected Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            LocateExpression locateExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Optional<Value> optional = super.createSelectionValue(
                fromTable, aliasGenerator, locateExpression, parameterValues,
                parameters);
        if (optional.isPresent()) {
            Locate locate = (Locate) optional.get();
            if (locate.getPosition().isPresent()) {
                Value value = createLocateReplacement(locate);
                if (locateExpression.getFrom().isPresent()) {
                    // one more QueryParameter in case of ParameterExpression as it is used twice,
                    // in the substring and sum
                    createParameterFromExpression(parameterValues,
                            locateExpression.getFrom().get(), aliasGenerator, parameters,
                            "from", Types.INTEGER, Optional.empty());
                }

                return Optional.of(value);
            }
        }

        return optional;
    }

    private Value createLocateReplacement(Locate locate) {
        Substring substring = new Substring(locate.getInputString(), locate.getPosition().get());
        Locate locate2 = new Locate(locate.getSearchString(), substring);
        Nullif nullif = new Nullif(locate2, 0);
        SqlBinaryExpression sqlBinaryExpressionDiff = new SqlBinaryExpressionImpl(SqlExpressionOperator.DIFF,
                locate.getPosition().get(), 1);
        SqlBinaryExpression sqlBinaryExpressionSum = new SqlBinaryExpressionImpl(SqlExpressionOperator.SUM, nullif,
                sqlBinaryExpressionDiff);
        return new Coalesce(sqlBinaryExpressionSum, 0);
    }
}
