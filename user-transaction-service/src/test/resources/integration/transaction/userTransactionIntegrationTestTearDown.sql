-- Created during integration test
DELETE
FROM user_transaction_service."order"
WHERE position_id = 999 AND amount = 6;

DELETE
FROM user_transaction_service.user_transaction
WHERE user_id = 'b273ba0f-3b83-4cd4-a8bc-d44e5067ce6d';

-- Created during userTransactionIntegrationTestData.sql
DELETE
FROM user_transaction_service."order"
WHERE id IN (999, 1000);

DELETE
FROM user_transaction_service.user_transaction
WHERE id IN (999, 1000);
