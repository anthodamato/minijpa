# minijpa
minijpa is a JPA 2.2 Spec implementation (currently partially implemented).  
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
@JoinColumn  
@JoinColumns  
@ManyToMany  
@ManyToOne  
@MappedSuperclass  
@OneToMany  
@OneToOne  
@SequenceGenerator  
@SqlResultSetMapping  
@Table  
@Temporal  
@Version  


XML mapping is currently not supported.  

## Supported Databases  
- **Apache Derby** *10.15.2.0*  
- **MySQL** *8.0.28*  
- **PostgreSQL** *13.3*  
- **MariaDB** *10.5.15*  
- **Oracle** *12.2.0.1*  
- **H2** *2.1.210*  

## Building  
Built using Java SE 11    
1. Install Java SE 11  
2. Install Maven 3.6.3 or higher  
3. git clone https://github.com/adamatolondon/minijpa.git
4. cd minijpa  
5. mvn clean package -Dmaven.test.skip  
6. mvn test  

## Running the unit tests  
- **Apache Derby**:  
     - mvn test  
  

- **MySQL**:  
    - mvn test -Dminijpa.test=mysql  
  

- **PostgreSQL**  
    - mvn test -Dminijpa.test=postgres  
  

- **MariaDB**  
    - mvn test -Dminijpa.test=mariadb  
  

- **Oracle**  
    - mvn test -Dminijpa.test=oracle  
  

- **H2**  
    - mvn test -Dminijpa.test=h2  
  
## License  
[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)  

## Implementation Notes  
##### Jdbc level  
- Reading from database when attribute types are known:  
(for example using a 'findById'). In this case entity class and attribute types are known.  
In case the attribute type is simple like Integer, String, etc. the building of the value is straightforward but in other cases a conversion is needed. For example, the @Enumerated annotation requires a conversion, from Varchar or Integer to Enumeration. Also, the Boolean type is stored as Integer in Oracle databases. The conversion is made using an AttributeMapper (very similar to JPA AttributeConverter).  
- Reading from database when attribute types are unknown, for example, using native queries:  
the sql type returned by ResultSet is used  

##### Jpql  
Obtained from official JPA documentation the Jpql grammar is here '/minijpa-core/jpql/BNF2.txt'.  
The Jpql parser (not completed) is generated using [JavaCC v7.0.10](https://javacc.github.io/javacc/).  
The JJTree grammar file is '/minijpa-core/jpql/JpqlParser.jjt'.  
Class generation is done using jjtree and javacc commands:  
- jjtree JpqlParser.jjt  
- javacc JpqlParser.jj  

##### Entity class enhancement  
Entity class enhancement is made using a Java agent (EntityAgent) and [Javassist](https://www.javassist.org/).  
After entity enhancement the minijpa metamodel is generated. It's a set of classes like MetaEntity, MetaAttribute, etc.  

## Extra Configuration  
##### Connection Pool support  
Currently, [c3p0](https://www.mchange.com/projects/c3p0/) and [DBCP](https://commons.apache.org/proper/commons-dbcp/) are supported in an application-managed entity manager. Here is a persistence.xml configuration example for c3p0:  

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
The property "c3p0.datasource" with the value "true" enables the connection pool.

			<property name="c3p0.datasource" value="true"></property>




