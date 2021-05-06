create table booking (dateof date not null, room_number integer not null, customer_id integer, primary key (dateof, room_number));

create table booking_sale (id bigint not null, perc integer not null, b_dateof date, b_room_number integer, primary key (id));

create sequence BOOKING_SALE_PK_SEQ;

alter table booking_sale add constraint FK68sgx7a1ydv6j101gaavq9x9g foreign key (b_dateof, b_room_number) references booking;
