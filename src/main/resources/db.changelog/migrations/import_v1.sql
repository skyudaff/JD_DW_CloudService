-- changeset skyudaff: 1
create table users
(
    id           bigserial primary key,
    login        varchar(255) not null unique,
    password     varchar(255) not null,
    created_date timestamp    not null default now()
);
-- rollback drop table users;

-- changeset skyudaff: 2
create table files
(
    id           serial primary key,
    hash         varchar(255) not null,
    file_name    varchar(255) not null,
    size         bigint       not null,
    file_bytes   OID          not null,
    type         varchar(255) not null,
    created_date timestamp    not null default now(),
    is_deleted   boolean               default false,
    user_id      bigint references files (id)
);
-- rollback drop table files;

-- changeset skyudaff: 3
create table users_roles
(
    user_id bigint references users (id),
    roles   varchar(15) default 'ROLE_USER'
);
-- rollback drop table users_roles;