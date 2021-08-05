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


JPQL is not implemented yet.  
XML mapping is not supported.  

## Supported Databases  
- **Apache Derby** *10.15.2.0*  
- **MySQL** *8.0.25*  
- **PostgreSQL** *13.3*  
- **MariaDB** *10.5.10*  
- **Oracle** *12.2.0.1*  
- **H2** *1.4.200*  

## Building  
Built using Java SE 11 but easy to downgrade to Java SE 9  
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

