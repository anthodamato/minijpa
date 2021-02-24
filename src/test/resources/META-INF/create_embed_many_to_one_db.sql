create table JobEmployee (id integer not null, jobDescription varchar(255), name varchar(255), pm_id integer, primary key (id));

create table ProgramManager (id integer not null, primary key (id));

alter table JobEmployee add constraint pm_fk foreign key (pm_id) references ProgramManager;

