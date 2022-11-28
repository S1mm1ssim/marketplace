-- Created during integration test
DELETE
FROM "user"
WHERE email = 'saveTest@test.com';

-- Users created during data script for integration test
DELETE
FROM "user"
WHERE email = 'sqluser@user.com';

DELETE
FROM "user"
WHERE email = 'sqluser2@user.com';

DELETE
FROM "user"
WHERE email = 'softDeleted@user.com';

DELETE
FROM company
WHERE id = 999;

DELETE
FROM company
WHERE id = 1000;

DELETE
FROM company
WHERE id = 1001;