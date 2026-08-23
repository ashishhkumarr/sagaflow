alter table orders add column cancel_reason varchar(200);

-- anything already in the table was written before the saga states existed, so it
-- only ever had created_at to go on
alter table orders add column updated_at timestamptz;
update orders set updated_at = created_at where updated_at is null;
alter table orders alter column updated_at set not null;

create index idx_orders_status_updated_at on orders (status, updated_at);
