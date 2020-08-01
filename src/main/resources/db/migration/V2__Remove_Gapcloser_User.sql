SET @gapcloserUserID := (SELECT u.id
                         FROM user u
                         WHERE u.email = "gapcloser@elite12.de");

UPDATE song s
SET s.user_author = NULL
WHERE s.user_author = @gapcloserUserID;

DELETE
FROM token
WHERE owner_id = @gapcloserUserID;
DELETE
FROM user
WHERE id = @gapcloserUserID;