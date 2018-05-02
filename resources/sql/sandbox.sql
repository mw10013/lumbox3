begin;

insert into user_groups(user_id, group_id)
values(1, 'members'), (1, 'devops');

-- select * from user_groups where user_id = 1;
-- select unnest(:groups) as group_id;

-- select unnest(:groups) as group_id
-- except
-- select group_id from user_groups where user_id = :user_id;

-- select group_id from user_groups where user_id = :user_id
-- except
-- select unnest(:groups) as group_id;

with old_groups as(
  select group_id from user_groups where user_id = :user_id
  except
  select unnest(cast(:groups as varchar[])) as group_id
), removed as(
  delete from user_groups
  where user_id = :user_id
    and group_id in (select group_id from old_groups)
  returning *
), new_groups as(
  select unnest(:groups) as group_id
  except
  select group_id from user_groups where user_id = :user_id
), added as(
  insert into user_groups(user_id, group_id)
    select :user_id as user_id, group_id
    from new_groups
  returning *
)
select 'removed' as op, group_id from removed
union
select 'added' as op, group_id from added;

select * from user_groups where user_id = :user_id;

rollback;


-- select group_id
-- from user_groups
-- where user_id = :user_id
--       and group_id <> all(:groups);

