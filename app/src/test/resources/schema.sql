CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

create table category
(
    id              bigint generated by default as identity,
    name            varchar(255) not null,
    parent_category bigint,
    description     text,
    constraint category_pkey
        primary key (id),
    constraint fk_category_parent
        foreign key (parent_category) references category
);

create table "user"
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

create table item
(
    id          uuid   default uuid_generate_v4() not null,
    name        varchar(255)                      not null,
    description text,
    created     timestamp                         not null,
    category_id bigint                            not null,
    version     bigint default 1                  not null,
    constraint item_pkey
        primary key (id),
    constraint fk_item_category
        foreign key (category_id) references category
);

create table position
(
    id         bigint generated by default as identity,
    item_id    uuid             not null,
    company_id bigint           not null,
    created_by uuid             not null,
    created    timestamp        not null,
    amount     double precision not null,
    version    bigint default 1 not null,
    min_amount double precision not null,
    constraint position_pkey
        primary key (id),
    constraint fk_position_item
        foreign key (item_id) references item
            on delete cascade,
    constraint fk_position_user
        foreign key (created_by) references "user"
            on delete cascade,
    constraint amount_check
        check (amount >= (0.01)::double precision),
    constraint min_amount_check
        check (min_amount >= (0.01)::double precision)
);