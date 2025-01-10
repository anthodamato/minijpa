/*
 * Copyright 2021 Antonio Damato <anto.damato@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.minijpa.jpa.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minijpa.jdbc.DDLData;
import org.minijpa.jdbc.PkSequenceGenerator;
import org.minijpa.jpa.model.*;
import org.minijpa.jpa.model.relationship.*;
import org.minijpa.sql.model.ColumnDeclaration;
import org.minijpa.sql.model.CompositeSqlPk;
import org.minijpa.sql.model.ForeignKeyDeclaration;
import org.minijpa.sql.model.JdbcDDLData;
import org.minijpa.sql.model.JdbcJoinColumnMapping;
import org.minijpa.sql.model.SimpleSqlPk;
import org.minijpa.sql.model.SqlCreateJoinTable;
import org.minijpa.sql.model.SqlCreateSequence;
import org.minijpa.sql.model.SqlCreateTable;
import org.minijpa.sql.model.SqlDDLStatement;
import org.minijpa.sql.model.SqlPk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDbJdbc implements DbJdbc {

    private final Logger log = LoggerFactory.getLogger(AbstractDbJdbc.class);

    @Override
    public PkStrategy findPkStrategy(PkGenerationType pkGenerationType) {
        if (pkGenerationType == null) {
            return PkStrategy.PLAIN;
        }

        if (pkGenerationType == PkGenerationType.IDENTITY) {
            return PkStrategy.IDENTITY;
        }

        if (pkGenerationType == PkGenerationType.SEQUENCE
                || pkGenerationType == PkGenerationType.AUTO) {
            return PkStrategy.SEQUENCE;
        }

        return PkStrategy.PLAIN;
    }

    private int indexOfFirstEntity(List<MetaEntity> entities) {
        for (int i = 0; i < entities.size(); ++i) {
            MetaEntity metaEntity = entities.get(i);
            List<RelationshipMetaAttribute> relationshipAttributes = metaEntity.expandRelationshipAttributes();
            if (relationshipAttributes.isEmpty()) {
                return i;
            }

            long rc = relationshipAttributes.stream().filter(a -> a.getRelationship().isOwner()).count();
            if (rc == 0) {
                return i;
            }
        }

        return 0;
    }

    private List<MetaEntity> sortForDDL(List<MetaEntity> entities) {
        List<MetaEntity> sorted = new ArrayList<>();
        List<MetaEntity> toSort = new ArrayList<>(entities);
        for (int i = 0; i < entities.size(); ++i) {
            int index = indexOfFirstEntity(toSort);
            sorted.add(toSort.get(index));
            toSort.remove(index);
        }

        return sorted;
    }

    private ColumnDeclaration toColumnDeclaration(AbstractMetaAttribute a) {
        Optional<JdbcDDLData> optional = Optional.empty();
        log.debug("toColumnDeclaration: a={}", a);
        if (a.getDdlData().isPresent()) {
            DDLData ddlData = a.getDdlData().get();
            JdbcDDLData jdbcDDLData = new JdbcDDLData(ddlData.getColumnDefinition(), ddlData.getLength(),
                    ddlData.getPrecision(), ddlData.getScale(), ddlData.getNullable(), ddlData.getUnique());
            optional = Optional.of(jdbcDDLData);
        }

        return new ColumnDeclaration(a.getColumnName(), a.getDatabaseType(), optional);
    }


    private SqlPk buildJdbcPk(Pk pk) {
        if (pk.isIdClass()) {
            List<ColumnDeclaration> columnDeclarations = new ArrayList<>();
            for (AbstractMetaAttribute abstractMetaAttribute : pk.getAttributes()) {
                columnDeclarations.add(toColumnDeclaration(abstractMetaAttribute));
            }

            IdClassPk idClassPk = (IdClassPk) pk;
            List<ColumnDeclaration> constraintColumnDeclarations = new ArrayList<>(columnDeclarations);
            if (idClassPk.getRelationshipMetaAttribute() != null) {
                if (idClassPk.getRelationshipMetaAttribute().getRelationship().getJoinColumnMapping().isPresent()) {
                    List<JoinColumnAttribute> joinColumnAttributes = idClassPk.getRelationshipMetaAttribute().getRelationship().getJoinColumnMapping().get().getJoinColumnAttributes();
                    for (JoinColumnAttribute joinColumnAttribute : joinColumnAttributes) {
                        constraintColumnDeclarations.add(new ColumnDeclaration(joinColumnAttribute.getColumnName(), joinColumnAttribute.getDatabaseType()));
                    }
                }
            }

            return new CompositeSqlPk(columnDeclarations, constraintColumnDeclarations);
        }

        if (pk.isComposite()) {
            List<ColumnDeclaration> columnDeclarations = new ArrayList<>();
            for (AbstractMetaAttribute abstractMetaAttribute : pk.getAttributes()) {
                columnDeclarations.add(toColumnDeclaration(abstractMetaAttribute));
            }

            return new CompositeSqlPk(columnDeclarations, columnDeclarations);
        }

        return new SimpleSqlPk(toColumnDeclaration(pk.getAttribute()),
                pk.getPkGeneration().getPkStrategy() == PkStrategy.IDENTITY);
    }


    @Override
    public List<SqlDDLStatement> buildDDLStatementsCreateTables(
            Map<String, MetaEntity> entities,
            List<MetaEntity> sorted) {
        List<SqlDDLStatement> sqlStatements = new ArrayList<>();
        sorted.forEach(v -> {
            List<MetaAttribute> attributes = new ArrayList<>(v.getBasicAttributes());
            attributes.addAll(v.expandEmbeddables());

            // foreign keys
            List<JoinColumnMapping> joinColumnMappings = v.expandJoinColumnMappings();
            List<ForeignKeyDeclaration> foreignKeyDeclarations = new ArrayList<>();
            for (JoinColumnMapping joinColumnMapping : joinColumnMappings) {
                MetaEntity toEntity = entities.get(joinColumnMapping.getAttribute().getType().getName());
                JdbcJoinColumnMapping jdbcJoinColumnMapping = JdbcSqlStatementFactory
                        .toJdbcJoinColumnMapping(joinColumnMapping, false);
                foreignKeyDeclarations.add(
                        new ForeignKeyDeclaration(jdbcJoinColumnMapping, toEntity.getTableName()));
            }

            log.debug("buildDDLStatementsCreateTables: v.getTableName()={}", v.getTableName());
            List<ColumnDeclaration> columnDeclarations = attributes.stream()
                    .map(this::toColumnDeclaration).collect(Collectors.toList());

            SqlCreateTable sqlCreateTable = new SqlCreateTable(v.getTableName(), buildJdbcPk(v.getId()),
                    columnDeclarations, foreignKeyDeclarations);
            sqlStatements.add(sqlCreateTable);
        });
        return sqlStatements;
    }


    protected SqlCreateSequence toJdbcSequenceParams(PkSequenceGenerator pkSequenceGenerator) {
        SqlCreateSequence sqlCreateSequence = new SqlCreateSequence();
        sqlCreateSequence.setAllocationSize(pkSequenceGenerator.getAllocationSize());
        sqlCreateSequence.setCatalog(pkSequenceGenerator.getCatalog());
        sqlCreateSequence.setInitialValue(pkSequenceGenerator.getInitialValue());
//        sqlCreateSequence.setName(pkSequenceGenerator.getName());
        sqlCreateSequence.setSchema(pkSequenceGenerator.getSchema());
        sqlCreateSequence.setSequenceName(pkSequenceGenerator.getSequenceName());
        return sqlCreateSequence;
    }

    @Override
    public List<SqlDDLStatement> buildDDLStatementsCreateSequences(List<MetaEntity> sorted) {
        List<PkSequenceGenerator> pkSequenceGenerators = sorted.stream()
                .filter(c -> c.getId().getPkGeneration().getPkStrategy() == PkStrategy.SEQUENCE)
                .map(c -> c.getId().getPkGeneration().getPkSequenceGenerator()).distinct()
                .collect(Collectors.toList());

        return pkSequenceGenerators.stream()
                .map(this::toJdbcSequenceParams).collect(Collectors.toList());
    }

    @Override
    public List<SqlDDLStatement> buildDDLStatementsCreateJoinTables(List<MetaEntity> sorted) {
        List<SqlDDLStatement> sqlStatements = new ArrayList<>();
        sorted.forEach(v -> {
            List<Relationship> relationships = v.expandRelationshipAttributes().stream()
                    .filter(a -> a.getRelationship().getJoinTable() != null && a.getRelationship().isOwner())
                    .map(RelationshipMetaAttribute::getRelationship).collect(Collectors.toList());
            for (Relationship relationship : relationships) {
                RelationshipJoinTable relationshipJoinTable = relationship.getJoinTable();
                List<ForeignKeyDeclaration> foreignKeyDeclarations = new ArrayList<>();
                JdbcJoinColumnMapping owningJdbcJoinColumnMapping = JdbcSqlStatementFactory
                        .toJdbcJoinColumnMapping(relationshipJoinTable.getOwningJoinColumnMapping(), false);
                foreignKeyDeclarations.add(new ForeignKeyDeclaration(owningJdbcJoinColumnMapping,
                        relationshipJoinTable.getOwningEntity().getTableName()));
                boolean unique = relationship instanceof OneToManyRelationship;
                JdbcJoinColumnMapping targetJdbcJoinColumnMapping = JdbcSqlStatementFactory
                        .toJdbcJoinColumnMapping(relationshipJoinTable.getTargetJoinColumnMapping(), unique);
                foreignKeyDeclarations.add(new ForeignKeyDeclaration(targetJdbcJoinColumnMapping,
                        relationshipJoinTable.getTargetEntity().getTableName()));
                SqlCreateJoinTable sqlCreateJoinTable = new SqlCreateJoinTable(
                        relationshipJoinTable.getTableName(),
                        foreignKeyDeclarations);
                sqlStatements.add(sqlCreateJoinTable);
            }
        });

        return sqlStatements;
    }

    @Override
    public List<SqlDDLStatement> buildDDLStatements(Map<String, MetaEntity> entities) {
        List<MetaEntity> sorted = sortForDDL(new ArrayList<>(entities.values()));
        List<SqlDDLStatement> ddlStatementsCreateTables = buildDDLStatementsCreateTables(entities,
                sorted);
        List<SqlDDLStatement> sqlStatements = new ArrayList<>(ddlStatementsCreateTables);

        List<SqlDDLStatement> ddlStatementsCreateSequences = buildDDLStatementsCreateSequences(sorted);
        sqlStatements.addAll(ddlStatementsCreateSequences);

        List<SqlDDLStatement> ddlStatementsCreateJoinTables = buildDDLStatementsCreateJoinTables(
                sorted);
        sqlStatements.addAll(ddlStatementsCreateJoinTables);

        return sqlStatements;
    }

}
