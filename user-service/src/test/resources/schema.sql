CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA user_service;

create table user_service."user"
(
    id         uuid default uuid_generate_v4() not null,
    username   varchar(255)                    not null,
    email      varchar(255)                    not null,
    name       varchar(255)                    not null,
    created    timestamp                       not null,
    updated    timestamp                       not null,
    company_id bigint                          not null,
    constraint user_pkey
        primary key (id),
    constraint user_email_key
        unique (email),
    constraint user_username_key
        unique (username)
);