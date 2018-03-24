create table users(
  user_id serial primary key,
  -- https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address
  -- user_id is email.
  user_email varchar(254) unique not null,

  -- https://www.mscharhag.com/software-development/bcrypt-maximum-password-length
  -- https://security.stackexchange.com/questions/39849/does-bcrypt-have-a-maximum-password-length
  -- https://stackoverflow.com/questions/5881169/what-column-type-length-should-i-use-for-storing-a-bcrypt-hashed-password-in-a-d
  -- restrict unencrypted password length to 50.
  -- buddy needs 98 for encrypting passwords.
   encrypted_password varchar(98) not null,
   created_at timestamptz not null default current_timestamp
 );

-- Multiple statements in migratus: https://github.com/yogthos/migratus
-- Not sure if this is really needed for postgres.
--;;

create table groups(
  group_id varchar(50) primary key
);

insert into groups(group_id)
values('members'), ('customers'), ('biz'), ('support'), ('devops'), ('admins');

create table user_groups(
  user_id integer references users(user_id),
  group_id varchar(50) references groups(group_id),
  primary key(user_id, group_id)
);

/*
CREATE TABLE users
(id VARCHAR(20) PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIMESTAMP,
 is_active BOOLEAN,
 pass VARCHAR(300));
 */