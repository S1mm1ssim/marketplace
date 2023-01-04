CREATE SCHEMA company_service;

create table company_service.company
(
    id          bigint generated by default as identity,
    name        varchar(255)          not null,
    email       varchar(255)          not null,
    created     timestamp             not null,
    description text                  not null,
    is_deleted  boolean default false not null,
    constraint company_pkey
        primary key (id),
    constraint company_email_key
        unique (email)
);