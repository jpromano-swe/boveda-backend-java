create table if not exists deposit_events (
                                            event_id uuid primary key,
                                            user_id uuid not null,
                                            source varchar(40) not null,
  external_event_id varchar(120) not null,
  amount numeric(19, 2) not null check (amount > 0),
  currency char(3) not null,
  occurred_at timestamptz not null,
  detected_at timestamptz not null,
  correlation_id varchar(100) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
  );

create unique index if not exists ux_deposit_events_source_external
  on deposit_events (source, external_event_id);

create index if not exists ix_deposit_events_user_occurred_at
  on deposit_events (user_id, occurred_at desc);
