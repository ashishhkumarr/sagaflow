create table processed_messages (
    message_id uuid primary key,
    consumer varchar(64) not null,
    handled_at timestamptz not null
);
