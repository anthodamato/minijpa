create table citizen (id integer primary key, first_name varchar(100), last_name varchar(100), version int);

create sequence SEQ_GEN_SEQUENCE;

create table address (id int generated always as identity primary key, name varchar(100), postcode varchar(100), tt boolean);
