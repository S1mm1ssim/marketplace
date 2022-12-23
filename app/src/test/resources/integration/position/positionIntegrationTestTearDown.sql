-- Created during integration test
DELETE
FROM position
WHERE item_id = 'b6b7764c-ed62-47a4-a68d-3cad4da1e187'
  AND company_id = 999
  AND created_by = 'b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d'
  AND amount = 2;

-- Created during positionIntegrationTestData.sql
DELETE
FROM position
WHERE id = 999;
DELETE
FROM position
WHERE id = 1000;
DELETE
FROM position
WHERE id = 1001;

DELETE
FROM item
WHERE id = 'b338b0c2-ae82-437f-a55c-8017c12895b4';
DELETE
FROM item
WHERE id = '10aaa42d-ddce-4d62-a612-c2a108dabd36';
DELETE
FROM item
WHERE id = 'b6b7764c-ed62-47a4-a68d-3cad4da1e187';

DELETE
FROM category
WHERE id = 1000;
DELETE
FROM category
WHERE id = 999;

DELETE
FROM "user"
WHERE id = 'b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d';
DELETE
FROM "user"
WHERE id = '722cd920-e127-4cc2-93b9-e9b4a8f18873';
DELETE
FROM "user"
WHERE id = 'c048ef0e-fe46-4c65-9c01-d88af74ba0ab';