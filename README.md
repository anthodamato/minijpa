# minijpa
minijpa is a JPA 2.2 Spec implementation (currently, partially implemented).  
List of implemented (or partially implemented) annotations:  

@Basic  
@Column  
@Entity  
@Embeddable  
@Embedded  
@EmbeddedId  
@Enumerated  
@GeneratedValue  
@Id  
@IdClass  
@JoinColumn  
@JoinColumns  
@ManyToMany  
@ManyToOne  
@MappedSuperclass  
@NamedQueries  
@NamedQuery  
@NamedNativeQueries  
@NamedNativeQuery  
@OneToMany  
@OneToOne  
@SequenceGenerator  
@SqlResultSetMapping  
@Table  
@Temporal  
@Transient  
@Version  


XML mapping is currently not supported.  

## Supported Databases  
- **H2** *2.1.210* and higher  
- **MySQL** *8.0.28* and higher  
- **PostgreSQL** *13.3* and higher  
- **MariaDB** *10.5.15* and higher  
- **Oracle** *12.2.0.1*  
- **Apache Derby** *10.15.2.0*

## Building  
Built using Java SE 11    
1. Install Java SE 11  
2. Install Maven 3.6.3 or higher  
3. git clone https://github.com/adamatolondon/minijpa.git
4. cd minijpa  
5. mvn clean install -Dmaven.test.skip  
6. mvn test  

Running Jacoco Report  
- mvn test jacoco:report  

## Setting up test database on Docker
- **MySQL**:  
     - docker run --name mysql9 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql:9.7.1  
     - docker exec -it mysql9 bash
     - mysql -u root -ppassword
     - create database minijpatest;
- **PostgreSQL**
     - docker run --name postgres18 -p5432:5432 -e POSTGRES_PASSWORD=password -d postgres:18.4
     - docker exec -it postgres18 bash
     - psql -Upostgres
     - create database minijpatest;
- **MariaDB**
     - docker run -p 3306:3306  --name mariadb12 -e MARIADB_ROOT_PASSWORD=password -d mariadb:12.3.2
     - docker exec -it mariadb12 bash
     - mariadb -uroot -ppassword
     - create database minijpatest;


## Running the unit tests  
- **H2**:  
     - mvn test  

- **MySQL**:  
    - mvn test -Dminijpa.test=mysql  

- **PostgreSQL**  
    - mvn test -Dminijpa.test=postgres  

- **MariaDB**  
    - mvn test -Dminijpa.test=mariadb  

- **Oracle**  
    - mvn test -Dminijpa.test=oracle  

- **Apache Derby**  
    - mvn test -Dminijpa.test=derby  
  

## Extra Configuration  
##### Connection Pool support  
Currently, [c3p0](https://www.mchange.com/projects/c3p0/) and [DBCP](https://commons.apache.org/proper/commons-dbcp/) are supported as connection pools. Here is a persistence.xml configuration example for c3p0:  

            <property name="c3p0.datasource" value="true"></property>
            <property name="c3p0.initialPoolSize" value="5"></property>
            <property name="c3p0.minPoolSize" value="5"></property>
            <property name="c3p0.maxPoolSize" value="20"></property>
            <property name="c3p0.acquireIncrement" value="2"></property>
            <property name="c3p0.maxIdleTime" value="40"></property>
            <property name="c3p0.maxStatements" value="2"></property>
            <property name="c3p0.maxStatementsPerConnection" value="3"></property>

and for DBCP:  

            <property name="dbcp.datasource" value="true"></property>
            <property name="dbcp.initialSize" value="5"></property>
            <property name="dbcp.maxTotal" value="8"></property>
            <property name="dbcp.maxIdle" value="3"></property>
            <property name="dbcp.minIdle" value="0"></property>
            <property name="dbcp.maxWaitMillis" value="2000"></property>

The library must be in the classpath.  
The property "c3p0.datasource" set to "true" enables the connection pool.

            <property name="c3p0.datasource" value="true"></property>

##### Multiple Join Split  
In order to avoid cartesian product with multiple joins built with criteria you can use the query hint *QueryHints.SPLIT_MULTIPLE_JOINS*.
MultipleJoinTest class contains an example test method.

## Metamodel Class Generation  
Metamodel classes can be generated using the '*org.minijpa.jpa.metamodel.generator.MetamodelGenerator*' procedure provided by the module 'minijpa-metamodel-generator' with the following parameters:  

1. persistence unit name  
2. persistence xml file path  
3. destination folder  


Example:  

java -cp minijpa-metamodel-generator/target/minijpa-metamodel-generator-0.0.1-SNAPSHOT.jar:"/Users/myuser/workspace/minijpa/minijpa-core/target/test-classes" org.minijpa.jpa.metamodel.generator.MetamodelGenerator mapped_superclass /Users/myuser/workspace/minijpa/minijpa-core/src/test/resources/META-INF/persistence.xml /Users/myuser/workspace/minijpa/minijpa-core/src/test/java

## Implementation Notes
##### Modules
- **minijpa-core**:  
  Core JPA implementation
- **minijpa-jdbc**:  
  JDBC layer. Executes SQL statements
- **minijpa-sql-model**:  
  Converts the model generated in minijpa-core in Sql Statements
- **minijpa-metamodel-generator**:  
  Generates JPA Metamodel classes

##### JPQL  
Obtained from official JPA documentation the Jpql grammar is here '/minijpa-core/jpql/BNF2.txt'.  
The Jpql parser (not completed) is generated using [JavaCC v7.0.10](https://javacc.github.io/javacc/).  
The JJTree grammar file is '/minijpa-core/jpql/JpqlParser.jjt'.  
Class generation is done using jjtree and javacc commands:  
- jjtree JpqlParser.jjt  
- javacc JpqlParser.jj  

##### Entity class enhancement  
Entity class enhancement is made using a Java agent (EntityAgent) and [Javassist](https://www.javassist.org/).  
After entity enhancement the minijpa metamodel is generated. It's a set of classes like MetaEntity, MetaAttribute, etc.  

## License
[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)  




