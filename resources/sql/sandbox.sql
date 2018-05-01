begin;

insert into user_groups(user_id, group_id) values(1, 'members');

select * from user_groups where user_id = 1;
select unnest(:groups) as group_id;

select unnest(:groups) as group_id
except
select group_id from user_groups where user_id = :user_id;

select group_id
from user_groups
where user_id = :user_id
      and group_id <> all(:groups);

select group_id from user_groups where user_id = :user_id
except
select unnest(:groups) as group_id;

rollback;