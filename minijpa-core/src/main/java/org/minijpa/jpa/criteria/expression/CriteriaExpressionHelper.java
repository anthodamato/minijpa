package org.minijpa.jpa.criteria.expression;

import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.mapper.ObjectConverter;
import org.minijpa.jpa.ParameterUtils;
import org.minijpa.jpa.criteria.AttributePath;
import org.minijpa.jpa.criteria.CriteriaUtils;
import org.minijpa.jpa.criteria.MiniRoot;
import org.minijpa.jpa.jpql.AggregateFunctionType;
import org.minijpa.jpa.jpql.FunctionUtils;
import org.minijpa.jpa.model.AbstractMetaAttribute;
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
import org.minijpa.sql.model.function.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Parameter;
import javax.persistence.criteria.CriteriaBuilder.Trimspec;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;
import java.sql.Types;
import java.util.*;

public class CriteriaExpressionHelper {

    private final Logger log = LoggerFactory.getLogger(CriteriaExpressionHelper.class);

    private TableColumn createTableColumnFromPath(AttributePath<?> attributePath,
                                                  AliasGenerator tableAliasGenerator) {
        AbstractMetaAttribute attribute = attributePath.getMetaAttribute();
        return new TableColumn(
                FromTable.of(attributePath.getMetaEntity().getTableName(),
                        tableAliasGenerator.getDefault(attributePath.getMetaEntity().getTableName())),
                new Column(attribute.getColumnName()));
    }

    private AggregateFunctionType getAggregateFunction(
            org.minijpa.jpa.criteria.expression.AggregateFunctionType aggregateFunctionType) {
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

        throw new IllegalArgumentException(
                "Unknown operator for expression type: " + expressionOperator);
    }

    public QueryParameter createQueryParameterForParameterExpression(
            Map<Parameter<?>, Object> parameterValues,
            ParameterExpression<?> parameterExpression,
            String columnName,
            Integer sqlType,
            ObjectConverter objectConverter) {
        if (parameterExpression.getName() != null) {
            Object value = ParameterUtils.findParameterValueByName(parameterExpression.getName(), parameterValues);
            return new QueryParameter(columnName, value, sqlType, objectConverter);
        }

        if (parameterExpression.getPosition() != null) {
            Object value = ParameterUtils.findParameterValueByPosition(
                    parameterExpression.getPosition(), parameterValues);
            return new QueryParameter(columnName, value, sqlType, objectConverter);
        }

        // Can the parameter value be set to null!?
        Object value = parameterValues.get(parameterExpression);
        return new QueryParameter(columnName, value, sqlType, objectConverter);
    }

    public Object createParameterFromExpression(
            Map<Parameter<?>, Object> parameterValues,
            Expression<?> expression,
            AliasGenerator aliasGenerator,
            List<QueryParameter> parameters,
            String columnName,
            Integer sqlType,
            ObjectConverter objectConverter) {
        if (expression instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expression;
            return createTableColumnFromPath(attributePath, aliasGenerator);
        }

        if (expression instanceof ParameterExpression<?>) {
            ParameterExpression<?> parameterExpression = (ParameterExpression<?>) expression;
            parameters.add(
                    createQueryParameterForParameterExpression(parameterValues,
                            parameterExpression, columnName,
                            sqlType,
                            objectConverter));
            return CriteriaUtils.QM;
        }

        return null;
    }

    protected Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            NegationExpression<?> negationExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Object xParam = createParameterFromExpression(parameterValues, negationExpression.getExpression(),
                aliasGenerator,
                parameters, "negation", Types.OTHER, null);
        return Optional.of(new Negation(xParam));
    }

    protected Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            CoalesceExpression<?> coalesceExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        List<Object> arguments = new ArrayList<>();
        if (coalesceExpression.getX() != null) {
            Object xParam = createParameterFromExpression(parameterValues,
                    (Expression<?>) coalesceExpression.getX(),
                    aliasGenerator, parameters, "coalesce", Types.OTHER, null);
            arguments.add(xParam);
        }

        if (coalesceExpression.getY() != null) {
            Object param = createParameterFromExpression(parameterValues,
                    (Expression<?>) coalesceExpression.getY(),
                    aliasGenerator, parameters, "coalesce", Types.OTHER, null);
            arguments.add(param);
        }

        if (coalesceExpression.getyValue() != null) {
            arguments.add(CriteriaUtils.buildValue(coalesceExpression.getyValue()));
        }

        arguments.addAll(coalesceExpression.getArguments());
        return Optional.of(new Coalesce(arguments.toArray()));
    }

    protected Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            NullifExpression<?> nullifExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Object xParam = createParameterFromExpression(
                parameterValues, nullifExpression.getX(), aliasGenerator,
                parameters,
                "nullif", Types.OTHER, null);

        if (nullifExpression.getY() != null) {
            Object yParam = createParameterFromExpression(parameterValues,
                    (Expression<?>) nullifExpression.getY(),
                    aliasGenerator, parameters, "nullif", Types.OTHER, null);
            return Optional.of(new Nullif(xParam, yParam));
        }

        return Optional.of(new Nullif(xParam, nullifExpression.getyValue()));
    }

    protected Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            SubstringExpression substringExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Substring.Builder builder = new Substring.Builder();
        Object xParam = createParameterFromExpression(parameterValues,
                substringExpression.getX(), aliasGenerator,
                parameters,
                "locate", Types.VARCHAR, null);
        builder.withArgument(xParam);

        if (substringExpression.getFrom() != null) {
            Object fromParam = createParameterFromExpression(parameterValues,
                    substringExpression.getFrom(),
                    aliasGenerator,
                    parameters,
                    "locate",
                    Types.INTEGER,
                    null);
            builder.withFrom(fromParam);
        }

        if (substringExpression.getFromInteger() != null) {
            builder.withFrom(CriteriaUtils.buildValue(substringExpression.getFromInteger()));
        }

        if (substringExpression.getLen() != null) {
            Object lenParam = createParameterFromExpression(parameterValues,
                    substringExpression.getLen(),
                    aliasGenerator,
                    parameters, "len", Types.INTEGER, null);
            builder.withLen(Optional.of(lenParam));
        }

        if (substringExpression.getLenInteger() != null) {
            builder.withLen(Optional.of(substringExpression.getLenInteger()));
        }

        return Optional.of(builder.build());
    }

    protected Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            LocateExpression locateExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Locate.Builder builder = new Locate.Builder();
        Object xParam = createParameterFromExpression(
                parameterValues, locateExpression.getX(), aliasGenerator,
                parameters,
                "locate", Types.VARCHAR, null);
        builder.withInputString(xParam);

        if (locateExpression.getPattern() != null) {
            Object patternParam = createParameterFromExpression(parameterValues,
                    locateExpression.getPattern(),
                    aliasGenerator, parameters, "locate", Types.VARCHAR, null);
            builder.withSearchString(patternParam);
        }

        if (locateExpression.getPatternString() != null) {
            builder.withSearchString(CriteriaUtils.buildValue(locateExpression.getPatternString()));
        }

        if (locateExpression.getFrom() != null) {
            Object fromParam = createParameterFromExpression(parameterValues,
                    locateExpression.getFrom(),
                    aliasGenerator,
                    parameters, "from", Types.INTEGER, null);
            builder.withPosition(Optional.of(fromParam));
        }

        if (locateExpression.getFromInteger() != null) {
            builder.withPosition(Optional.of(locateExpression.getFromInteger()));
        }

        return Optional.of(builder.build());
    }

    private Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            ConcatExpression concatExpression) {
        List<Object> values = new ArrayList<>();
        if (concatExpression.getX() != null) {
            AttributePath<?> attributePath = (AttributePath<?>) concatExpression.getX();
            values.add(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (concatExpression.getxValue() != null) {
            values.add(CriteriaUtils.buildValue(concatExpression.getxValue()));
        }

        if (concatExpression.getY() != null) {
            AttributePath<?> attributePath = (AttributePath<?>) concatExpression.getY();
            values.add(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (concatExpression.getyValue() != null) {
            values.add(CriteriaUtils.buildValue(concatExpression.getyValue()));
        }

        return Optional.of(new Concat(values.toArray()));
    }

    private Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            TrimExpression trimExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Trim.Builder builder = new Trim.Builder();
        builder.withArgument(
                createParameterFromExpression(parameterValues,
                        trimExpression.getX(), aliasGenerator, parameters,
                        "locate", Types.VARCHAR, null));

        if (trimExpression.getT() != null) {
            log.debug("createSelectionValue: trimExpression.getT().get()={}",
                    trimExpression.getT());
            builder.withTrimCharacter(
                    (String) createParameterFromExpression(parameterValues,
                            trimExpression.getT(),
                            aliasGenerator, parameters, "trim", Types.VARCHAR, null));
        }

        if (trimExpression.gettChar() != null) {
            log.debug("createSelectionValue: trimExpression.gettChar().get()={}",
                    trimExpression.gettChar());
            builder.withTrimCharacter(CriteriaUtils.buildValue("" + trimExpression.gettChar()));
        }

        if (trimExpression.getTs() != null) {
            Trimspec trimspec = trimExpression.getTs();
            if (trimspec == Trimspec.TRAILING) {
                builder.withTrimType(Optional.of(TrimType.TRAILING));
            } else if (trimspec == Trimspec.LEADING) {
                builder.withTrimType(Optional.of(TrimType.LEADING));
            } else if (trimspec == Trimspec.BOTH) {
                builder.withTrimType(Optional.of(TrimType.BOTH));
            }
        }

        return Optional.of(builder.build());
    }

    private Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            UnaryExpression<?> unaryExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        Optional<Value> optional = createSelectionValue(fromTable, aliasGenerator,
                unaryExpression.getExpression(),
                parameterValues, parameters);
        if (unaryExpression.getExpressionOperator() == ExpressionOperator.ABS) {
            return Optional.of(new Abs(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.SQRT) {
            return Optional.of(new Sqrt(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.LOWER) {
            return Optional.of(new Lower(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.UPPER) {
            return Optional.of(new Upper(optional.get()));
        } else if (unaryExpression.getExpressionOperator() == ExpressionOperator.NEGATION) {
            return Optional.of(new Negation(optional.get()));
        }

        return Optional.empty();
    }

    private List<Object> createSelectionValuesFromExpression(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            BinaryExpression<?> binaryExpression,
            Integer sqlType,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        List<Object> list = new ArrayList<>();
        if (binaryExpression.getX() != null) {
            list.add(createParameterFromExpression(parameterValues,
                    binaryExpression.getX(), aliasGenerator,
                    parameters,
                    "locate", sqlType, null));
        }

        if (binaryExpression.getxValue() != null) {
            list.add(CriteriaUtils.buildValue(binaryExpression.getxValue()));
        }

        if (binaryExpression.getY() != null) {
            list.add(createParameterFromExpression(parameterValues,
                    binaryExpression.getY(), aliasGenerator,
                    parameters,
                    "locate", sqlType, null));
        }

        if (binaryExpression.getyValue() != null) {
            list.add(CriteriaUtils.buildValue(binaryExpression.getyValue()));
        }

        return list;
    }

    private Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            BinaryExpression<?> binaryExpression,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        if (binaryExpression.getExpressionOperator() == ExpressionOperator.MOD) {
            List<Object> list = createSelectionValuesFromExpression(fromTable, aliasGenerator,
                    binaryExpression,
                    Types.INTEGER, parameterValues, parameters);
            return Optional.of(new Mod(list.get(0), list.get(1)));
        }

        SqlBinaryExpressionBuilder builder = new SqlBinaryExpressionBuilder(
                getSqlExpressionOperator(binaryExpression.getExpressionOperator()));
        if (binaryExpression.getX() != null) {
            AttributePath<?> attributePath = (AttributePath<?>) binaryExpression.getX();
            builder.setLeftExpression(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (binaryExpression.getxValue() != null) {
            if (CriteriaUtils.requireQM(binaryExpression.getxValue())) {
                builder.setLeftExpression(CriteriaUtils.QM);
            } else {
                builder.setLeftExpression(CriteriaUtils.buildValue(binaryExpression.getxValue()));
            }
        }

        if (binaryExpression.getY() != null) {
            AttributePath<?> attributePath = (AttributePath<?>) binaryExpression.getY();
            builder.setRightExpression(createTableColumnFromPath(attributePath, aliasGenerator));
        }

        if (binaryExpression.getyValue() != null) {
            if (CriteriaUtils.requireQM(binaryExpression.getyValue())) {
                builder.setRightExpression(CriteriaUtils.QM);
            } else {
                builder.setRightExpression(CriteriaUtils.buildValue(binaryExpression.getyValue()));
            }
        }

        SqlBinaryExpression sqlBinaryExpression = builder.build();
        log.debug("createSelectionValue: sqlBinaryExpression={}", sqlBinaryExpression);
        return Optional.of((Value) sqlBinaryExpression);
    }

    private Optional<Value> createSelectionValue(
            AliasGenerator aliasGenerator,
            AggregateFunctionExpression<?> aggregateFunctionExpression) {
        Expression<?> expr = aggregateFunctionExpression.getX();
        if (aggregateFunctionExpression
                .getAggregateFunctionType()
                == org.minijpa.jpa.criteria.expression.AggregateFunctionType.COUNT) {
            if (expr instanceof AttributePath<?>) {
                AttributePath<?> attributePath = (AttributePath<?>) expr;
                AbstractMetaAttribute metaAttribute = attributePath.getMetaAttribute();
                FromTable fromTable = buildFromTable(attributePath, aliasGenerator);
                return Optional.of(
                        new Count(new TableColumn(fromTable, new Column(metaAttribute.getColumnName())),
                                aggregateFunctionExpression.isDistinct()));
            } else if (expr instanceof MiniRoot<?>) {
                MiniRoot<?> miniRoot = (MiniRoot<?>) expr;
                MetaEntity metaEntity = miniRoot.getMetaEntity();
                List<MetaAttribute> idAttrs = metaEntity.getId().getAttributes();
                return Optional.of(new Count(new TableColumn(
                        FromTable.of(metaEntity.getTableName(),
                                aliasGenerator.getDefault(metaEntity.getTableName())),
                        new Column(idAttrs.get(0).getColumnName())), aggregateFunctionExpression.isDistinct()));
            }
        } else if (expr instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) expr;
            AbstractMetaAttribute metaAttribute = attributePath.getMetaAttribute();
            FromTable fromTable = buildFromTable(attributePath, aliasGenerator);
            Value value = FunctionUtils.createAggregateFunction(
                    getAggregateFunction(aggregateFunctionExpression.getAggregateFunctionType()),
                    new TableColumn(fromTable, new Column(metaAttribute.getColumnName())), false);
            return Optional.of(value);
        }

        return Optional.empty();
    }

    private FromTable buildFromTable(
            AttributePath<?> attributePath,
            AliasGenerator aliasGenerator) {
        MetaEntity metaEntity = attributePath.getMetaEntity();
        return FromTable.of(metaEntity.getTableName(),
                aliasGenerator.getDefault(metaEntity.getTableName()));
    }

    public Optional<Value> createSelectionValue(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            Selection<?> selection,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        if (selection == null) {
            return Optional.empty();
        }

        if (selection instanceof TypecastExpression) {
            return createSelectionValue(fromTable, aliasGenerator,
                    ((TypecastExpression<?>) selection).getExpression(),
                    parameterValues, parameters);
        }

        log.debug("createSelectionValue: selection={}", selection);
        if (selection instanceof AttributePath<?>) {
            AttributePath<?> attributePath = (AttributePath<?>) selection;
            AbstractMetaAttribute metaAttribute = attributePath.getMetaAttribute();
            FromTable ft = buildFromTable(attributePath, aliasGenerator);
            return Optional.of(new TableColumn(ft, new Column(metaAttribute.getColumnName())));
        } else if (selection instanceof AggregateFunctionExpression<?>) {
            return createSelectionValue(aliasGenerator, (AggregateFunctionExpression<?>) selection);
        } else if (selection instanceof BinaryExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (BinaryExpression<?>) selection, parameterValues,
                    parameters);
        } else if (selection instanceof UnaryExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (UnaryExpression<?>) selection, parameterValues,
                    parameters);
        } else if (selection instanceof ConcatExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (ConcatExpression) selection);
        } else if (selection instanceof TrimExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (TrimExpression) selection, parameterValues,
                    parameters);
        } else if (selection instanceof LocateExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (LocateExpression) selection, parameterValues,
                    parameters);
        } else if (selection instanceof SubstringExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (SubstringExpression) selection, parameterValues,
                    parameters);
        } else if (selection instanceof NullifExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (NullifExpression<?>) selection, parameterValues,
                    parameters);
        } else if (selection instanceof CoalesceExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (CoalesceExpression<?>) selection,
                    parameterValues,
                    parameters);
        } else if (selection instanceof NegationExpression) {
            return createSelectionValue(fromTable, aliasGenerator, (NegationExpression<?>) selection,
                    parameterValues,
                    parameters);
        } else if (selection instanceof CurrentDateExpression) {
            return Optional.of(new CurrentDate());
        } else if (selection instanceof CurrentTimeExpression) {
            return Optional.of(new CurrentTime());
        } else if (selection instanceof CurrentTimestampExpression) {
            return Optional.of(new CurrentTimestamp());
        }

        return Optional.empty();
    }

    public List<Value> createSelectionValues(
            FromTable fromTable,
            AliasGenerator aliasGenerator,
            Selection<?> selection,
            Map<Parameter<?>, Object> parameterValues,
            List<QueryParameter> parameters) {
        if (selection == null) {
            return Collections.emptyList();
        }

        List<Value> values = new ArrayList<>();
        if (selection.isCompoundSelection()) {
            List<Selection<?>> selections = selection.getCompoundSelectionItems();
            for (Selection<?> s : selections) {
                Optional<Value> optional = createSelectionValue(fromTable, aliasGenerator, s, parameterValues,
                        parameters);
                optional.ifPresent(values::add);
            }
        } else {
            Optional<Value> optional = createSelectionValue(fromTable, aliasGenerator, selection, parameterValues,
                    parameters);
            optional.ifPresent(values::add);
        }

        return values;
    }

}
