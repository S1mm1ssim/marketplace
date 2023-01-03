-- INSERT INTO "user"(id, username, email, name, created, updated, company_id)
-- VALUES ('b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d', 'user1', 'sqluser1@user.com', 'full name', now(), now(), 999),
--        ('722cd920-e127-4cc2-93b9-e9b4a8f18873', 'user2', 'sqluser2@mail.com', 'full name', now(), now(), 1000),
--        ('c048ef0e-fe46-4c65-9c01-d88af74ba0ab', 'softDeleted', 'softDeleted@user.com', 'full name', now(), now(), 1001);

INSERT INTO category(id, name, parent_category, description)
VALUES (999, 'root', null, 'description'),
       (1000, 'electronics', 999, 'electronics');

INSERT INTO item(id, name, description, created, category_id)
VALUES ('b338b0c2-ae82-437f-a55c-8017c12895b4', 'item1', 'description', now(), 1000),
       ('10aaa42d-ddce-4d62-a612-c2a108dabd36', 'item2', 'description', now(), 1000),
       ('b6b7764c-ed62-47a4-a68d-3cad4da1e187', 'item3', 'description', now(), 1000);

INSERT INTO position(id, item_id, company_id, created_by, created, amount, min_amount)
VALUES (999, 'b338b0c2-ae82-437f-a55c-8017c12895b4', 1000, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now(), 15, 0.1),
       (1000, '10aaa42d-ddce-4d62-a612-c2a108dabd36', 1000, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now(), 5.5, 0.1),
       (1001, 'b6b7764c-ed62-47a4-a68d-3cad4da1e187', 1002, 'c048ef0e-fe46-4c65-9c01-d88af74ba0ab', now(), 100, 0.1),
       -- To be deleted
       (1002, '10aaa42d-ddce-4d62-a612-c2a108dabd36', 1000, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now(), 4, 0.1);
