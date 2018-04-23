-- :name create-user! :? :1
-- :doc Insert a new user record and add to users group.
with new_user as(
  insert into users(user_email, encrypted_password)
  values(:user_email, :encrypted_password)
  returning *
), group_assignment as(
  insert into user_groups(user_id, group_id)
    select user_id, 'users' from new_user
  returning *
)
select new_user.*, Array[group_assignment.group_id] as groups
from new_user
  join group_assignment using(user_id);

-- :name insert-user! :<!
-- :doc Insert a new user record.
insert into users(user_email, encrypted_password)
values(:user_email, :encrypted_password)
returning *;

-- :name delete-user! :! :n
-- :doc Delete a user by id.
delete from users
where user_id = :user_id;

-- :name update-user! :! :n
-- :doc Updates an existing user record.
update users
set user_email = :user_email,
    encrypted_password = :encrypted_password
where user_id = :user_id;

-- :name add-user-to-group! :! :n
-- :doc Add user to group
insert into user_groups(user_id, group_id)
values(:user_id, :group_id);

-- :name remove-user-from-group! :! :n
-- :doc Remove user from group
delete from user_groups
where user_id = :user_id and group_id = :group_id;

-- :name users :? :*
-- :doc Returns all users ordered by email.
select *
from users
order by user_email;

-- :name user-by-email :? :1
-- :doc Retrieves user by email.
select *
from users
where user_email = :user_email;

/*
-- :name create-user! :! :n
-- :doc Creates a new user record.
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id
*/
