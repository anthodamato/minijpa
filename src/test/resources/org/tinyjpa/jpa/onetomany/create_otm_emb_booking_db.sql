create table hotelbookingdetail (dateof date primary key, room_number int not null, price float not null);

create sequence HOTELCUSTOMER_PK_SEQ;

create table hotelcustomer (id integer primary key, name varchar(100));

create table hotelbookingdetail_hotelcustomer (hotelbookingdetail_dateof date, hotelbookingdetail_room_number int, customers_id integer);
