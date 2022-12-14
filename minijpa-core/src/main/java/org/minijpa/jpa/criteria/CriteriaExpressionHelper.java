package org.minijpa.jpa.criteria;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.mapper.AttributeMapper;
import org.minijpa.jpa.jpql.AggregateFunctionType;
import org.minijpa.jpa.jpql.FunctionUtils;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.sql.model.Column;
import org.minijpa.sql.model.FromTable;
import org.minijpa.sql.model.TableColumn;
import org.minijpa.sql.model.Value;
import org.minijpa.sql.model.expression.SqlBinaryExpression;
import org.minijpa.sql.model.expression.SqlBinaryExpressionBuilder;
import org.minijpa.sql.model.expression.SqlExpressionOperator;
import org.minijpa.sql.model.function.Abs;
import org.minijpa.sql.model.function.Concat;
import org.minijpa.sql.model.function.Count;
import org.minijpa.sql.model.function.CurrentDate;
import org.minijpa.sql.model.function.CurrentTime;
import org.minijpa.sql.model.function.CurrentTimestamp;
import org.minijpa.sql.model.function.Locate;
import org.minijpa.sql.model.function.Lower;
import org.minijpa.sql.model.function.Mod;
import org.minijpa.sql.model.function.Sqrt;
import org.minijpa.sql.model.function.Trim;
import org.minijpa.sql.model.function.TrimType;
import org.minijpa.sql.model.function.Upper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriteriaExpressionHelper {
    private Logger LOG = LoggerFactory.getLogger(CriteriaExpressionHelper.class);

    private TableColumn createTableColumnFromPath(AttributePath<?> attributePath, AliasGenerator tableAliasGenerator) {
        MetaAttribute attribute = attributePath.getMetaAttribute();
        return new TableColumn(
                FromTable.of(attributePath.getMetaEntity().getTableName(),
                        tableAliasGenerator.getDefault(attributePath.getMetaEntity().getTableName())),
                new Column(attribute.getColumnName()));
    }

    private AggregateFunctionType getAggregateFunction(
            org.minijpa.jpa.criteria.AggregateFunctionType aggregateFunctionType) {
        switch (aggregateFunctionType) {
        case AVG:
            return AggregateFunctionType.AVG;
        case MAX:
            return AggregateFunctionType.MAX;
        case MIN:
            return AggregateFunctionType.MIN;
        case COUNT:
            return AggregateFunctionType.COUNT;
        case SUM:
            return AggregateFunctionType.SUM;
        default:
            break;
        }

        throw new IllegalArgumentException(
                "Unknown aggregate function type for predicate type: " + aggregateFunctionType);
    }

    private SqlExpressionOperator getSqlExpressionOperator(ExpressionOperator expressionOperator) {
        switch (expressionOperator) {
        case DIFF:
            return SqlExpressionOperator.DIFF;
        case MINUS:
            return SqlExpressionOperator.MINUS;
        case PROD:
            return SqlExpressionOperator.PROD;
        case QUOT:
            return SqlExpressionOperator.QUOT;
        case SUM:
            return SqlExpressionOperator.SUM;
        default:
            break;
        }

        throw new IllegalArgumentException("Unknown operator for expression type: " + expressionOperator);
    }

    public QueryParameter createQueryParameterForParameterExpression(Query query,
            ParameterExpression<?> parameterExpression, String columnName, Integer sqlType,
            Optional<AttributeMapper> attributeMapper) {
        Object value = null;
        if (parameterExpression.getName() != null)
            value = query.getParameterValue(parameterExpression.getName());
        else if (parameterExpression.getPosition() != null)
            value = query.getParameter(parameterExpression.getPosition());

        return new QueryParameter(columnName, value, sqlType, attributeMapper);
    }

    public Object createParameterFromExpression(Query query, Expression<?> expression, AliasGenerator aliasGenerator,
            List<QueryParameter> parameters, String columnName, Integer sqlType,
            Optional<AttributeMapper> attributeMapper) {
        if (expression instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression;
            return createTableColumnFromPath(attributePath, aliasGenerator);
        }

        if (expression instanceof ParameterExpression<?>) {
            ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression;
            parameters.add(createQueryParameterForParameterExpression(query, parameterExpression, columnName, sqlType,
                    attributeMapper));
            return CriteriaUtils.QM;
        }

        return null;
    }

    protected Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            LocateExpression locateExpression, Query query, List<QueryParameter> parameters) {
        Locate.Builder builder = new Locate.Builder();
        Object xParam = createParameterFromExpression(query, locateExpression.getX(), aliasGenerator, parameters,
                "locate", Types.VARCHAR, Optional.empty());
        builder.withInputString(xParam);

        if (locateExpression.getPattern().isPresent()) {
            Object patternParam = createParameterFromExpression(query, locateExpression.getPattern().get(),
                    aliasGenerator, parameters, "locate", Types.VARCHAR, Optional.empty());
            builder.withSearchString(patternParam);
        }

        if (locateExpression.getPatternString().isPresent()) {
            builder.withSearchString(CriteriaUtils.buildValue(locateExpression.getPatternString().get()));
        }

        if (locateExpression.getFrom().isPresent()) {
            Object fromParam = createParameterFromExpression(query, locateExpression.getFrom().get(), aliasGenerator,
                    parameters, "from", Types.INTEGER, Optional.empty());
            builder.withPosition(Optional.of(fromParam));
        }

        if (locateExpression.getFromInteger().isPresent()) {
            builder.withPosition(Optional.of(locateExpression.getFromInteger().get()));
        }

        return Optional.of(builder.build());
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            ConcatExpression concatExpression) {
        List<Object> values = new ArrayList<>();
        if (concatExpression.getX().isPresent()) {
            AttributePath<?> attributePath = (AttributePath<?>) concatExpression.getX().get();
            values.add(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (concatExpression.getxValue().isPresent())
            values.add(CriteriaUtils.buildValue(concatExpression.getxValue().get()));

        if (concatExpression.getY().isPresent()) {
            AttributePath<?> attributePath = (AttributePath<?>) concatExpression.getY().get();
            values.add(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (concatExpression.getyValue().isPresent())
            values.add(CriteriaUtils.buildValue(concatExpression.getyValue().get()));

        return Optional.of(new Concat(values.toArray()));
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            TrimExpression trimExpression, Query query, List<QueryParameter> parameters) {
        Trim.Builder builder = new Trim.Builder();
        builder.withArgument(createParameterFromExpression(query, trimExpression.getX(), aliasGenerator, parameters,
                "locate", Types.VARCHAR, Optional.empty()));

        if (trimExpression.getT().isPresent()) {
            LOG.debug("createSelectionValue: trimExpression.getT().get()={}", trimExpression.getT().get());
            builder.withTrimCharacter((String) createParameterFromExpression(query, trimExpression.getT().get(),
                    aliasGenerator, parameters, "trim", Types.VARCHAR, Optional.empty()));
        }

        if (trimExpression.gettChar().isPresent()) {
            LOG.debug("createSelectionValue: trimExpression.gettChar().get()={}", trimExpression.gettChar().get());
            builder.withTrimCharacter(CriteriaUtils.buildValue("" + trimExpression.gettChar().get()));
        }

        if (trimExpression.getTs().isPresent()) {
            Trimspec trimspec = trimExpression.getTs().get();
            if (trimspec == Trimspec.TRAILING)
                builder.withTrimType(Optional.of(TrimType.TRAILING));
            else if (trimspec == Trimspec.LEADING)
                builder.withTrimType(Optional.of(TrimType.LEADING));
            else if (trimspec == Trimspec.BOTH)
                builder.withTrimType(Optional.of(TrimType.BOTH));
        }

        return Optional.of(builder.build());
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            UnaryExpression<?> unaryExpression, Query query, List<QueryParameter> parameters) {
        Optional<Value> optional = createSelectionValue(fromTable, aliasGenerator, unaryExpression.getExpression(),
                query, parameters);
        if (unaryExpression.getExpressionOperator() == ExpressionOperator.ABS) {
            return Optional.of(new Abs(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.SQRT) {
            return Optional.of(new Sqrt(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.LOWER) {
            return Optional.of(new Lower(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.UPPER) {
            return Optional.of(new Upper(optional.get()));
        }

        return Optional.empty();
    }

    private List<Object> createSelectionValuesFromExpression(FromTable fromTable, AliasGenerator aliasGenerator,
            BinaryExpression<?> binaryExpression, Integer sqlType, Query query, List<QueryParameter> parameters) {
        List<Object> list = new ArrayList<>();
        if (binaryExpression.getX().isPresent()) {
            list.add(createParameterFromExpression(query, binaryExpression.getX().get(), aliasGenerator, parameters,
                    "locate", sqlType, Optional.empty()));
        }

        if (binaryExpression.getxValue().isPresent()) {
            list.add(CriteriaUtils.buildValue(binaryExpression.getxValue().get()));
        }

        if (binaryExpression.getY().isPresent()) {
            list.add(createParameterFromExpression(query, binaryExpression.getY().get(), aliasGenerator, parameters,
                    "locate", sqlType, Optional.empty()));
        }

        if (binaryExpression.getyValue().isPresent()) {
            list.add(CriteriaUtils.buildValue(binaryExpression.getyValue().get()));
        }

        return list;
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            BinaryExpression<?> binaryExpression, Query query, List<QueryParameter> parameters) {
        if (binaryExpression.getExpressionOperator() == ExpressionOperator.MOD) {
            List<Object> list = createSelectionValuesFromExpression(fromTable, aliasGenerator, binaryExpression,
                    Types.INTEGER, query, parameters);
            return Optional.of(new Mod(list.get(0), list.get(1)));
        }

        SqlBinaryExpressionBuilder builder = new SqlBinaryExpressionBuilder(
                getSqlExpressionOperator(binaryExpression.getExpressionOperator()));
        if (binaryExpression.getX().isPresent()) {
            AttributePath<?> attributePath = (AttributePath<?>) binaryExpression.getX().get();
            builder.setLeftExpression(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (binaryExpression.getxValue().isPresent())
            if (CriteriaUtils.requireQM(binaryExpression.getxValue().get()))
                builder.setLeftExpression(CriteriaUtils.QM);
            else
                builder.setLeftExpression(CriteriaUtils.buildValue(binaryExpression.getxValue().get()));

        if (binaryExpression.getY().isPresent()) {
            AttributePath<?> attributePath = (AttributePath<?>) binaryExpression.getY().get();
            builder.setRightExpression(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (binaryExpression.getyValue().isPresent())
            if (CriteriaUtils.requireQM(binaryExpression.getyValue().get()))
                builder.setRightExpression(CriteriaUtils.QM);
            else
                builder.setRightExpression(CriteriaUtils.buildValue(binaryExpression.getyValue().get()));

        SqlBinaryExpression sqlBinaryExpression = builder.build();
        LOG.debug("createSelectionValue: sqlBinaryExpression={}", sqlBinaryExpression);
        return Optional.of((Value) sqlBinaryExpression);
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            AggregateFunctionExpression<?> aggregateFunctionExpression) {
        Expression<?> expr = aggregateFunctionExpression.getX();
        if (aggregateFunctionExpression
                .getAggregateFunctionType() == org.minijpa.jpa.criteria.AggregateFunctionType.COUNT) {
            if (expr instanceof AttributePath<?>) {
                AttributePath<?> miniPath = (AttributePath<?>) expr;
                MetaAttribute metaAttribute = miniPath.getMetaAttribute();
                return Optional.of(new Count(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())),
                        aggregateFunctionExpression.isDistinct()));
            } else if (expr instanceof MiniRoot<?>) {
                MiniRoot<?> miniRoot = (MiniRoot<?>) expr;
                MetaEntity metaEntity = miniRoot.getMetaEntity();
                List<MetaAttribute> idAttrs = metaEntity.getId().getAttributes();
                return Optional.of(new Count(new TableColumn(
                        FromTable.of(metaEntity.getTableName(), aliasGenerator.getDefault(metaEntity.getTableName())),
                        new Column(idAttrs.get(0).getColumnName())), aggregateFunctionExpression.isDistinct()));
            }
        } else if (expr instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) expr;
            MetaAttribute metaAttribute = miniPath.getMetaAttribute();
            Value value = FunctionUtils.createAggregateFunction(
                    getAggregateFunction(aggregateFunctionExpression.getAggregateFunctionType()),
                    new TableColumn(fromTable, new Column(metaAttribute.getColumnName())), false);
            return Optional.of(value);
        }

        return Optional.empty();
    }

    private Optional<Value> createSelectionValue(FromTable fromTable, AliasGenerator aliasGenerator,
            Selection<?> selection, Query query, List<QueryParameter> parameters) {
        if (selection == null)
            return Optional.empty();

        if (selection instanceof TypecastExpression)
            return createSelectionValue(fromTable, aliasGenerator, ((TypecastExpression<?>) selection).getExpression(),
                    query, parameters);

        LOG.debug("createSelectionValue: selection={}", selection);
        if (selection instanceof AttributePath<?>) {
            AttributePath<?> miniPath = (AttributePath<?>) selection;
            MetaAttribute metaAttribute = miniPath.getMetaAttribute();
            return Optional.of(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())));
        } else if (selection instanceof AggregateFunctionExpression<?>) {
            return createSelectionValue(fromTable, aliasGenerator, (AggregateFunctionExpression<?>) selection);
        } else if (selection instanceof BinaryExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (BinaryExpression<?>) selection, query, parameters);
        } else if (selection instanceof UnaryExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (UnaryExpression<?>) selection, query, parameters);
        } else if (selection instanceof ConcatExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (ConcatExpression) selection);
        } else if (selection instanceof TrimExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (TrimExpression) selection, query, parameters);
        } else if (selection instanceof LocateExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (LocateExpression) selection, query, parameters);
        } else if (selection instanceof CurrentDateExpression) {
            return Optional.of(new CurrentDate());
        } else if (selection instanceof CurrentTimeExpression) {
            return Optional.of(new CurrentTime());
        } else if (selection instanceof CurrentTimestampExpression) {
            return Optional.of(new CurrentTimestamp());
        }

        return Optional.empty();
    }

    public List<Value> createSelectionValues(FromTable fromTable, AliasGenerator aliasGenerator, Selection<?> selection,
            Query query, List<QueryParameter> parameters) {
        if (selection == null)
            return Collections.emptyList();

        List<Value> values = new ArrayList<>();
        if (selection.isCompoundSelection()) {
            List<Selection<?>> selections = selection.getCompoundSelectionItems();
            for (Selection<?> s : selections) {
                Optional<Value> optional = createSelectionValue(fromTable, aliasGenerator, s, query, parameters);
                if (optional.isPresent())
                    values.add(optional.get());
            }
        } else {
            Optional<Value> optional = createSelectionValue(fromTable, aliasGenerator, selection, query, parameters);
            if (optional.isPresent())
                values.add(optional.get());
        }

        return values;
    }

}
