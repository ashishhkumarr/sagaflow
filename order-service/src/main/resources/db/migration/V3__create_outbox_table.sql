create table outbox (
    id uuid primary key,
    topic varchar(64) not null,
    message_key varchar(64) not null,
    payload text not null,
    correlation_id varchar(64),
    created_at timestamptz not null,
    sent_at timestamptz
);

-- the poller only ever looks at rows that have not gone out yet, so the index only
-- covers those and stays small even when the table has a lot of history in it
create index idx_outbox_unsent on outbox (created_at) where sent_at is null;
