create table if not exists outbox (
                                    id uuid primary key,
                                    event_type varchar(80) not null,
                                    aggregate_type varchar(80) not null,
                                    aggregate_id uuid not null,
                                    payload jsonb not null,
                                    status varchar(20) not null,
                                    attempts int not null default 0,
                                    next_retry_at timestamptz not null,
                                    last_error text null,
                                    created_at timestamptz not null,
                                    updated_at timestamptz not null
);

create index if not exists ix_outbox_status_retry_created
  on outbox (status, next_retry_at, created_at);

create index if not exists ix_outbox_aggregate
  on outbox (aggregate_type, aggregate_id);
