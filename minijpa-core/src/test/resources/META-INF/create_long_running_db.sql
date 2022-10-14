create table random_data (id bigint not null, fib1 integer, fib2 integer, fib3 integer, fib4 integer, fib5 integer, fib_string varchar(255), multiply2 integer, primary key (id));
create table random_group (id bigint not null, name varchar(255), primary key (id));
create table random_group_random_data (RandomGroup_id bigint not null, randomDataValues_id bigint not null);
create sequence RANDOM_DATA_PK_SEQ start with 1 increment by 1;
create sequence RANDOM_GROUP_PK_SEQ start with 1 increment by 1;
alter table random_group_random_data add constraint FK51asrqptku7hl5tr4ywrokwas foreign key (randomDataValues_id) references random_data;
alter table random_group_random_data add constraint FKsydivfk0cixg6gertilqty573 foreign key (RandomGroup_id) references random_group;

