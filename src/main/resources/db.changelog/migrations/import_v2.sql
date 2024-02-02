-- changeset skyudaff: 4
insert into users (login, password)
values ('user@example.org', '$2a$10$z9YCXxYP6BcMbm6sUmcWQOs72B1iPiJn7dG6BNZFHaUb8io.JJqii'),
       ('admin@example.org', '$2a$10$.Ls/oFMwqO4HcCeKGGQgKepODrTd.GiG9q3aqbpEen2z6vmPXD.1W');

insert into users_roles (user_id, roles)
values (1, 'ROLE_USER'),
       (2, 'ROLE_ADMIN');