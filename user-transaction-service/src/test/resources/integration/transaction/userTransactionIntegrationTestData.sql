INSERT INTO user_transaction_service.user_transaction(id, user_id, created)
VALUES (999, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now()),
       (1000, '722cd920-e127-4cc2-93b9-e9b4a8f18873', now());

INSERT INTO user_transaction_service."order"(id, amount, position_id, user_transaction_id)
VALUES (999, 10, 999, 999),
       (1000, 10, 1000, 1000);
