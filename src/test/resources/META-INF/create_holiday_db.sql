create sequence HOLIDAY_PK_SEQ;

create table holiday (id int primary key, travellers int, checkin date, nights int, referenceName varchar(100));
