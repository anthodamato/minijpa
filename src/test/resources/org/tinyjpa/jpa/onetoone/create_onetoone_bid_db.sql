create sequence PERSON_PK_SEQ;
create table person (id integer primary key, name varchar(100), fingerprint_id int);

create sequence FINGERPRINT_PK_SEQ;
create table fingerprint (id integer primary key, type varchar(100));
