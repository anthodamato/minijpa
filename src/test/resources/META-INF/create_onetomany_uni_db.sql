create table store (id integer primary key, name varchar(100));

create sequence STORE_PK_SEQ;

create table item (id integer primary key, name varchar(100), model varchar(100));

create sequence ITEM_PK_SEQ;

create table store_item (store_id integer, items_id integer);
