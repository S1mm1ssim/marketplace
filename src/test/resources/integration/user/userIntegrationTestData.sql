INSERT INTO company(id, name, email, created, description, is_deleted)
VALUES (999, 'company', 'company@company.com', now(), 'description', false),
       (1000, 'company2', 'company2@company2.com', now(), 'description', false),
       (1001, 'softDeletedCompany', 'softDeleted@company.com', now(), 'description', true);