create sequence CITY_PK_SEQ;

create table city (id integer primary key, name varchar(100), region_id int);

create sequence REGION_PK_SEQ;

create table region (id integer primary key, name varchar(100), population int);
