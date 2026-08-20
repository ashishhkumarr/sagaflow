create table payments (
    order_id uuid primary key,
    customer_id varchar(64) not null,
    amount numeric(10, 2) not null,
    status varchar(20) not null,
    reason varchar(200),
    created_at timestamptz not null
);
