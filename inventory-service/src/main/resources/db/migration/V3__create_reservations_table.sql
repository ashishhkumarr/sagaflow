create table reservations (
    order_id uuid primary key,
    item varchar(120) not null,
    quantity int not null,
    status varchar(20) not null,
    created_at timestamptz not null
);
