package org.minijpa.jpa.jpql;

import org.minijpa.jdbc.BasicFetchParameter;
import org.minijpa.jdbc.FetchParameter;
import org.minijpa.jdbc.QueryParameter;
import org.minijpa.jdbc.db.SqlSelectDataBuilder;
import org.minijpa.jpa.MetaEntityHelper;
import org.minijpa.jpa.criteria.CriteriaUtils;
import org.minijpa.jpa.db.AttributeFetchParameter;
import org.minijpa.jpa.db.DbConfiguration;
import org.minijpa.jpa.db.StatementParameters;
import org.minijpa.jpa.db.StatementType;
import org.minijpa.jpa.model.AbstractMetaAttribute;
import org.minijpa.jpa.model.MetaAttribute;
import org.minijpa.jpa.model.MetaEntity;
import org.minijpa.jpa.model.RelationshipMetaAttribute;
import org.minijpa.jpa.model.relationship.RelationshipJoinTable;
import org.minijpa.metadata.AliasGenerator;
import org.minijpa.metadata.PersistenceUnitContext;
import org.minijpa.sql.model.*;
import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.*;
import org.minijpa.sql.model.expression.SqlExpressionImpl;
import org.minijpa.sql.model.function.*;
import org.minijpa.sql.model.join.FromJoin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpqlParserVisitorImpl implements JpqlParserVisitor {

    private final Logger LOG = LoggerFactory.getLogger(JpqlParserVisitorImpl.class);

    private final PersistenceUnitContext persistenceUnitContext;
    private final DbConfiguration dbConfiguration;
    private AliasGenerator tableAliasGenerator;

    public JpqlParserVisitorImpl(PersistenceUnitContext persistenceUnitContext,
                                 DbConfiguration dbConfiguration) {
        this.persistenceUnitContext = persistenceUnitContext;
        this.dbConfiguration = dbConfiguration;
    }

    @Override
    public Object visit(ASTQLStatement node, Object data) {
        LOG.debug("visit: ASTQLStatement data={}", data);
        LOG.debug("visit: ASTQLStatement node={}", node);
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSelectStatement node, Object data) {
        this.tableAliasGenerator = persistenceUnitContext.createTableAliasGenerator();
        JpqlVisitorParameters jpqlVisitorParameters = new JpqlVisitorParameters();
        jpqlVisitorParameters.hints = ((JpqlParserInputData) data).getHints();
        node.childrenAccept(this, jpqlVisitorParameters);
        LOG.debug("visit: ASTSelectStatement - ");
        return createSelectFromParameters(jpqlVisitorParameters);
    }


    private Object createSelectFromParameters(
            JpqlVisitorParameters jpqlVisitorParameters) {
        SqlSelectDataBuilder selectBuilder = new SqlSelectDataBuilder();

        if (jpqlVisitorParameters.distinct) {
            selectBuilder.distinct();
        }

        if (jpqlVisitorParameters.identificationVariableEntity != null
                && jpqlVisitorParameters.identificationVariableEntity
                == jpqlVisitorParameters.sourceEntity) {
            selectBuilder.withResult(FromTable.of(jpqlVisitorParameters.sourceEntity.getTableName()));
        }

        jpqlVisitorParameters.fromTables.forEach(selectBuilder::withFromTable);
        if (jpqlVisitorParameters.fromJoins != null) {
            jpqlVisitorParameters.fromJoins.forEach(selectBuilder::withFromTable);
        }

        // add values and fetch parameters in case of fetch join
        if (jpqlVisitorParameters.statementType == StatementType.FETCH_JOIN) {
            jpqlVisitorParameters.fetchJoinMetaEntities.forEach(joinMetaEntity -> {
                jpqlVisitorParameters.values.addAll(buildFetchJoinValues(joinMetaEntity));
                jpqlVisitorParameters.fetchParameters.addAll(MetaEntityHelper.convertAllAttributes(joinMetaEntity));
            });
        }

        selectBuilder.withValues(jpqlVisitorParameters.values);
        jpqlVisitorParameters.values.forEach(v -> LOG.debug("createFromParameters: v={}", v));

        selectBuilder.withConditions(jpqlVisitorParameters.conditions);
        if (jpqlVisitorParameters.groupBy != null) {
            selectBuilder.withGroupBy(jpqlVisitorParameters.groupBy);
        }

        selectBuilder.withOrderBy(jpqlVisitorParameters.orderByList);

        LOG.debug("createFromParameters: jpqlVisitorParameters.conditions={}",
                jpqlVisitorParameters.conditions);

        selectBuilder.withFetchParameters(jpqlVisitorParameters.fetchParameters);
        SqlSelect sqlSelect = selectBuilder.build();
        if (jpqlVisitorParameters.statementType == StatementType.FETCH_JOIN) {
            return new StatementParameters(
                    sqlSelect,
                    jpqlVisitorParameters.parameters,
                    StatementType.FETCH_JOIN,
                    jpqlVisitorParameters.fetchJoinMetaEntities,
                    jpqlVisitorParameters.fetchJoinMetaAttributes);
        }

        return new StatementParameters(sqlSelect, jpqlVisitorParameters.parameters);
    }

    private List<Value> buildFetchJoinValues(MetaEntity fetchJoinMetaEntity) {
        Optional<String> optionalExistsAlias = tableAliasGenerator.findAliasByObjectName(
                fetchJoinMetaEntity.getTableName());
        if (optionalExistsAlias.isEmpty()) {
            throw new SemanticException("'" + fetchJoinMetaEntity.getName() + "' Entity alias not found");
        }

        FromTable fetchFromTable = FromTable.of(fetchJoinMetaEntity.getTableName(),
                optionalExistsAlias.get());
        return MetaEntityHelper.toValues(fetchJoinMetaEntity, fetchFromTable);
    }

    private Optional<MetaEntity> findMetaEntityBySqlAlias(String sqlAlias) {
        Optional<String> objectName = tableAliasGenerator.findObjectNameByAlias(sqlAlias);
        if (objectName.isEmpty()) {
            return Optional.empty();
        }

        Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByTableName(
                objectName.get());
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        return optional;
    }

    @Override
    public Object visit(ASTFromClause node, Object data) {
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTSelectClause node, Object data) {
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        jpqlVisitorParameters.distinct = node.isDistinct();
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTSelectExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        LOG.debug("visit: ASTSelectExpression node.jjtGetNumChildren()={}", node.jjtGetNumChildren());
        processSelectExpression(node, jpqlVisitorParameters);
        return object;
    }

    private void processSelectExpression(ASTSelectExpression node,
                                         JpqlVisitorParameters jpqlVisitorParameters) {
        if (node.jjtGetNumChildren() > 0) {
            Node node0 = node.jjtGetChild(0);
            LOG.debug("visit: ASTSelectExpression node0={}", node0);
            if (node0 instanceof ASTSingleValuedPathExpression) {
                ASTSingleValuedPathExpression singleValuedPathExpression = (ASTSingleValuedPathExpression) node0;
                List<MetaAttribute> metaAttributes = singleValuedPathExpression.getMetaAttributes();
                List<TableColumn> values = MetaEntityHelper.toValues(metaAttributes,
                        singleValuedPathExpression.getFromTable());
                jpqlVisitorParameters.values.addAll(values);
                List<FetchParameter> fetchParameters = metaAttributes.stream()
                        .map(AttributeFetchParameter::build).collect(Collectors.toList());
                jpqlVisitorParameters.fetchParameters.addAll(fetchParameters);
            } else if (node0 instanceof ASTScalarExpression) {
                LOG.debug("visit: ASTSelectExpression node0={}", node0);
                LOG.debug("visit: ASTSelectExpression node0_0.jjtGetNumChildren()={}",
                        node0.jjtGetNumChildren());
                if (node0.jjtGetNumChildren() > 0) {
                    LOG.debug("visit: ASTSelectExpression node0_0.jjtGetChild(0)={}", node0.jjtGetChild(0));
                }

                Value value = ((ASTScalarExpression) node0).getValue();
                jpqlVisitorParameters.values.add(value);
                jpqlVisitorParameters.fetchParameters
                        .add(createScalarExpressionFetchParameter((ASTScalarExpression) node0));
            } else if (node0 instanceof ASTAggregateExpression) {
                jpqlVisitorParameters.values.add(((ASTAggregateExpression) node0).getValue());
                jpqlVisitorParameters.fetchParameters
                        .add(createAggregateExpressionFetchParameter((ASTAggregateExpression) node0));
            }
        } else {
            String identificationVariable = node.getIdentificationVariable();
            LOG.debug("visit: ASTSelectItem identificationVariable={}", identificationVariable);
            if (identificationVariable != null) {
                String sqlTableAlias = jpqlVisitorParameters.aliases.get(identificationVariable);
                Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
                if (optional.isEmpty()) {
                    throw new SemanticException(
                            "Entity not found for alias '" + identificationVariable + "'");
                }

                MetaEntity metaEntity = optional.get();
                if (jpqlVisitorParameters.distinct) {
                    List<TableColumn> values = MetaEntityHelper.toValues(metaEntity.getId().getAttributes(),
                            FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                    jpqlVisitorParameters.values.addAll(values);
                    List<TableColumn> attrValues = MetaEntityHelper.toValues(metaEntity.getBasicAttributes(),
                            FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                    jpqlVisitorParameters.values.addAll(attrValues);

                    List<FetchParameter> fetchParameters = new ArrayList<>();
                    metaEntity.getId().getAttributes().forEach(v -> {
                        fetchParameters.add(AttributeFetchParameter.build(v));
                    });
                    metaEntity.getBasicAttributes().forEach(v -> {
                        fetchParameters.add(AttributeFetchParameter.build(v));
                    });

                    jpqlVisitorParameters.fetchParameters.addAll(fetchParameters);
                    jpqlVisitorParameters.identificationVariableEntity = metaEntity;
                } else {
                    List<Value> values = MetaEntityHelper.toValues(metaEntity,
                            FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                    jpqlVisitorParameters.values.addAll(values);
                    List<FetchParameter> fetchParameters = MetaEntityHelper.convertAllAttributes(metaEntity);
                    jpqlVisitorParameters.fetchParameters.addAll(fetchParameters);
                    jpqlVisitorParameters.identificationVariableEntity = metaEntity;
                }
            }
        }
    }

    @Override
    public Object visit(ASTSelectItem node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        String result_variable = node.getAlias();
        LOG.debug("visit: ASTSelectItem result_variable={}", result_variable);
        ASTSelectExpression selectExpression = (ASTSelectExpression) node.jjtGetChild(0);
        List<Value> values = selectExpression.getValues();
        List<FetchParameter> fetchParameters = selectExpression.getFetchParameters();
        LOG.debug("visit: ASTSelectItem values={}", values);
        if (values != null) {
            jpqlVisitorParameters.values.addAll(values);
            if (result_variable != null && result_variable.length() > 0) {
                jpqlVisitorParameters.resultVariables.put(result_variable, values);
            }
        }

        if (fetchParameters != null) {
            jpqlVisitorParameters.fetchParameters.addAll(fetchParameters);
        }

        return object;
    }

    @Override
    public Object visit(ASTRangeVariableDeclaration node, Object data) {
        Object object = node.childrenAccept(this, data);

        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        String rvdEntityName = node.getEntityName();
        LOG.debug("visit: ASTRangeVariableDeclaration rvdEntityName={}", rvdEntityName);
        Optional<MetaEntity> optional = persistenceUnitContext.findMetaEntityByName(rvdEntityName);
        if (optional.isEmpty()) {
            throw new SemanticException("Entity name '" + node.getEntityName() + "' not found");
        }

        String rvdAlias = node.getAlias();
        LOG.debug("visit: ASTRangeVariableDeclaration rvdAlias={}", rvdAlias);

        MetaEntity sourceEntity = optional.get();
        Optional<String> optionalExistsAlias = tableAliasGenerator.findAliasByObjectName(
                sourceEntity.getTableName());
        if (optionalExistsAlias.isPresent() && optionalExistsAlias.get().equals(rvdAlias)) {
            throw new SemanticException("Entity alias '" + optionalExistsAlias.get() + "' already used");
        }

        if (optionalExistsAlias.isEmpty()) {
            String tableAlias = tableAliasGenerator.getDefault(sourceEntity.getTableName());
            LOG.debug("visit: ASTRangeVariableDeclaration optionalAlias.isEmpty() tableAlias={}",
                    tableAlias);
            jpqlVisitorParameters.aliases.put(rvdAlias, tableAlias);
            jpqlVisitorParameters.sourceEntity = sourceEntity;
            FromTable fromTable = FromTable.of(sourceEntity.getTableName(), tableAlias);
            jpqlVisitorParameters.fromTables.add(fromTable);
        } else {
            String tableAlias = tableAliasGenerator.next(sourceEntity.getTableName());
            LOG.debug("visit: ASTRangeVariableDeclaration !optionalAlias.isEmpty() tableAlias={}",
                    tableAlias);
            jpqlVisitorParameters.aliases.put(rvdAlias, tableAlias);
            jpqlVisitorParameters.sourceEntity = sourceEntity;
            FromTable fromTable = FromTable.of(sourceEntity.getTableName(), tableAlias);
            jpqlVisitorParameters.fromTables.add(fromTable);
        }

        return object;
    }

    @Override
    public Object visit(ASTIdentificationVariableDeclaration node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSubtype node, Object data) {
        LOG.debug("visit: ASTSubtype data={}", data);
        LOG.debug("visit: ASTSubtype node={}", node);
        return node.childrenAccept(this, data);
    }

    private org.minijpa.sql.model.join.JoinType decodeJoinType(JoinType joinType) {
        if (joinType.isLeft()) {
            return org.minijpa.sql.model.join.JoinType.Left;
        }

        return org.minijpa.sql.model.join.JoinType.Inner;
    }

    @Override
    public Object visit(ASTJoin node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTJoin data={}", data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        JoinType jt = node.getJoinType();
        LOG.debug("visit: join.jjtGetNumChildren()={}", node.jjtGetNumChildren());
        ASTJoinAssociationPathExpression joinAssociationPathExpression = (ASTJoinAssociationPathExpression) node
                .jjtGetChild(0);
        String identificationVariable = node.getIdentificationVariable();
        LOG.debug("visit: joinAlias={}", identificationVariable);
        buildJoin(jpqlVisitorParameters, jt, joinAssociationPathExpression, identificationVariable);
        return object;
    }

    private void buildJoin(
            JpqlVisitorParameters jpqlVisitorParameters,
            JoinType jt,
            ASTJoinAssociationPathExpression joinAssociationPathExpression,
            String identificationVariable) {
        ASTJoinSingleValuedPathExpression joinSingleValuedPathExpression = (ASTJoinSingleValuedPathExpression) joinAssociationPathExpression
                .jjtGetChild(0);

        LOG.debug("buildJoin: joinSingleValuedPathExpression={}", joinSingleValuedPathExpression);
        String jpqlAlias = joinSingleValuedPathExpression.getIdentificationVariable();
        String attributePath = joinSingleValuedPathExpression.getPath().isEmpty()
                ? joinSingleValuedPathExpression.getFieldName()
                : joinSingleValuedPathExpression.getPath() + "."
                + joinSingleValuedPathExpression.getFieldName();
        LOG.debug("buildJoin: attributePath={}", attributePath);

        String sqlTableAlias = jpqlVisitorParameters.aliases.get(jpqlAlias);
        Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
        if (optional.isEmpty()) {
            throw new SemanticException("Entity not found for alias '" + jpqlAlias + "'");
        }

        MetaEntity metaEntity = optional.get();
//        AbstractMetaAttribute metaAttribute = AttributeUtil.findAttributeFromPath(attributePath,
//                metaEntity);
        AbstractMetaAttribute metaAttribute = metaEntity.findAttributeFromPath(attributePath);
        if (metaAttribute == null) {
            throw new SemanticException(
                    "Attribute path '" + attributePath + "' on '" + metaEntity.getName()
                            + "' entity not found");
        }

        if (!(metaAttribute instanceof RelationshipMetaAttribute)) {
            throw new SemanticException(
                    "Attribute '" + metaAttribute.getName() + "' is not a relationship attribute");
        }

        createRelationshipFromJoin(jpqlVisitorParameters, (RelationshipMetaAttribute) metaAttribute,
                metaEntity,
                identificationVariable, decodeJoinType(jt));
    }

    private void createRelationshipFromJoin(
            JpqlVisitorParameters jpqlVisitorParameters,
            RelationshipMetaAttribute relationshipMetaAttribute,
            MetaEntity metaEntity,
            String entityAlias,
            org.minijpa.sql.model.join.JoinType joinType) {
        LOG.debug("createRelationshipFromJoin: metaAttribute.getRelationship().getJoinTable()="
                + relationshipMetaAttribute.getRelationship().getJoinTable());
        if (relationshipMetaAttribute.getRelationship().getJoinTable() != null) {
            String tableAlias = tableAliasGenerator
                    .getDefault(
                            relationshipMetaAttribute.getRelationship().getJoinTable().getTargetEntity().getTableName());
            jpqlVisitorParameters.aliases.put(entityAlias, tableAlias);
            List<FromJoin> fromJoins = dbConfiguration.getSqlStatementFactory().calculateJoins(metaEntity,
                    relationshipMetaAttribute, joinType, tableAliasGenerator);
            jpqlVisitorParameters.fromJoins.addAll(fromJoins);
            jpqlVisitorParameters.fetchJoinMetaEntities.add(relationshipMetaAttribute.getRelationship().getJoinTable().getTargetEntity());
        } else if (relationshipMetaAttribute.getRelationship().getJoinColumnMapping() != null) {
            String tableAlias = tableAliasGenerator
                    .getDefault(relationshipMetaAttribute.getRelationship().getAttributeType().getTableName());
            jpqlVisitorParameters.aliases.put(entityAlias, tableAlias);
            List<FromJoin> fromJoins = dbConfiguration.getSqlStatementFactory().calculateJoins(metaEntity,
                    relationshipMetaAttribute, joinType, tableAliasGenerator);
            jpqlVisitorParameters.fromJoins.addAll(fromJoins);
            jpqlVisitorParameters.fetchJoinMetaEntities.add(relationshipMetaAttribute.getRelationship().getAttributeType());
        }

        jpqlVisitorParameters.fetchJoinMetaAttributes.add(relationshipMetaAttribute);
    }

    @Override
    public Object visit(ASTJoinAssociationPathExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTJoinSingleValuedPathExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTMapFieldIdentificationVariable node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGeneralIdentificationVariable node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSimpleSubpath node, Object data) {
        Object object = node.childrenAccept(this, data);
        ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) node
                .jjtGetChild(0);
        node.setIdentificationVariable(generalIdentificationVariable.getIdentificationVariable());
        node.setMapFieldIdentificationVariable(
                generalIdentificationVariable.getMapFieldIdentificationVariable());
        return object;
    }

    @Override
    public Object visit(ASTGeneralSubpath node, Object data) {
        LOG.debug("visit: ASTGeneralSubpath");
        Object object = node.childrenAccept(this, data);
        Node node0 = node.jjtGetChild(0);
        if (node0 instanceof ASTSimpleSubpath) {
            ASTSimpleSubpath simpleSubpath = (ASTSimpleSubpath) node0;
            node.setIdentificationVariable(simpleSubpath.getIdentificationVariable());
            node.setMapFieldIdentificationVariable(simpleSubpath.getMapFieldIdentificationVariable());
            node.setPath(simpleSubpath.getPath());
        }

        return object;
    }

    @Override
    public Object visit(ASTTreatedSubpath node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTStateFieldPathExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        ASTGeneralSubpath generalSubpath = (ASTGeneralSubpath) node.jjtGetChild(0);
        node.setIdentificationVariable(generalSubpath.getIdentificationVariable());
        node.setMapFieldIdentificationVariable(generalSubpath.getMapFieldIdentificationVariable());
        node.setPath(generalSubpath.getPath());

        return object;
    }

    @Override
    public Object visit(ASTStateValuedPathExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTStateValuedPathExpression node.jjtGetNumChildren()={}",
                node.jjtGetNumChildren());
        LOG.debug("visit: ASTStateValuedPathExpression data={}", data);
        if (node.jjtGetNumChildren() == 0) {
            return object;
        }

        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        Node n0_0_0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTStateValuedPathExpression n0_0_0={}", n0_0_0);
        if (n0_0_0 instanceof ASTStateFieldPathExpression) {
            ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) n0_0_0;
            ASTGeneralSubpath generalSubPath = (ASTGeneralSubpath) stateFieldPathExpression.jjtGetChild(
                    0);
            String stateField = stateFieldPathExpression.getStateField();
            LOG.debug("visit: ASTStateValuedPathExpression stateField={}", stateField);
            Node n0_0_0_0 = generalSubPath.jjtGetChild(0);
            LOG.debug("visit: ASTStateValuedPathExpression n0_0_0_0={}", n0_0_0_0);
            if (n0_0_0_0 instanceof ASTSimpleSubpath) {
                ASTSimpleSubpath simpleSubpath = (ASTSimpleSubpath) n0_0_0_0;
                ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) simpleSubpath
                        .jjtGetChild(0);
                if (generalIdentificationVariable.jjtGetNumChildren() == 0) {
                    String identificationVariable = generalIdentificationVariable.getIdentificationVariable();

                    LOG.debug("visit: ASTStateValuedPathExpression identificationVariable={}",
                            identificationVariable);
                    String r = identificationVariable + "." + stateField;
                    LOG.debug("visit: ASTStateValuedPathExpression r={}", r);
                    node.setPath(r);
                } else {
                    // map_field_identification_variable()
                }
            }
        } else {
            ASTGeneralIdentificationVariable generalIdentificationVariable = (ASTGeneralIdentificationVariable) n0_0_0;
            String identificationVariable = generalIdentificationVariable.getIdentificationVariable();
            node.setPath(identificationVariable);
        }

        return object;
    }

    @Override
    public Object visit(ASTWhenClause node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTStringExpression node, Object data) {
        LOG.debug("visit: ASTStringExpression node={}", node);
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTStringExpression node.jjtGetNumChildren()={}", node.jjtGetNumChildren());
        if (node.jjtGetNumChildren() == 0) {
            return object;
        }

        Node n0_0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTStringExpression n0_0={}", n0_0);
        if (n0_0 instanceof ASTStateValuedPathExpression) {
            ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
            node.setPath(stateValuedPathExpression.getPath());
        }

        return object;
    }

    @Override
    public Object visit(ASTStringExpressionComparison node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTStringExpressionComparison object={}", object);
        ASTStringExpression stringExpression0 = (ASTStringExpression) node.jjtGetChild(0);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        String comparisonOperator = node.getComparisonOperator();
        Node n1 = node.jjtGetChild(1);
        if (n1 instanceof ASTStringExpression) {
            ASTStringExpression stringExpression1 = (ASTStringExpression) n1;
            Object queryParameterObject0 = decodeExpression(stringExpression0, jpqlVisitorParameters, null);
            Object queryParameterObject1 = decodeExpression(stringExpression1, jpqlVisitorParameters, queryParameterObject0);
            if (queryParameterObject1 instanceof TableColumn) {
                queryParameterObject0 = decodeExpression(stringExpression0, jpqlVisitorParameters, queryParameterObject1);
            }

            BinaryCondition binaryCondition = new BinaryCondition(decodeConditionType(comparisonOperator),
                    queryParameterObject0,
                    queryParameterObject1);
            node.setCondition(binaryCondition);
        }

        return object;
    }

    @Override
    public Object visit(ASTBooleanExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        if (node.jjtGetNumChildren() == 0) {
            return object;
        }

        Node n0_0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTBooleanExpression n0_0={}", n0_0);
        if (n0_0 instanceof ASTStateValuedPathExpression) {
            ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
            node.setPath(stateValuedPathExpression.getPath());
        }

        return object;
    }

    @Override
    public Object visit(ASTBooleanExpressionComparison node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        ASTBooleanExpression expression0 = (ASTBooleanExpression) node.jjtGetChild(0);

        String comparisonOperator = node.getComparisonOperator();
        Node n1 = node.jjtGetChild(1);
        if (n1 instanceof ASTBooleanExpression) {
            ASTBooleanExpression expression1 = (ASTBooleanExpression) n1;
            // query parameter can be on the left or on the right
            Object queryParameterObject0 = decodeExpression(expression0, jpqlVisitorParameters, null);
            Object queryParameterObject1 = decodeExpression(expression1, jpqlVisitorParameters, queryParameterObject0);
            if (queryParameterObject1 instanceof TableColumn) {
                queryParameterObject0 = decodeExpression(expression0, jpqlVisitorParameters, queryParameterObject1);
            }

            BinaryCondition binaryCondition = new BinaryCondition(decodeConditionType(comparisonOperator),
                    queryParameterObject0,
                    queryParameterObject1);
            LOG.debug("visit: ASTBooleanExpressionComparison binaryCondition={}", binaryCondition);
            node.setCondition(binaryCondition);
        }

        return object;
    }

    @Override
    public Object visit(ASTArithmeticExpressionComparison node, Object data) {
        Object object = node.childrenAccept(this, data);
        Node n0 = node.jjtGetChild(0);
        ASTArithmeticExpression expression0 = (ASTArithmeticExpression) n0;
        List<Object> r0 = new ArrayList<>();
        processArithmeticExpressionResult(expression0, r0);

        String comparisonOperator = node.getComparisonOperator();
        Node n1 = node.jjtGetChild(1);
        if (n1 instanceof ASTArithmeticExpression) {
            ASTArithmeticExpression expression1 = (ASTArithmeticExpression) n1;
            List<Object> r1 = new ArrayList<>();
            processArithmeticExpressionResult(expression1, r1);
            BinaryCondition.Builder builder = new BinaryCondition.Builder(
                    decodeConditionType(comparisonOperator));
            builder.withLeft(r0);
            builder.withRight(r1);
            node.setCondition(builder.build());
        }

        return object;
    }

    @Override
    public Object visit(ASTDatetimeExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        if (node.jjtGetNumChildren() == 0) {
            return object;
        }

        Node n0_0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTDatetimeExpression n0_0={}", n0_0);
        if (n0_0 instanceof ASTStateValuedPathExpression) {
            ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
            node.setPath(stateValuedPathExpression.getPath());
        }

        return object;
    }

    @Override
    public Object visit(ASTDatetimeExpressionComparison node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        Node n0 = node.jjtGetChild(0);
        ASTDatetimeExpression expression0 = (ASTDatetimeExpression) n0;

        String comparisonOperator = node.getComparisonOperator();
        Node n1 = node.jjtGetChild(1);
        if (n1 instanceof ASTDatetimeExpression) {
            ASTDatetimeExpression expression1 = (ASTDatetimeExpression) n1;
            BinaryCondition.Builder builder = new BinaryCondition.Builder(
                    decodeConditionType(comparisonOperator));
            // query parameter can be on the left or on the right
            Object queryParameterObject0 = decodeExpression(expression0, jpqlVisitorParameters, null);
            Object queryParameterObject1 = decodeExpression(expression1, jpqlVisitorParameters, queryParameterObject0);
            if (queryParameterObject1 instanceof TableColumn) {
                queryParameterObject0 = decodeExpression(expression0, jpqlVisitorParameters, queryParameterObject1);
            }

            builder.withLeft(queryParameterObject0);
            builder.withRight(queryParameterObject1);
            node.setCondition(builder.build());
        }

        return object;
    }

    private ConditionType decodeConditionType(String comparisonOperator) {
        LOG.debug("decodeConditionType: comparisonOperator={}", comparisonOperator);
        if (comparisonOperator.equals(">")) {
            return ConditionType.GREATER_THAN;
        }
        if (comparisonOperator.equals(">=")) {
            return ConditionType.GREATER_THAN_OR_EQUAL_TO;
        }
        if (comparisonOperator.equals("<")) {
            return ConditionType.LESS_THAN;
        }
        if (comparisonOperator.equals("<=")) {
            return ConditionType.LESS_THAN_OR_EQUAL_TO;
        }
        if (comparisonOperator.equals("<>")) {
            return ConditionType.NOT_EQUAL;
        }
        if (comparisonOperator.equals("=")) {
            return ConditionType.EQUAL;
        }

        return null;
    }

    private Object decodeExpression(ASTArithmeticPrimary expression,
                                    JpqlVisitorParameters jpqlVisitorParameters,
                                    Object queryParameterAttribute) {
        if (expression.getPath() != null) {
            Object[] sqlPath = splitJpqlPath(expression.getPath(), jpqlVisitorParameters);
            return buildTableColumnFromPath(sqlPath);
        }

        if (expression.getInputParameter() != null) {
            QueryParameter queryParameter = buildQueryParameter(
                    queryParameterAttribute,
//                    jpqlVisitorParameters.parameterMap,
                    expression.getInputParameter());
            jpqlVisitorParameters.parameters.add(queryParameter);
            return CriteriaUtils.QM;
        }

        Object result = expression.getResult();
        if (result instanceof String) {
            return result;
        }

        if (result instanceof SqlSelect) {
            return result;
        }

        if (expression.jjtGetNumChildren() > 0) {
            Node node0 = expression.jjtGetChild(0);
            if (node0 instanceof ASTFunctionsReturningNumerics) {
                Node node1 = node0.jjtGetChild(0);
                return decodeFunction(node1, jpqlVisitorParameters, queryParameterAttribute);
            } else if (node0 instanceof ASTAggregateExpression) {
                return ((ASTAggregateExpression) node0).getValue();
            }
        }

        return "";
    }

    private Object decodeFunction(
            Node node,
            JpqlVisitorParameters jpqlVisitorParameters,
            Object queryParameterAttribute) {
        if (node instanceof ASTConcatFunction) {
            List<Object> args = new ArrayList<>();
            for (Node n : ((SimpleNode) node).children) {
                args.add(decodeExpression(n, jpqlVisitorParameters, queryParameterAttribute));
            }

            return new Concat(args.toArray());
        } else if (node instanceof ASTSubstringFunction) {
            Node node0 = node.jjtGetChild(0);
            Object param1 = decodeExpression(node0, jpqlVisitorParameters, queryParameterAttribute);
            List<Object> param2 = new ArrayList<>();
            processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(1), param2);
            if (node.jjtGetNumChildren() > 2) {
                List<Object> param3 = new ArrayList<>();
                processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(2), param3);
                return new Substring(param1, param2, Optional.of(param3));
            }

            return new Substring(param1, param2);
        } else if (node instanceof ASTTrimFunction) {
            Object param = decodeExpression(node.jjtGetChild(0), jpqlVisitorParameters, queryParameterAttribute);
            Optional<TrimType> trimType = ((ASTTrimFunction) node).getTrimType() != null
                    ? Optional.of(((ASTTrimFunction) node).getTrimType())
                    : Optional.empty();
            if (((ASTTrimFunction) node).getTrimCharacter() == null) {
                return new Trim(param, trimType);
            } else {
                return new Trim(param, trimType, ((ASTTrimFunction) node).getTrimCharacter());
            }
        } else if (node instanceof ASTLowerFunction) {
            Object param = decodeExpression(node.jjtGetChild(0), jpqlVisitorParameters, queryParameterAttribute);
            return new Lower(param);
        } else if (node instanceof ASTUpperFunction) {
            Object param = decodeExpression(node.jjtGetChild(0), jpqlVisitorParameters, queryParameterAttribute);
            return new Upper(param);
        } else if (node instanceof ASTLengthFunction) {
            Object param = decodeExpression(node.jjtGetChild(0), jpqlVisitorParameters, queryParameterAttribute);
            return new Length(param);
        } else if (node instanceof ASTLocateFunction) {
            Object param1 = decodeExpression(node.jjtGetChild(0), jpqlVisitorParameters, queryParameterAttribute);
            Object param2 = decodeExpression(node.jjtGetChild(1), jpqlVisitorParameters, queryParameterAttribute);
            if (node.jjtGetNumChildren() > 2) {
                List<Object> param3 = new ArrayList<>();
                processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(2), param3);
                return new Locate(param1, param2, Optional.of(param3));
            }

            return new Locate(param1, param2);
        } else if (node instanceof ASTAbsFunction) {
            List<Object> param = new ArrayList<>();
            processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(0), param);
            return new Abs(param);
        } else if (node instanceof ASTSqrtFunction) {
            List<Object> param = new ArrayList<>();
            processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(0), param);
            return new Sqrt(param);
        } else if (node instanceof ASTModFunction) {
            List<Object> dividend = new ArrayList<>();
            processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(0), dividend);
            List<Object> divider = new ArrayList<>();
            processArithmeticExpressionResult((ASTArithmeticExpression) node.jjtGetChild(1), dividend);
            return new Mod(dividend, divider);
        }

        return null;
    }

    private TableColumn buildTableColumnFromPath(Object[] sqlPath) {
        MetaEntity metaEntity = (MetaEntity) sqlPath[1];
        TableColumn tableColumn = new TableColumn(
                FromTable.of(metaEntity.getTableName(), (String) sqlPath[0]),
                new Column((String) sqlPath[2]));
        return tableColumn;
    }

    private Object decodeExpression(ASTStringExpression expression,
                                    JpqlVisitorParameters jpqlVisitorParameters,
                                    Object queryParameterAttribute) {
        if (expression.getPath() != null) {
            Object[] sqlPath = splitJpqlPath(expression.getPath(), jpqlVisitorParameters);
            return buildTableColumnFromPath(sqlPath);
        }

        if (expression.getStringLiteral() != null) {
            return expression.getStringLiteral();
        }

        if (expression.getInputParameter() != null) {
            QueryParameter queryParameter = buildQueryParameter(
                    queryParameterAttribute,
//                    jpqlVisitorParameters.parameterMap,
                    expression.getInputParameter());
            jpqlVisitorParameters.parameters.add(queryParameter);
            return CriteriaUtils.QM;
        }

        if (expression.jjtGetNumChildren() > 0) {
            Node node0 = expression.jjtGetChild(0);
            if (node0 instanceof ASTFunctionsReturningStrings) {
                Node node1 = node0.jjtGetChild(0);
                return decodeFunction(node1, jpqlVisitorParameters, queryParameterAttribute);
            }
        }

        return "";
    }

    private Object decodeExpression(ASTBooleanExpression expression,
                                    JpqlVisitorParameters jpqlVisitorParameters,
                                    Object queryParameterAttribute) {
        if (expression.getBooleanValue() != null) {
            return expression.getBooleanValue();
        }

        if (expression.getPath() != null) {
            Object[] sqlPath = splitJpqlPath(expression.getPath(), jpqlVisitorParameters);
            return buildTableColumnFromPath(sqlPath);
        }

        if (expression.getInputParameter() != null) {
            QueryParameter queryParameter = buildQueryParameter(
                    queryParameterAttribute,
//                    jpqlVisitorParameters.parameterMap,
                    expression.getInputParameter());
            jpqlVisitorParameters.parameters.add(queryParameter);
            return CriteriaUtils.QM;
        }

        throw new SemanticException("Unknown Boolean expression: " + expression);
    }

    private Object decodeExpression(ASTDatetimeExpression expression,
                                    JpqlVisitorParameters jpqlVisitorParameters,
                                    Object queryParameterAttribute) {
        if (expression.getFunction() != null) {
            return expression.getFunction();
        }

        if (expression.getPath() != null) {
            Object[] sqlPath = splitJpqlPath(expression.getPath(), jpqlVisitorParameters);
            return buildTableColumnFromPath(sqlPath);
        }

        if (expression.getInputParameter() != null) {
            QueryParameter queryParameter = buildQueryParameter(
                    queryParameterAttribute,
//                    jpqlVisitorParameters.parameterMap,
                    expression.getInputParameter());
            jpqlVisitorParameters.parameters.add(queryParameter);
            return CriteriaUtils.QM;
        }

        return "";
    }

    private Object decodeExpression(
            Node expression,
            JpqlVisitorParameters jpqlVisitorParameters,
            Object queryParameterObject) {
        if (expression instanceof ASTArithmeticPrimary) {
            return decodeExpression((ASTArithmeticPrimary) expression, jpqlVisitorParameters, queryParameterObject);
        }

        if (expression instanceof ASTStringExpression) {
            return decodeExpression((ASTStringExpression) expression, jpqlVisitorParameters, queryParameterObject);
        }

        if (expression instanceof ASTBooleanExpression) {
            return decodeExpression((ASTBooleanExpression) expression, jpqlVisitorParameters, queryParameterObject);
        }

        if (expression instanceof ASTDatetimeExpression) {
            return decodeExpression((ASTDatetimeExpression) expression, jpqlVisitorParameters, queryParameterObject);
        }

        return null;
    }

    @Override
    public Object visit(ASTComparisonExpression node, Object data) {
        LOG.debug("visit: ASTComparisonExpression data={}", data);
        LOG.debug("visit: ASTComparisonExpression node={}", node);
        LOG.debug("visit: ASTComparisonExpression node.jjtGetNumChildren()={}",
                node.jjtGetNumChildren());
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTComparisonExpression object={}", object);
        Node n0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTComparisonExpression n0={}", n0);
        node.setCondition(((ConditionNode) n0).getCondition());
        return object;
    }

    @Override
    public Object visit(ASTSimpleCondExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTSimpleCondExpression object={}", object);
        node.setCondition(((ConditionNode) node.jjtGetChild(0)).getCondition());
        return object;
    }

    @Override
    public Object visit(ASTConditionalPrimary node, Object data) {
        LOG.debug("visit: ASTConditionalPrimary data={}", data);
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTConditionalPrimary object={}", object);
        Node n = node.jjtGetChild(0);
        if (n instanceof ASTSimpleCondExpression) {
            node.setCondition(((ASTSimpleCondExpression) n).getCondition());
            return object;
        }

        Condition condition = new NestedCondition(((ASTConditionalExpression) n).getCondition());
        node.setCondition(condition);
        return object;
    }

    @Override
    public Object visit(ASTConditionalFactor node, Object data) {
        LOG.debug("visit: ASTConditionalFactor data={}", data);
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTConditionalFactor object={}", object);
        ASTConditionalPrimary conditionalPrimary = (ASTConditionalPrimary) node.jjtGetChild(0);
        if (node.isNot()) {
            node.setCondition(new NotCondition(conditionalPrimary.getCondition()));
        } else {
            node.setCondition(conditionalPrimary.getCondition());
        }

        return object;
    }

    @Override
    public Object visit(ASTConditionalTerm node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTConditionalTerm object={}", object);
        if (node.jjtGetNumChildren() == 1) {
            node.setCondition(((ASTConditionalFactor) node.jjtGetChild(0)).getCondition());
        } else {
            List<Condition> conditions = new ArrayList<>();
            for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
                Condition condition = ((ASTConditionalFactor) node.jjtGetChild(i)).getCondition();
                conditions.add(condition);
            }

            node.setCondition(new BinaryLogicConditionImpl(ConditionType.AND, conditions));
        }

        return object;
    }

    @Override
    public Object visit(ASTConditionalExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTConditionalExpression object={}", object);
        if (node.jjtGetNumChildren() == 1) {
            node.setCondition(((ASTConditionalTerm) node.jjtGetChild(0)).getCondition());
        } else {
            List<Condition> conditions = new ArrayList<>();
            for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
                Condition condition = ((ASTConditionalTerm) node.jjtGetChild(i)).getCondition();
                conditions.add(condition);
            }

            node.setCondition(new BinaryLogicConditionImpl(ConditionType.OR, conditions));
        }

        return object;
    }

    @Override
    public Object visit(ASTWhereClause node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTWhereClause object={}", object);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        jpqlVisitorParameters.conditions.add(
                ((ASTConditionalExpression) node.jjtGetChild(0)).getCondition());
        return object;
    }

    @Override
    public Object visit(ASTHavingClause node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSingleValuedObjectPathExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTCollectionValuedPathExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTArithmeticPrimary node, Object data) {
        Object object = node.childrenAccept(this, data);
        LOG.debug("visit: ASTArithmeticPrimary node.getResult()={}", node.getResult());
        if (node.jjtGetNumChildren() == 0) {
            return object;
        }

        Node n0_0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTArithmeticPrimary n0_0={}", n0_0);
        if (n0_0 instanceof ASTStateValuedPathExpression) {
            ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0_0;
            node.setPath(stateValuedPathExpression.getPath());
        } else if (n0_0 instanceof ASTSubquery) {
            ASTSubquery subquery = (ASTSubquery) n0_0;
            node.setResult(subquery.getStatementParameters().getSqlStatement());
        }

        return object;
    }

    @Override
    public Object visit(ASTArithmeticFactor node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        ASTArithmeticPrimary n0_0 = (ASTArithmeticPrimary) node.jjtGetChild(0);
        node.setResult(decodeExpression(n0_0, jpqlVisitorParameters, null));
        return object;
    }

    private void processArithmeticTermResult(ASTArithmeticTerm node, List<Object> list) {
        ASTArithmeticFactor n0_0 = (ASTArithmeticFactor) node.jjtGetChild(0);
        LOG.debug("processArithmeticTermResult: n0_0.getResult()={}", n0_0.getResult());
        list.add(n0_0.getSign());
        list.add(n0_0.getResult());
        for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
            ASTArithmeticFactor n_i = (ASTArithmeticFactor) node.jjtGetChild(i);
            list.add(node.getSigns().get(i - 1));
            list.add(n_i.getSign());
            list.add(n_i.getResult());
        }
    }

    private void processArithmeticExpressionResult(ASTArithmeticExpression node, List<Object> list) {
        ASTArithmeticTerm n0_0 = (ASTArithmeticTerm) node.jjtGetChild(0);
        LOG.debug("processArithmeticExpressionResult: n0_0.getResult()={}", n0_0.getResult());
        processArithmeticTermResult(n0_0, list);
        for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
            ASTArithmeticTerm n_i = (ASTArithmeticTerm) node.jjtGetChild(i);
            list.add(node.getSigns().get(i - 1));
            processArithmeticTermResult(n_i, list);
        }
    }

    @Override
    public Object visit(ASTArithmeticTerm node, Object data) {
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTArithmeticExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTQualifiedIdentificationVariable node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTScalarExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        Node node0 = node.jjtGetChild(0);
        if (node0 instanceof ASTArithmeticExpression) {
            ASTArithmeticExpression arithmeticExpression = (ASTArithmeticExpression) node0;
            List<Object> list = new ArrayList<>();
            processArithmeticExpressionResult(arithmeticExpression, list);
            Value value = new SqlExpressionImpl(list);
            node.setValue(value);
        } else if (node0 instanceof ASTDatetimeExpression) {
            ASTDatetimeExpression datetimeExpression = (ASTDatetimeExpression) node0;
            Value value = new SqlExpressionImpl(
                    decodeExpression(datetimeExpression, jpqlVisitorParameters, null));
            LOG.debug("visit: ASTSelectExpression value={}", value);
            node.setValue(value);
        } else if (node0 instanceof ASTStringExpression) {
            ASTStringExpression expression = (ASTStringExpression) node0;
            Value value = new SqlExpressionImpl(decodeExpression(expression, jpqlVisitorParameters, null));
            node.setValue(value);
        } else if (node0 instanceof ASTBooleanExpression) {
            ASTBooleanExpression expression = (ASTBooleanExpression) node0;
            Value value = new SqlExpressionImpl(decodeExpression(expression, jpqlVisitorParameters, null));
            node.setValue(value);
        }

        return object;
    }

    private FetchParameter createScalarExpressionFetchParameter(
            ASTScalarExpression scalarExpression) {
        Node node0 = scalarExpression.jjtGetChild(0);
        Value value = scalarExpression.getValue();

        if (node0 instanceof ASTArithmeticExpression) {
            return new BasicFetchParameter("arithmeticExpression", null);
        } else if (node0 instanceof ASTDatetimeExpression) {
            SqlExpressionImpl sqlExpressionImpl = (SqlExpressionImpl) value;
            if (sqlExpressionImpl.getExpression() instanceof Function) {
                Function function = (Function) sqlExpressionImpl.getExpression();
                if (function instanceof CurrentDate) {
                    return new BasicFetchParameter("datetimeExpression", Types.DATE);
                }
                if (function instanceof CurrentTime) {
                    return new BasicFetchParameter("datetimeExpression", Types.TIME);
                }
                if (function instanceof CurrentTimestamp) {
                    return new BasicFetchParameter("datetimeExpression", Types.TIMESTAMP);
                }
                // switch (sqlFunction) {
                // case CURRENT_DATE:
                // return new FetchParameter("datetimeExpression", java.sql.Date.class,
                // java.sql.Date.class, Types.DATE, null, null, false);
                // case CURRENT_TIME:
                // return new FetchParameter("datetimeExpression", java.sql.Time.class,
                // java.sql.Time.class, Types.TIME, null, null, false);
                // case CURRENT_TIMESTAMP:
                // return new FetchParameter("datetimeExpression", java.sql.Timestamp.class,
                // java.sql.Timestamp.class, Types.TIMESTAMP, null, null, false);
                // }
            } else {
                return new BasicFetchParameter("datetimeExpression", -1);
            }
        } else if (node0 instanceof ASTBooleanExpression) {
            return new BasicFetchParameter("booleanExpression", Types.BOOLEAN);
        }

        return new BasicFetchParameter("scalarExpression", Types.VARCHAR);
    }

    @Override
    public Object visit(ASTEnumExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTAllOrAnyExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTExistsExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTNullComparisonExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        if (node.jjtGetNumChildren() > 0) {
            ASTSingleValuedPathExpression singleValuedPathExpression = (ASTSingleValuedPathExpression) node
                    .jjtGetChild(0);
            List<MetaAttribute> metaAttributes = singleValuedPathExpression.getMetaAttributes();
            TableColumn tableColumn = new TableColumn(
                    singleValuedPathExpression.getFromTable(),
                    new Column(metaAttributes.get(0).getColumnName()));
            Condition condition;
            if (node.isNot()) {
                condition = new UnaryCondition(ConditionType.IS_NOT_NULL, tableColumn);
            } else {
                condition = new UnaryCondition(ConditionType.IS_NULL, tableColumn);
            }

            node.setCondition(condition);
        } else {
            // ...
        }

        return object;
    }

    @Override
    public Object visit(ASTLikeExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        Node n0 = node.jjtGetChild(0);
        LOG.debug("ASTLikeExpression: n0={}", n0);
        Object left = decodeExpression((ASTStringExpression) n0, jpqlVisitorParameters, null);
        Object right = null;
        if (node.getPatternValue() != null) {
            right = CriteriaUtils.buildValue(node.getPatternValue());
        } else if (node.getInputParameter() != null) {
            right = CriteriaUtils.QM;
//            QueryParameter queryParameter = buildQueryParameter(left, jpqlVisitorParameters.parameterMap, node.getInputParameter());
            QueryParameter queryParameter = buildQueryParameter(left, node.getInputParameter());
            jpqlVisitorParameters.parameters.add(queryParameter);
        }

        Condition condition = new LikeCondition(left, right, node.getEscapeCharacter(), node.isNot());
        node.setCondition(condition);
        return object;
    }

//    private QueryParameter buildQueryParameter(
//            Object column,
//            Map<Parameter<?>, Object> parameterMap,
//            String inputParameter) {
//        Optional<Object> optional = ParameterUtils.findParameterValue(parameterMap, inputParameter);
//        if (optional.isEmpty())
//            throw new SemanticException("Input parameter '" + inputParameter + "' not found");
//
//        QueryParameter queryParameter = new QueryParameter(
//                column,
//                optional.get(),
//                JdbcTypes.sqlTypeFromClass(optional.get().getClass()));
//        queryParameter.setInputParameter(inputParameter);
//        return queryParameter;
//    }

    private QueryParameter buildQueryParameter(
            Object column,
            String inputParameter) {
        QueryParameter queryParameter = new QueryParameter(
                column,
                null,
                null);
        queryParameter.setInputParameter(inputParameter);
        return queryParameter;
    }

    @Override
    public Object visit(ASTInItem node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTInExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        Node n0 = node.jjtGetChild(0);
        TableColumn tableColumn = null;
        if (n0 instanceof ASTStateValuedPathExpression) {
            ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0;
            String path = stateValuedPathExpression.getPath();
            LOG.debug("ASTInExpression: ASTStateValuedPathExpression path={}", path);
            Object[] sqlPath = splitJpqlPath(path, jpqlVisitorParameters);
            tableColumn = buildTableColumnFromPath(sqlPath);
        }

        if (node.jjtGetNumChildren() > 1) {
            List<Object> items = new ArrayList<>();
            if (node.jjtGetChild(1) instanceof ASTInItem) {
                for (int i = 1; i < node.jjtGetNumChildren(); ++i) {
                    ASTInItem inItem = (ASTInItem) node.jjtGetChild(i);
                    if (inItem.getLiteral() != null) {
                        items.add(inItem.getLiteral());
                    }
                }
            } else {
                // subquery
                ASTSubquery subquery = (ASTSubquery) node.jjtGetChild(1);
                items.add(subquery.getStatementParameters().getSqlStatement());
            }

            Condition condition = new InCondition(tableColumn, items, node.isNot());
            node.setCondition(condition);
        } else {
            // input parameter
        }

        return object;
    }

    @Override
    public Object visit(ASTSingleValuedPathExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        Node node0 = node.jjtGetChild(0);
        LOG.debug("visit: ASTSingleValuedPathExpression node0={}", node0);
        if (node0 instanceof ASTStateFieldPathExpression) {
            ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) node0;
            node.setIdentificationVariable(stateFieldPathExpression.getIdentificationVariable());
            node.setMapFieldIdentificationVariable(
                    stateFieldPathExpression.getMapFieldIdentificationVariable());
            node.setPath(stateFieldPathExpression.getPath());
            node.setStateField(stateFieldPathExpression.getStateField());
            if (node.getIdentificationVariable() != null) {
                String sqlTableAlias = jpqlVisitorParameters.aliases.get(node.getIdentificationVariable());
                Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
                if (optional.isEmpty()) {
                    throw new SemanticException(
                            "Entity not found for alias '" + node.getIdentificationVariable() + "'");
                }

                MetaEntity metaEntity = optional.get();

                String attributePath = stateFieldPathExpression.getStateField();
                if (!stateFieldPathExpression.getPath().isEmpty()) {
                    attributePath =
                            stateFieldPathExpression.getPath() + "." + stateFieldPathExpression.getStateField();
                }

                node.setMetaEntity(metaEntity);
                node.setAttributePath(attributePath);

//                if (AttributeUtil.isAttributePathPk(attributePath, metaEntity)) {
                if (metaEntity.getId().getName().equals(attributePath)) {
                    node.setMetaAttributes(metaEntity.getId().getAttributes());
                } else {
                    MetaAttribute metaAttribute = (MetaAttribute) metaEntity.findAttributeFromPath(
                            attributePath);
                    if (metaAttribute == null) {
                        throw new SemanticException(
                                "Attribute path '" + attributePath + "' on '" + metaEntity.getName()
                                        + "' entity not found");
                    }

                    node.setMetaAttributes(List.of(metaAttribute));
                }

                node.setFromTable(FromTable.of(metaEntity.getTableName(), sqlTableAlias));
            }
        }

        return object;
    }

    private List<MetaAttribute> createAttributesFromSingleValuedPathExpression(
            ASTSingleValuedPathExpression singleValuedPathExpression,
            JpqlVisitorParameters jpqlVisitorParameters) {
        Node node1 = singleValuedPathExpression.jjtGetChild(0);
        if (node1 instanceof ASTStateFieldPathExpression) {
            ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) node1;
            // identification variable.path.stateField
            String identificationVariable = stateFieldPathExpression.getIdentificationVariable();
            if (identificationVariable != null) {
                LOG.debug("visit: ASTSelectExpression stateFieldPathExpression.getStateField()="
                        + stateFieldPathExpression.getStateField());

                String sqlTableAlias = jpqlVisitorParameters.aliases.get(identificationVariable);
                Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
                if (optional.isEmpty()) {
                    throw new SemanticException(
                            "Entity not found for alias '" + identificationVariable + "'");
                }

                MetaEntity metaEntity = optional.get();
                String attributePath = singleValuedPathExpression.getAttributePath();
                if (metaEntity.getId().getName().equals(attributePath)) {
//                if (AttributeUtil.isAttributePathPk(attributePath, metaEntity)) {
                    List<TableColumn> values = MetaEntityHelper.toValues(metaEntity.getId().getAttributes(),
                            FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                    jpqlVisitorParameters.values.addAll(values);

                    List<FetchParameter> fetchParameters = new ArrayList<>();
                    metaEntity.getId().getAttributes().forEach(v -> {
                        fetchParameters.add(AttributeFetchParameter.build(v));
                    });
                    jpqlVisitorParameters.fetchParameters.addAll(fetchParameters);
                } else {
                    AbstractMetaAttribute metaAttribute = metaEntity.findAttributeFromPath(attributePath);
                    if (metaAttribute == null) {
                        throw new SemanticException(
                                "Attribute path '" + attributePath + "' on '" + metaEntity.getName()
                                        + "' entity not found");
                    }

                    TableColumn tableColumn = new TableColumn(
                            FromTable.of(metaEntity.getTableName(), sqlTableAlias),
                            new Column(metaAttribute.getColumnName()));
                    jpqlVisitorParameters.values.addAll(Arrays.asList(tableColumn));
                    jpqlVisitorParameters.fetchParameters
                            .addAll(List.of(AttributeFetchParameter.build(metaAttribute)));
                }
            }
        }

        return new ArrayList<>();
    }

    @Override
    public Object visit(ASTGroupByItem node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGroupByClause node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        GroupBy groupBy = new GroupBy();
        for (Node n : node.children) {
            ASTGroupByItem groupbyItem = (ASTGroupByItem) n;
            if (groupbyItem.jjtGetNumChildren() == 0) {
                // identification variable
            } else {
                Node node0 = groupbyItem.jjtGetChild(0);
                if (node0 instanceof ASTSingleValuedPathExpression) {
                    ASTSingleValuedPathExpression singleValuedPathExpression = (ASTSingleValuedPathExpression) node0;
                    List<MetaAttribute> metaAttributes = singleValuedPathExpression.getMetaAttributes();
                    List<TableColumn> values = MetaEntityHelper.toValues(metaAttributes,
                            singleValuedPathExpression.getFromTable());
                    values.forEach(v -> groupBy.addColumn(v));
                }
            }
        }

        jpqlVisitorParameters.groupBy = groupBy;
        return object;
    }

    @Override
    public Object visit(ASTOrderByItem node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTOrderByClause node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        for (Node n : node.children) {
            ASTOrderByItem orderByItem = (ASTOrderByItem) n;
            if (orderByItem.jjtGetNumChildren() == 0) {
                // result variable
            } else {
                Node n0 = orderByItem.jjtGetChild(0);
                if (n0 instanceof ASTStateFieldPathExpression) {
                    ASTStateFieldPathExpression stateFieldPathExpression = (ASTStateFieldPathExpression) n0;
                    // identification variable.path.stateField
                    String identificationVariable = stateFieldPathExpression.getIdentificationVariable();
                    if (identificationVariable != null) {
                        LOG.debug("visit: ASTSelectExpression stateFieldPathExpression.getStateField()="
                                + stateFieldPathExpression.getStateField());
                        String sqlTableAlias = jpqlVisitorParameters.aliases.get(identificationVariable);
                        Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
                        if (optional.isEmpty()) {
                            throw new SemanticException(
                                    "Entity not found for alias '" + identificationVariable + "'");
                        }

                        MetaEntity metaEntity = optional.get();
                        String attributePath = stateFieldPathExpression.getStateField();
                        if (!stateFieldPathExpression.getPath().isEmpty()) {
                            attributePath = stateFieldPathExpression.getPath() + "."
                                    + stateFieldPathExpression.getStateField();
                        }

//                        if (AttributeUtil.isAttributePathPk(attributePath, metaEntity)) {
                        if (metaEntity.getId().getName().equals(attributePath)) {
                            List<TableColumn> values = MetaEntityHelper.toValues(
                                    metaEntity.getId().getAttributes(),
                                    FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                            values.forEach(v -> {
                                OrderBy orderBy = new OrderBy(v, orderByItem.getOrderByType());
                                jpqlVisitorParameters.orderByList.add(orderBy);
                            });
                        } else {
                            AbstractMetaAttribute metaAttribute = metaEntity.findAttributeFromPath(attributePath);
                            if (metaAttribute == null) {
                                throw new SemanticException("Attribute path '" + attributePath + "' on '"
                                        + metaEntity.getName() + "' entity not found");
                            }

                            TableColumn tableColumn = new TableColumn(
                                    FromTable.of(metaEntity.getTableName(), sqlTableAlias),
                                    new Column(metaAttribute.getColumnName()));

                            OrderBy orderBy = new OrderBy(tableColumn, orderByItem.getOrderByType());
                            jpqlVisitorParameters.orderByList.add(orderBy);
                        }
                    }
                }

            }
        }

        return object;
    }

    @Override
    public Object visit(ASTFunctionArg node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGeneralCaseExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTCaseExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSimpleSelectClause node, Object data) {
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        jpqlVisitorParameters.distinct = node.isDistinct();
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTDerivedCollectionMemberDeclaration node, Object data) {
        LOG.debug("visit: ASTDerivedCollectionMemberDeclaration");
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTTreatedDerivedPath node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSimpleDerivedPath node, Object data) {
        LOG.debug("visit: ASTSimpleDerivedPath");
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTGeneralDerivedPath node, Object data) {
        LOG.debug("visit: ASTGeneralDerivedPath");
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTDerivedPathExpression node, Object data) {
        LOG.debug("visit: ASTDerivedPathExpression");
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSubselectIdentificationVariableDeclaration node, Object data) {
        LOG.debug("visit: ASTSubselectIdentificationVariableDeclaration");
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        Object object = node.childrenAccept(this, data);
        Node n = node.jjtGetChild(0);
        if (n instanceof ASTIdentificationVariableDeclaration) {

        } else if (n instanceof ASTGeneralDerivedPath) {
            ASTGeneralDerivedPath generalDerivedPath = (ASTGeneralDerivedPath) n;
            String entityAlias = node.getEntityAlias();
            Node n1 = generalDerivedPath.jjtGetChild(0);
            if (n1 instanceof ASTSimpleDerivedPath) {
                ASTSimpleDerivedPath simpleDerivedPath = (ASTSimpleDerivedPath) n1;
                String iv = simpleDerivedPath.getIdentificationVariable();
                String sqlTableAlias = jpqlVisitorParameters.aliases.get(iv);
                Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
                if (optional.isEmpty()) {
                    throw new SemanticException("Entity not found for alias '" + iv + "'");
                }

                MetaEntity metaEntity = optional.get();

                String attributePath = simpleDerivedPath.getpath();
                AbstractMetaAttribute metaAttribute = metaEntity.findAttributeFromPath(attributePath);
                if (metaAttribute == null) {
                    throw new SemanticException(
                            "Attribute path '" + attributePath + "' on '" + metaEntity.getName()
                                    + "' entity not found");
                }

                if (metaAttribute instanceof RelationshipMetaAttribute) {
                    RelationshipMetaAttribute relationshipMetaAttribute = (RelationshipMetaAttribute) metaAttribute;
                    FromTable fromTable = FromTable
                            .of(relationshipMetaAttribute.getRelationship().getAttributeType().getTableName(),
                                    tableAliasGenerator
                                            .getDefault(
                                                    relationshipMetaAttribute.getRelationship().getAttributeType()
                                                            .getTableName()));
                    jpqlVisitorParameters.fromTables.add(fromTable);
                    if (relationshipMetaAttribute.getRelationship().getJoinTable() != null) {
                        RelationshipJoinTable relationshipJoinTable = relationshipMetaAttribute.getRelationship()
                                .getJoinTable();
                        String tableAlias = tableAliasGenerator
                                .getDefault(relationshipJoinTable.getTargetEntity().getTableName());
                        jpqlVisitorParameters.aliases.put(entityAlias, tableAlias);
                        jpqlVisitorParameters.fromTables.add(
                                new FromTableImpl(relationshipJoinTable.getTableName(),
                                        tableAliasGenerator.getDefault(relationshipJoinTable.getTableName())));
                    } else {
                        String tableAlias = tableAliasGenerator
                                .getDefault(
                                        relationshipMetaAttribute.getRelationship().getAttributeType().getTableName());
                        jpqlVisitorParameters.aliases.put(entityAlias, tableAlias);
                    }

                    Condition condition = dbConfiguration.getSqlStatementFactory().generateJoinCondition(
                            relationshipMetaAttribute.getRelationship(), metaEntity,
                            relationshipMetaAttribute.getRelationship().getAttributeType(), tableAliasGenerator);
                    jpqlVisitorParameters.conditions.add(condition);
                }

                LOG.debug("visit: ASTSubselectIdentificationVariableDeclaration metaAttribute={}",
                        metaAttribute);
            }
        } else if (n instanceof ASTDerivedCollectionMemberDeclaration) {

        }

        return object;
    }

    @Override
    public Object visit(ASTSubqueryFromClause node, Object data) {
        Object object = node.childrenAccept(this, data);
        return object;
    }

    @Override
    public Object visit(ASTSubquery node, Object data) {
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        JpqlVisitorParameters jvp = new JpqlVisitorParameters();
        jvp.aliases.putAll(jpqlVisitorParameters.aliases);
        Object object = node.childrenAccept(this, jvp);

        StatementParameters statementParameters = (StatementParameters) createSelectFromParameters(jvp);
        node.setStatementParameters(statementParameters);
        return jpqlVisitorParameters;
    }

    @Override
    public Object visit(ASTAggregateExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        Object argument = null;
        if (node.getAggregateFunctionType() == AggregateFunctionType.COUNT
                && node.jjtGetNumChildren() == 0) {
            // identification_variable
            String sqlTableAlias = jpqlVisitorParameters.aliases.get(node.getIdentificationVariable());
            Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
            if (optional.isEmpty()) {
                throw new SemanticException(
                        "Entity not found for alias '" + node.getIdentificationVariable() + "'");
            }

            MetaEntity metaEntity = optional.get();
            argument = new TableColumn(FromTable.of(metaEntity.getTableName(), sqlTableAlias),
                    new Column(metaEntity.getId().getAttributes().get(0).getColumnName()));
        } else {
            Node n0 = node.jjtGetChild(0);
            if (n0 instanceof ASTStateValuedPathExpression) {
                ASTStateValuedPathExpression stateValuedPathExpression = (ASTStateValuedPathExpression) n0;
                argument = createValueFrom(stateValuedPathExpression, jpqlVisitorParameters);
            } else if (n0 instanceof ASTFunctionsReturningStrings) {
                Node functionNode = n0.jjtGetChild(0);
                argument = decodeFunction(functionNode, jpqlVisitorParameters, null);
            } else if (n0 instanceof ASTFunctionsReturningNumerics) {
                Node functionNode = n0.jjtGetChild(0);
                argument = decodeFunction(functionNode, jpqlVisitorParameters, null);
            }
        }

        Value value = FunctionUtils.createAggregateFunction(node.getAggregateFunctionType(), argument,
                node.isDistinct());
        node.setValue(value);
        return object;
    }

    private FetchParameter createAggregateExpressionFetchParameter(
            ASTAggregateExpression aggregateExpression) {
        AggregateFunctionType aggregateFunctionType = aggregateExpression.getAggregateFunctionType();
        if (aggregateFunctionType == AggregateFunctionType.AVG) {
            return new BasicFetchParameter("aggregateExpression", null);
        }

        if (aggregateFunctionType == AggregateFunctionType.COUNT) {
            return new BasicFetchParameter("aggregateExpression", null);
        }

        if (aggregateFunctionType == AggregateFunctionType.SUM) {
            return new BasicFetchParameter("aggregateExpression", null);
        }

        return null;
        // return new FetchParameter("aggregateExpression", null, Types.BIGINT, null);
    }

    /**
     * Splits the given Jpql path to a Sql path.
     *
     * @param jpqlPath              Jpql path
     * @param jpqlVisitorParameters visitor parameters
     * @return a string array like [table alias, table name, column name]
     */
    private Object[] splitJpqlPath(String jpqlPath, JpqlVisitorParameters jpqlVisitorParameters) {
        String[] sps = jpqlPath.split("\\.");
        if (sps.length == 1) {
            String sqlTableAlias = jpqlVisitorParameters.aliases.get(sps[0]);
            Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
            if (optional.isEmpty()) {
                throw new SemanticException("Entity not found for alias '" + sps[0] + "'");
            }

            MetaEntity metaEntity = optional.get();

            Object[] result = {sqlTableAlias, metaEntity,
                    metaEntity.getId().getAttributes().get(0).getColumnName()};
            return result;
        } else {
            String identificationVariable = sps[0];
            String attributePath = jpqlPath.substring(identificationVariable.length() + 1);
            String sqlTableAlias = jpqlVisitorParameters.aliases.get(sps[0]);
            Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
            if (optional.isEmpty()) {
                throw new SemanticException("Entity not found for alias '" + sps[0] + "'");
            }

            MetaEntity metaEntity = optional.get();

            AbstractMetaAttribute metaAttribute = metaEntity.findAttributeFromPath(attributePath);
            if (metaAttribute == null) {
                throw new SemanticException(
                        "Attribute path '" + attributePath + "' on '" + metaEntity.getName()
                                + "' entity not found");
            }

            Object[] result = {sqlTableAlias, metaEntity, metaAttribute.getColumnName()};
            return result;
        }
    }

    private Value createValueFrom(ASTStateValuedPathExpression stateValuedPathExpression,
                                  JpqlVisitorParameters jpqlVisitorParameters) {
        String path = stateValuedPathExpression.getPath();
        LOG.debug("createValueFrom: ASTStateValuedPathExpression path={}", path);
        Object[] sqlPath = splitJpqlPath(path, jpqlVisitorParameters);
        return buildTableColumnFromPath(sqlPath);
    }

    @Override
    public Object visit(ASTSimpleSelectExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        // identification_variable
        if (node.jjtGetNumChildren() == 0) {
            String identificationVariable = node.getIdentificationVariable();
            String sqlTableAlias = jpqlVisitorParameters.aliases.get(identificationVariable);
            Optional<MetaEntity> optional = findMetaEntityBySqlAlias(sqlTableAlias);
            if (optional.isEmpty()) {
                throw new SemanticException("Entity not found for alias '" + identificationVariable + "'");
            }

            MetaEntity metaEntity = optional.get();
            if (jpqlVisitorParameters.distinct) {
                List<TableColumn> values = MetaEntityHelper.toValues(metaEntity.getId().getAttributes(),
                        FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                jpqlVisitorParameters.values.addAll(values);
            } else {
                List<Value> values = MetaEntityHelper.toValues(metaEntity,
                        FromTable.of(metaEntity.getTableName(), sqlTableAlias));
                jpqlVisitorParameters.values.addAll(values);
            }

            return object;
        }

        Node n = node.jjtGetChild(0);
        if (n instanceof ASTAggregateExpression) {
            jpqlVisitorParameters.values.add(((ASTAggregateExpression) n).getValue());
        } else if (n instanceof ASTScalarExpression) {
            jpqlVisitorParameters.values.add(((ASTScalarExpression) n).getValue());
        } else if (n instanceof ASTSingleValuedPathExpression) {
            ASTSingleValuedPathExpression singleValuedPathExpression = (ASTSingleValuedPathExpression) n;
            List<MetaAttribute> metaAttributes = singleValuedPathExpression.getMetaAttributes();
            List<TableColumn> values = MetaEntityHelper.toValues(metaAttributes,
                    singleValuedPathExpression.getFromTable());
            jpqlVisitorParameters.values.addAll(values);
        }

        return object;
    }

    @Override
    public Object visit(ASTEntityTypeExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTTypeDiscriminator node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTEntityTypeExpressionComparison node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTBetweenExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        ConditionNode conditionNode = (ConditionNode) node.jjtGetChild(0);
        node.setCondition(conditionNode.getCondition());
        return object;
    }

    @Override
    public Object visit(ASTArithmeticBetweenExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        ASTArithmeticExpression arithmeticExpression = (ASTArithmeticExpression) node.jjtGetChild(0);
        List<Object> r0 = new ArrayList<>();
        processArithmeticExpressionResult(arithmeticExpression, r0);
        BetweenCondition.Builder builder = new BetweenCondition.Builder(r0);

        arithmeticExpression = (ASTArithmeticExpression) node.jjtGetChild(1);
        r0 = new ArrayList<>();
        processArithmeticExpressionResult(arithmeticExpression, r0);
        builder.withLeftExpression(r0);

        arithmeticExpression = (ASTArithmeticExpression) node.jjtGetChild(2);
        r0 = new ArrayList<>();
        processArithmeticExpressionResult(arithmeticExpression, r0);
        builder.withRightExpression(r0);

        builder.withNot(node.isNot());

        node.setCondition(builder.build());
        return object;
    }

    @Override
    public Object visit(ASTStringBetweenExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        ASTStringExpression expression = (ASTStringExpression) node.jjtGetChild(0);
        Object queryParameterObject = decodeExpression(expression, jpqlVisitorParameters, null);
        BetweenCondition.Builder builder = new BetweenCondition.Builder(queryParameterObject);

        expression = (ASTStringExpression) node.jjtGetChild(1);
        builder.withLeftExpression(decodeExpression(expression, jpqlVisitorParameters, queryParameterObject));

        expression = (ASTStringExpression) node.jjtGetChild(2);
        builder.withRightExpression(decodeExpression(expression, jpqlVisitorParameters, queryParameterObject));

        builder.withNot(node.isNot());

        node.setCondition(builder.build());
        return object;
    }

    @Override
    public Object visit(ASTDatetimeBetweenExpression node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;
        ASTDatetimeExpression expression = (ASTDatetimeExpression) node.jjtGetChild(0);
        Object queryParameterObject = decodeExpression(expression, jpqlVisitorParameters, null);
        BetweenCondition.Builder builder = new BetweenCondition.Builder(
                queryParameterObject);

        expression = (ASTDatetimeExpression) node.jjtGetChild(1);
        builder.withLeftExpression(decodeExpression(expression, jpqlVisitorParameters, queryParameterObject));

        expression = (ASTDatetimeExpression) node.jjtGetChild(2);
        builder.withRightExpression(decodeExpression(expression, jpqlVisitorParameters, queryParameterObject));

        builder.withNot(node.isNot());

        node.setCondition(builder.build());
        return object;
    }

    @Override
    public Object visit(ASTConstructorExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTFunctionsReturningStrings node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTConcatFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSubstringFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTTrimFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTLowerFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTUpperFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTFunctionsReturningNumerics node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTLengthFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTLocateFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTAbsFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSqrtFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTModFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTSizeFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTIndexFunction node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTEntityOrValueExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTCollectionMemberExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTEmptyCollectionComparisonExpression node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTConstructorItem node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTFetchJoin node, Object data) {
        Object object = node.childrenAccept(this, data);
        JpqlVisitorParameters jpqlVisitorParameters = (JpqlVisitorParameters) data;

        JoinType jt = node.getJoinType();
        LOG.debug("visit: join.jjtGetNumChildren()={}", node.jjtGetNumChildren());
        ASTJoinAssociationPathExpression joinAssociationPathExpression = (ASTJoinAssociationPathExpression) node
                .jjtGetChild(0);
        String identificationVariable = node.getIdentificationVariable();
        LOG.debug("visit: joinAlias={}", identificationVariable);
        buildJoin(jpqlVisitorParameters, jt, joinAssociationPathExpression, identificationVariable);
        jpqlVisitorParameters.statementType = StatementType.FETCH_JOIN;
        return object;
    }

    @Override
    public Object visit(ASTCollectionMemberDeclaration node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(ASTFunctionInvocation node, Object data) {
        return node.childrenAccept(this, data);
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        return node.childrenAccept(this, data);
    }

}
