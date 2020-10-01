--create table book (id int primary key, title varchar(100), author varchar(100));

--create sequence APP.HIBERNATE_SEQUENCE;

create table book (id int generated always as identity primary key, title varchar(100), author varchar(100), format varchar(100) not null, pages int not null);
