create table job_employee (id integer not null, jd varchar(255), name varchar(255), pm_id integer, primary key (id));

create table program_manager (id integer not null, name varchar(255) not null, primary key (id));

alter table job_employee add constraint pm_fk foreign key (pm_id) references program_manager;

