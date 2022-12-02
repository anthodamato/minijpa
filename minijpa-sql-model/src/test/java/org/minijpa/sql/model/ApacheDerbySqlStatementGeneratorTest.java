package org.minijpa.sql.model;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.minijpa.sql.model.aggregate.GroupBy;
import org.minijpa.sql.model.condition.BetweenCondition;
import org.minijpa.sql.model.condition.BinaryCondition;
import org.minijpa.sql.model.condition.BinaryLogicCondition;
import org.minijpa.sql.model.condition.BinaryLogicConditionImpl;
import org.minijpa.sql.model.condition.Condition;
import org.minijpa.sql.model.condition.ConditionType;
import org.minijpa.sql.model.condition.InCondition;
import org.minijpa.sql.model.condition.LikeCondition;
import org.minijpa.sql.model.condition.UnaryCondition;
import org.minijpa.sql.model.condition.UnaryLogicCondition;
import org.minijpa.sql.model.condition.UnaryLogicConditionImpl;
import org.minijpa.sql.model.expression.SqlBinaryExpression;
import org.minijpa.sql.model.expression.SqlBinaryExpressionImpl;
import org.minijpa.sql.model.expression.SqlExpressionOperator;
import org.minijpa.sql.model.function.Abs;
import org.minijpa.sql.model.function.Avg;
import org.minijpa.sql.model.function.Concat;
import org.minijpa.sql.model.function.Count;
import org.minijpa.sql.model.function.CurrentDate;
import org.minijpa.sql.model.function.CurrentTime;
import org.minijpa.sql.model.function.CurrentTimestamp;
import org.minijpa.sql.model.function.Length;
import org.minijpa.sql.model.function.Locate;
import org.minijpa.sql.model.function.Lower;
import org.minijpa.sql.model.function.Max;
import org.minijpa.sql.model.function.Min;
import org.minijpa.sql.model.function.Mod;
import org.minijpa.sql.model.function.Sqrt;
import org.minijpa.sql.model.function.Substring;
import org.minijpa.sql.model.function.Sum;
import org.minijpa.sql.model.function.Trim;
import org.minijpa.sql.model.function.TrimType;
import org.minijpa.sql.model.function.Upper;
import org.minijpa.sql.model.join.FromJoin;
import org.minijpa.sql.model.join.FromJoinImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheDerbySqlStatementGeneratorTest {
    private final Logger LOG = LoggerFactory.getLogger(ApacheDerbySqlStatementGeneratorTest.class);

    private static final SqlStatementGenerator sqlStatementGenerator = new ApacheDerbySqlStatementGenerator();

    @BeforeAll
    static void init() {
        sqlStatementGenerator.init();
    }

    @Test
    public void simpleSelect() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        List<Condition> conditions = Arrays.asList(binaryCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select c.id from citizen AS c where c.first_name = 'Sam'",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void orderBy() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column dobColumn = new Column("dob");
        Column allColumn = new Column("*");
        List<Value> values = Arrays.asList(new TableColumn(fromTable, allColumn));
        OrderBy orderBy = new OrderBy(new TableColumn(fromTable, dobColumn), OrderByType.DESC);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values)
                .withOrderBy(Arrays.asList(orderBy)).build();
        Assertions.assertEquals("select c.* from citizen AS c order by c.dob DESC",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void delete() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        SqlDelete sqlDelete = new SqlDelete(fromTable, Optional.of(binaryCondition));
        Assertions.assertEquals("delete from citizen AS c where c.first_name = 'Sam'",
                sqlStatementGenerator.export(sqlDelete));
    }

    @Test
    public void binaryLogicCondition() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        BinaryCondition binaryCondition1 = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        BinaryCondition binaryCondition2 = new BinaryCondition.Builder(ConditionType.NOT_EQUAL)
                .withLeft(new TableColumn(fromTable, surnameColumn)).withRight("'Smith'").build();
        BinaryLogicCondition binaryLogicCondition1 = new BinaryLogicConditionImpl(ConditionType.AND,
                Arrays.asList(binaryCondition1, binaryCondition2), true);

        LikeCondition likeCondition = new LikeCondition(new TableColumn(fromTable, nameColumn), "'Ed%'", null);
        BinaryCondition binaryCondition4 = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, surnameColumn)).withRight("'Smith'").build();
        BinaryLogicCondition binaryLogicCondition2 = new BinaryLogicConditionImpl(ConditionType.AND,
                Arrays.asList(likeCondition, binaryCondition4), true);

        BinaryLogicCondition binaryLogicCondition = new BinaryLogicConditionImpl(ConditionType.OR,
                Arrays.asList(binaryLogicCondition1, binaryLogicCondition2));

        List<Condition> conditions = Arrays.asList(binaryLogicCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals(
                "select c.id from citizen AS c where (c.first_name = 'Sam' and c.last_name <> 'Smith') or (c.first_name like 'Ed%' and c.last_name = 'Smith')",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void unaryLogicCondition() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        BinaryCondition binaryCondition1 = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, nameColumn)).withRight("'Sam'").build();
        BinaryCondition binaryCondition2 = new BinaryCondition.Builder(ConditionType.NOT_EQUAL)
                .withLeft(new TableColumn(fromTable, surnameColumn)).withRight("'Smith'").build();
        BinaryLogicCondition binaryLogicCondition1 = new BinaryLogicConditionImpl(ConditionType.AND,
                Arrays.asList(binaryCondition1, binaryCondition2));

        UnaryLogicCondition unaryLogicCondition = new UnaryLogicConditionImpl(ConditionType.NOT, binaryLogicCondition1);

        List<Condition> conditions = Arrays.asList(unaryLogicCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals(
                "select c.id from citizen AS c where not (c.first_name = 'Sam' and c.last_name <> 'Smith')",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void unaryConditionEqualsTrue() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column genderColumn = new Column("gender");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        Condition condition = new UnaryCondition(ConditionType.EQUALS_TRUE, new TableColumn(fromTable, genderColumn));

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select c.id from citizen AS c where c.gender = TRUE",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void unaryConditionEqualsFalse() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column genderColumn = new Column("gender");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        Condition condition = new UnaryCondition(ConditionType.EQUALS_FALSE, new TableColumn(fromTable, genderColumn));

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select c.id from citizen AS c where c.gender = FALSE",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void unaryConditionIsNull() {
        FromTable fromTable = new FromTableImpl("account", "a");
        Column idColumn = new Column("id");
        Column expiryDateColumn = new Column("expiry_date");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        Condition condition = new UnaryCondition(ConditionType.IS_NULL, new TableColumn(fromTable, expiryDateColumn));

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select a.id from account AS a where a.expiry_date is null",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void unaryConditionIsNotNull() {
        FromTable fromTable = new FromTableImpl("account", "a");
        Column idColumn = new Column("id");
        Column expiryDateColumn = new Column("expiry_date");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        Condition condition = new UnaryCondition(ConditionType.IS_NOT_NULL,
                new TableColumn(fromTable, expiryDateColumn));

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select a.id from account AS a where a.expiry_date is not null",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void betweenCondition() {
        FromTable fromTable = new FromTableImpl("account", "a");
        Column idColumn = new Column("id");
        Column expiryDateColumn = new Column("expiry_date");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        Condition condition = new BetweenCondition(new TableColumn(fromTable, expiryDateColumn), "'01-01-2000'",
                "'01-01-2020'", true);

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals(
                "select a.id from account AS a where a.expiry_date not between '01-01-2000' and '01-01-2020'",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void unaryConditionInOperator() {
        FromTable fromTable = new FromTableImpl("account", "a");
        Column idColumn = new Column("id");
        Column statusColumn = new Column("status");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));
        List<Object> params = Arrays.asList("'CLOSED'", "'SUSPENDED'");
        Condition condition = new InCondition(new TableColumn(fromTable, statusColumn), params, true);

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select a.id from account AS a where a.status not in ('CLOSED', 'SUSPENDED')",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void distinct() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, nameColumn));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).distinct().build();
        Assertions.assertEquals("select distinct c.first_name from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void sum() {
        FromTable fromTable = new FromTableImpl("region", "r");
        Column populationColumn = new Column("population");

        List<Value> values = Arrays.asList(new Sum(new TableColumn(fromTable, populationColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select SUM(r.population) from region AS r", sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void count() {
        FromTable fromTable = new FromTableImpl("product", "p");
        List<Value> values = Arrays.asList(new Count("*"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select COUNT(*) from product AS p", sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void groupBy() {
        FromTable fromTable = new FromTableImpl("product", "p");
        Column categoryColumn = new Column("category");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, categoryColumn), new Count("*"));
        GroupBy groupBy = new GroupBy(new TableColumn(fromTable, categoryColumn));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withGroupBy(groupBy).build();
        Assertions.assertEquals("select p.category, COUNT(*) from product AS p group by p.category",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void innerJoin() {
        Column regionIdColumn = new Column("id");

        Column nameColumn = new Column("name");
        Column regionColumn = new Column("region_id");
        FromTable cityTable = new FromTableImpl("city", "c");
        FromTable regionTable = new FromTableImpl("region", "r");
        FromJoin fromJoin = new FromJoinImpl(cityTable, regionTable.getAlias().get(), Arrays.asList(regionIdColumn),
                Arrays.asList(regionColumn));

        Column regionNameColumn = new Column("name");

        List<Value> values = Arrays.asList(new TableColumn(regionTable, regionNameColumn));
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(cityTable, nameColumn)).withRight("'Nottingham'").build();
        List<Condition> conditions = Arrays.asList(binaryCondition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(regionTable).withJoin(fromJoin).withValues(values)
                .withConditions(conditions).build();

        Assertions.assertEquals(
                "select r.name from region AS r INNER JOIN city AS c ON r.id = c.region_id where c.name = 'Nottingham'",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void insert() {
        Column idColumn = new Column("id");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        SqlInsert sqlInsert = new SqlInsert(FromTable.of("citizen"), Arrays.asList(idColumn, nameColumn, surnameColumn),
                false, false, Optional.empty());
        Assertions.assertEquals("insert into citizen (id,first_name,last_name) values (?,?,?)",
                sqlStatementGenerator.export(sqlInsert));
    }

    @Test
    public void update() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column idColumn = new Column("id");
        Column surnameColumn = new Column("last_name");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight(456).build();

        SqlUpdate sqlUpdate = new SqlUpdate(fromTable, Arrays.asList(new TableColumn(fromTable, surnameColumn)),
                Optional.of(binaryCondition));
        Assertions.assertEquals("update citizen AS c set c.last_name = ? where c.id = 456",
                sqlStatementGenerator.export(sqlUpdate));
    }

    @Test
    public void updateNotEqual() {
        FromTable fromTable = new FromTableImpl("product");
        Column categoryColumn = new Column("category");
        TableColumn tableColumn = new TableColumn(fromTable, categoryColumn);
        Column idColumn = new Column("id");
        BinaryCondition binaryCondition = new BinaryCondition.Builder(ConditionType.NOT_EQUAL)
                .withLeft(new TableColumn(fromTable, idColumn)).withRight("1").build();

        SqlUpdate sqlUpdate = new SqlUpdate(fromTable, Arrays.asList(tableColumn), Optional.of(binaryCondition));
        Assertions.assertEquals("update product set category = ? where id <> 1",
                sqlStatementGenerator.export(sqlUpdate));
    }

    @Test
    public void createTable() {
        JdbcJoinColumnMapping jdbcJoinColumnMapping = new SingleJdbcJoinColumnMapping(
                new ColumnDeclaration("address_id", Long.class),
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)));
        ForeignKeyDeclaration foreignKeyDeclaration = new ForeignKeyDeclaration(jdbcJoinColumnMapping, "address");

        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("dob", java.sql.Date.class),
                        new ColumnDeclaration("citizenship", Boolean.class)),
                Arrays.asList(foreignKeyDeclaration));
        Assertions.assertEquals(
                "create table citizen (id bigint, first_name varchar(50), last_name varchar(50), dob date, citizenship boolean, address_id bigint, primary key (id), foreign key (address_id) references address)",
                sqlStatementGenerator.export(sqlCreateTable));
    }

    @Test
    public void createTableIdentityColumn() {
        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class), true),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("dob", java.sql.Date.class),
                        new ColumnDeclaration("citizenship", Boolean.class)));
        Assertions.assertEquals(
                "create table citizen (id bigint generated by default as identity, first_name varchar(50), last_name varchar(50), dob date, citizenship boolean, primary key (id))",
                sqlStatementGenerator.export(sqlCreateTable));
    }

    @Test
    public void createJoinTable() {
        JdbcJoinColumnMapping jdbcJoinColumnMapping1 = new SingleJdbcJoinColumnMapping(
                new ColumnDeclaration("citizen_id", Long.class),
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)));
        ForeignKeyDeclaration foreignKeyDeclaration1 = new ForeignKeyDeclaration(jdbcJoinColumnMapping1, "citizen");

        JdbcJoinColumnMapping jdbcJoinColumnMapping2 = new SingleJdbcJoinColumnMapping(
                new ColumnDeclaration("address_id", Long.class),
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)));
        ForeignKeyDeclaration foreignKeyDeclaration2 = new ForeignKeyDeclaration(jdbcJoinColumnMapping2, "address");

        SqlCreateJoinTable sqlCreateJoinTable = new SqlCreateJoinTable("citizen_address",
                Arrays.asList(foreignKeyDeclaration1, foreignKeyDeclaration2));
        Assertions.assertEquals(
                "create table citizen_address (citizen_id bigint not null, address_id bigint not null, foreign key (citizen_id) references citizen, foreign key (address_id) references address)",
                sqlStatementGenerator.export(sqlCreateJoinTable));
    }

    @Test
    public void createDataTypeTable() {
        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.of(16), Optional.of(2),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("datatype",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("counter", Integer.class),
                        new ColumnDeclaration("percentage", Float.class),
                        new ColumnDeclaration("division", Double.class),
                        new ColumnDeclaration("big_number", BigDecimal.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("dob", java.sql.Timestamp.class),
                        new ColumnDeclaration("timehh", Time.class)),
                Arrays.asList());
        Assertions.assertEquals(
                "create table datatype (id bigint, counter integer, percentage real, division double precision, big_number decimal(16,2), dob timestamp, timehh time, primary key (id))",
                sqlStatementGenerator.export(sqlCreateTable));
    }

    @Test
    public void createSequence() {
        SqlCreateSequence sqlCreateSequence = new SqlCreateSequence();
        sqlCreateSequence.setSequenceName("address_seq");
        sqlCreateSequence.setInitialValue(1);
        sqlCreateSequence.setAllocationSize(1);

        Assertions.assertEquals("create sequence address_seq start with 1 increment by 1",
                sqlStatementGenerator.export(sqlCreateSequence));
    }

    @Test
    public void exportDddlList() {
        JdbcDDLData jdbcDDLData = new JdbcDDLData(Optional.empty(), Optional.of(50), Optional.empty(), Optional.empty(),
                Optional.empty());
        SqlCreateTable sqlCreateTable = new SqlCreateTable("citizen",
                new SimpleSqlPk(new ColumnDeclaration("id", Long.class)),
                Arrays.asList(new ColumnDeclaration("first_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("last_name", String.class, Optional.of(jdbcDDLData)),
                        new ColumnDeclaration("dob", java.sql.Date.class),
                        new ColumnDeclaration("citizenship", Boolean.class)),
                Collections.emptyList());

        SqlCreateSequence sqlCreateSequence = new SqlCreateSequence();
        sqlCreateSequence.setSequenceName("citizen_seq");
        sqlCreateSequence.setInitialValue(1);
        sqlCreateSequence.setAllocationSize(1);
        List<String> ddls = sqlStatementGenerator.export(Arrays.asList(sqlCreateTable, sqlCreateSequence));
        Assertions.assertEquals(2, ddls.size());
        Assertions.assertEquals(
                "create table citizen (id bigint, first_name varchar(50), last_name varchar(50), dob date, citizenship boolean, primary key (id))",
                ddls.get(0));
        Assertions.assertEquals("create sequence citizen_seq start with 1 increment by 1", ddls.get(1));
    }

    @Test
    public void abs() {
        FromTable fromTable = new FromTableImpl("temperature", "t");
        Column minColumn = new Column("min_temp");
        List<Value> values = Arrays.asList(new Abs(new TableColumn(fromTable, minColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select ABS(t.min_temp) from temperature AS t",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void mod() {
        FromTable fromTable = new FromTableImpl("statistic", "s");
        Column occColumn = new Column("occurrences");
        List<Value> values = Arrays
                .asList(new SelectItem(new Mod(new TableColumn(fromTable, occColumn), 2), Optional.of("modulus")));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select MOD(s.occurrences, 2) AS modulus from statistic AS s",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void sqrt() {
        FromTable fromTable = new FromTableImpl("statistic", "s");
        Column occColumn = new Column("occurrences");
        List<Value> values = Arrays
                .asList(new SelectItem(new Sqrt(new TableColumn(fromTable, occColumn)), Optional.of("square_root")));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select SQRT(s.occurrences) AS square_root from statistic AS s",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void avg() {
        FromTable fromTable = new FromTableImpl("temperature", "t");
        Column minColumn = new Column("min_temp");
        List<Value> values = Arrays.asList(new Avg(new TableColumn(fromTable, minColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select AVG(t.min_temp) from temperature AS t",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void concat() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");
        Column surnameColumn = new Column("last_name");

        List<Value> values = Arrays.asList(
                new Concat(new TableColumn(fromTable, nameColumn), "' '", new TableColumn(fromTable, surnameColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select c.first_name||' '||c.last_name from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void length() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        List<Value> values = Arrays.asList(new Length(new TableColumn(fromTable, nameColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select LENGTH(c.first_name) from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void locate() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(new Locate("'a'", new TableColumn(fromTable, nameColumn)),
                Optional.of("position"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select LOCATE('a', c.first_name) AS position from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void lower() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(new Lower(new TableColumn(fromTable, nameColumn)), Optional.of("lw"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select LOWER(c.first_name) AS lw from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void upper() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(new Upper(new TableColumn(fromTable, nameColumn)), Optional.of("uw"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select UPPER(c.first_name) AS uw from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void trim() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(
                new Trim(new TableColumn(fromTable, nameColumn), Optional.of(TrimType.BOTH), "\""),
                Optional.of("name"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select TRIM(BOTH '\"' FROM c.first_name) AS name from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void min() {
        FromTable fromTable = new FromTableImpl("temperature", "t");
        Column minColumn = new Column("min_temp");
        List<Value> values = Arrays.asList(new Min(new TableColumn(fromTable, minColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select MIN(t.min_temp) from temperature AS t",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void max() {
        FromTable fromTable = new FromTableImpl("temperature", "t");
        Column minColumn = new Column("min_temp");
        List<Value> values = Arrays.asList(new Max(new TableColumn(fromTable, minColumn)));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select MAX(t.min_temp) from temperature AS t",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void substring() {
        FromTable fromTable = new FromTableImpl("citizen", "c");
        Column nameColumn = new Column("first_name");

        SelectItem selectItem = new SelectItem(new Substring(new TableColumn(fromTable, nameColumn), 2),
                Optional.of("name"));
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(Arrays.asList(selectItem)).build();
        Assertions.assertEquals("select SUBSTR(c.first_name, 2) AS name from citizen AS c",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void currentDate() {
        FromTable fromTable = new FromTableImpl("flights", "f");
        Column idColumn = new Column("id");
        Column availDateColumn = new Column("avail_date");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));

        Condition condition = new BinaryCondition.Builder(ConditionType.GREATER_THAN)
                .withLeft(new TableColumn(fromTable, availDateColumn)).withRight(new CurrentDate()).build();

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select f.id from flights AS f where f.avail_date > CURRENT_DATE",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void currentTimestamp() {
        FromTable fromTable = new FromTableImpl("flights", "f");
        Column idColumn = new Column("id");
        Column availDateColumn = new Column("avail_date");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));

        Condition condition = new BinaryCondition.Builder(ConditionType.GREATER_THAN)
                .withLeft(new TableColumn(fromTable, availDateColumn)).withRight(new CurrentTimestamp()).build();

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select f.id from flights AS f where f.avail_date > CURRENT_TIMESTAMP",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void currentTime() {
        FromTable fromTable = new FromTableImpl("flights", "f");
        Column idColumn = new Column("id");
        Column availDateColumn = new Column("start_time");

        List<Value> values = Arrays.asList(new TableColumn(fromTable, idColumn));

        Condition condition = new BinaryCondition.Builder(ConditionType.GREATER_THAN)
                .withLeft(new TableColumn(fromTable, availDateColumn)).withRight(new CurrentTime()).build();

        List<Condition> conditions = Arrays.asList(condition);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).withConditions(conditions)
                .build();
        Assertions.assertEquals("select f.id from flights AS f where f.start_time > CURRENT_TIME",
                sqlStatementGenerator.export(sqlSelect));
    }

    @Test
    public void binaryExpression() {
        FromTable fromTable = new FromTableImpl("temperature", "t");
        Column minColumn = new Column("min_temp");
        Column maxColumn = new Column("max_temp");
        SqlBinaryExpression sqlBinaryExpression = new SqlBinaryExpressionImpl(SqlExpressionOperator.DIFF,
                new TableColumn(fromTable, maxColumn), new TableColumn(fromTable, minColumn));
        SelectItem selectItem = new SelectItem(sqlBinaryExpression, Optional.of("difference"));
        List<Value> values = Arrays.asList(selectItem);
        SqlSelectBuilder sqlSelectBuilder = new SqlSelectBuilder();
        SqlSelect sqlSelect = sqlSelectBuilder.withFromTable(fromTable).withValues(values).build();
        Assertions.assertEquals("select t.max_temp-t.min_temp AS difference from temperature AS t",
                sqlStatementGenerator.export(sqlSelect));
    }

}
