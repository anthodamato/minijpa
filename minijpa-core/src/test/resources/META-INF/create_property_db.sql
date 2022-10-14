create table property (id bigint not null, address varchar(255), property_type varchar(255), primary key (id));

create table property_owner (id bigint not null, name varchar(255), primary key (id));

create table property_property_owner (property_id bigint not null, owners_id bigint not null);

alter table property_property_owner add constraint owners_id_fx foreign key (owners_id) references property_owner;

alter table property_property_owner add constraint property_id_fk foreign key (property_id) references property;

create sequence PROPERTY_PK_SEQ;

create sequence PROPERTY_OWNER_PK_SEQ;
