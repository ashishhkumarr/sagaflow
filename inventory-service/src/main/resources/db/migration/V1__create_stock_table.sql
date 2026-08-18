create table stock (
    item varchar(120) primary key,
    available int not null check (available >= 0)
);
