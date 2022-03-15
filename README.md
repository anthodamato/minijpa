# minijpa
minijpa is a JPA 3.0 Spec implementation (currently partially implemented).  
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


XML mapping is not supported.  

## Implementation Notes  
- Reading from database when attribute types are known:  
(for example using a 'findById'). In this case entity class and attribute types are known.  
In case the attribute type is simple like Integer, String, etc. the building of the value is straightforward but in other cases a conversion is needed. For example, the @Enumerated annotation requires a conversion, from Varchar or Integer to Enumeration. Also, the Boolean type is stored as Integer in Oracle databases. The conversion is made using an AttributeMapper.  
- Reading from database using native queries:  
the sql type returned by ResultSet is used  

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
5. mvn install  

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

