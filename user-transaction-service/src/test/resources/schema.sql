CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA user_transaction_service;

CREATE TYPE user_transaction_service.transaction_status AS ENUM ('IN_PROGRESS', 'SUCCESS', 'REJECTED');

create table user_transaction_service.user_transaction
(
    id      bigint generated by default as identity,
    user_id uuid                                                       not null,
    created timestamp                                                  not null,
    status  user_transaction_service.transaction_status
        default 'SUCCESS'::user_transaction_service.transaction_status not null,
    constraint user_transaction_pkey
        primary key (id)
);

create table user_transaction_service."order"
(
    id                  bigint generated by default as identity,
    amount              double precision not null,
    position_id         bigint           not null,
    user_transaction_id bigint           not null,
    constraint order_pkey
        primary key (id),
    constraint fk_order_user_transaction
        foreign key (user_transaction_id) references user_transaction_service.user_transaction,
    constraint amount_check
        check (amount >= (0.01)::double precision)
);