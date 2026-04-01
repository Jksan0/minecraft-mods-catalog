-- PostgreSQL: assign the minimal free positive id on INSERT.
-- Apply once to the target database (minecraft_mods).

create or replace function assign_min_free_id()
returns trigger
language plpgsql
as $$
declare
    min_free_id bigint;
begin
    -- Prevent race conditions for concurrent inserts into the same table.
    perform pg_advisory_xact_lock(hashtext(TG_TABLE_SCHEMA || '.' || TG_TABLE_NAME));

    execute format(
        $f$
        with ordered as (
            select id, lead(id) over (order by id) as next_id
            from %I.%I
        )
        select coalesce(
            (select 1 where not exists (select 1 from %I.%I where id = 1)),
            (select id + 1
             from ordered
             where next_id is distinct from id + 1
             order by id
             limit 1),
            1
        )
        $f$,
        TG_TABLE_SCHEMA, TG_TABLE_NAME,
        TG_TABLE_SCHEMA, TG_TABLE_NAME
    )
    into min_free_id;

    NEW.id := min_free_id;
    return NEW;
end;
$$;

drop trigger if exists trg_mods_min_free_id on mods;
create trigger trg_mods_min_free_id
before insert on mods
for each row
execute function assign_min_free_id();

drop trigger if exists trg_authors_min_free_id on authors;
create trigger trg_authors_min_free_id
before insert on authors
for each row
execute function assign_min_free_id();

drop trigger if exists trg_categories_min_free_id on categories;
create trigger trg_categories_min_free_id
before insert on categories
for each row
execute function assign_min_free_id();

drop trigger if exists trg_tags_min_free_id on tags;
create trigger trg_tags_min_free_id
before insert on tags
for each row
execute function assign_min_free_id();

drop trigger if exists trg_mod_versions_min_free_id on mod_versions;
create trigger trg_mod_versions_min_free_id
before insert on mod_versions
for each row
execute function assign_min_free_id();
