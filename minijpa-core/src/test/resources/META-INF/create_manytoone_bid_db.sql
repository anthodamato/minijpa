create sequence EMPLOYEE_PK_SEQ;
create table employee (id integer primary key, name varchar(100), salary decimal(19,2), department_id int);

create sequence DEPARTMENT_PK_SEQ;
create table department (id integer primary key, name varchar(100));
