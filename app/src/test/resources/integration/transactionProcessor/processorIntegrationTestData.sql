INSERT INTO "user"(id, username, email, name, created, updated, company_id)
VALUES ('722cd920-e127-4cc2-93b9-e9b4a8f18873', 'user2', 'sqluser2@mail.com', 'full name', now(), now(), 1000);

INSERT INTO category(id, name, parent_category, description)
VALUES (999, 'root', null, 'description'),
       (1000, 'electronics', 999, 'electronics');

INSERT INTO item(id, name, description, created, category_id)
VALUES ('b338b0c2-ae82-437f-a55c-8017c12895b4', 'item1', 'description', now(), 1000);

INSERT INTO position(id, item_id, company_id, created_by, created, amount, min_amount)
VALUES (999, 'b338b0c2-ae82-437f-a55c-8017c12895b4', 1000, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now(), 150, 0.1);
