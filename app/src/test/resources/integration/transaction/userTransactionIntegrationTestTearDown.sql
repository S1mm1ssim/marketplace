-- Created during integration test
DELETE
FROM "order"
WHERE position_id = 999 AND amount = 6;

DELETE
FROM user_transaction
WHERE user_id = 'b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d';

-- Created during userTransactionIntegrationTestData.sql
DELETE
FROM "order"
WHERE id IN (999, 1000);

DELETE
FROM user_transaction
WHERE id IN (999, 1000);

DELETE
FROM position
WHERE id IN (999, 1000);

DELETE
FROM item
WHERE id IN ('b338b0c2-ae82-437f-a55c-8017c12895b4', '10aaa42d-ddce-4d62-a612-c2a108dabd36');

DELETE
FROM category
WHERE id = 999;

DELETE
FROM "user"
WHERE id IN ('b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d', '722cd920-e127-4cc2-93b9-e9b4a8f18873');
