create sequence STATE_PK_SEQ;
create table state (id integer primary key, name varchar(100), capital_id int);

create sequence CAPITAL_PK_SEQ;
create table capital (id integer primary key, name varchar(100));
