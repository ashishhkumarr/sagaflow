create table order_steps (
    id bigserial primary key,
    order_id uuid not null references orders (id),
    status varchar(32) not null,
    detail varchar(200),
    at timestamptz not null
);

create index idx_order_steps_order on order_steps (order_id, at);
