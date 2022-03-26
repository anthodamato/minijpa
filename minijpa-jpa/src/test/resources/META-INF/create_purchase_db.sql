create table purchase_stats (id bigint not null, cash double, credit_card double, debit_card double, end_date date, start_date date, primary key (id));

create sequence PURCHASE_STATS_PK_SEQ;
