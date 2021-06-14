# minijpa
Mini JPA implementation

## Running the tests

Apache Derby 10.15.2.0:  

mvn test  

MySQL 8.0.25:  

mvn test -Dminijpa.test=mysql  

PostgreSQL 13.3:  

mvn test -Dminijpa.test=postgres  

MariaDB 10.5.10:  

mvn test -Dminijpa.test=mariadb  

Oracle 12.2.0.1:  

mvn test -Dminijpa.test=oracle  
