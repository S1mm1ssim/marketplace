INSERT INTO company(id, name, email, created, description, is_deleted)
VALUES (999, 'company', 'company@company.com', now(), 'description', false),
       (1000, 'company2', 'company2@company2.com', now(), 'description', false),
       (1001, 'softDeletedCompany', 'softDeleted@company.com', now(), 'description', true);


INSERT INTO "user"(id, username, email, name, role, created, updated, company_id)
VALUES ('b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d', 'user', 'sqluser@user.com', 'full name', 'MANAGER', now(), now(), 1000),
       ('097f575a-8e9d-4efe-ac01-d0d692262f4b', 'user', 'sqluser2@user.com', 'full name', 'MANAGER', now(), now(),
        1000),
       ('722cd920-e127-4cc2-93b9-e9b4a8f18873', 'toBeDeleted', 'toBeDeleted@mail.com', 'toBeDeleted', 'STORAGE_MANAGER',
        now(), now(), 999),
       ('c048ef0e-fe46-4c65-9c01-d88af74ba0ab', 'softDeleted', 'softDeleted@user.com', 'full name', 'MANAGER', now(),
        now(), 1001);