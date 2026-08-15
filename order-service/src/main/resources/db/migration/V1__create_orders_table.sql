create table orders (
    id uuid primary key,
    customer_id varchar(64) not null,
    item varchar(120) not null,
    quantity int not null,
    amount numeric(10, 2) not null,
    status varchar(32) not null,
    created_at timestamptz not null
);

create index idx_orders_status on orders (status);
