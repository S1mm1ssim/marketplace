INSERT INTO "user"(id, username, email, name, created, updated, company_id)
VALUES ('b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d', 'customer1', 'sqlcustomer1@user.com', 'full name', now(), now(), 999),
       ('722cd920-e127-4cc2-93b9-e9b4a8f18873', 'customer2', 'sqlcustomer2@email.com', 'full name', now(), now(), 999);

INSERT INTO category(id, name, parent_category, description)
VALUES (999, 'root', null, 'description');

INSERT INTO item(id, name, description, created, category_id)
VALUES ('b338b0c2-ae82-437f-a55c-8017c12895b4', 'item1', 'description', now(), 999),
       ('10aaa42d-ddce-4d62-a612-c2a108dabd36', 'item2', 'description', now(), 999);

INSERT INTO position(id, item_id, company_id, created_by, created, amount, min_amount)
VALUES (999, 'b338b0c2-ae82-437f-a55c-8017c12895b4', 999, 'b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d', now(), 150, 5),
       (1000, '10aaa42d-ddce-4d62-a612-c2a108dabd36', 999, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now(), 55, 1);

INSERT INTO user_transaction(id, user_id, created)
VALUES (999, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now()),
       (1000, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now());

INSERT INTO "order"(id, amount, position_id, user_transaction_id)
VALUES (999, 10, 999, 999),
       (1000, 10, 1000, 1000);
