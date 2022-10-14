create sequence TRIANGLE_PK_SEQ;

create table triangle (id integer primary key, sides integer not null, area integer);

create sequence SQUARE_PK_SEQ;

create table square (id integer primary key, sides integer not null, area integer);
