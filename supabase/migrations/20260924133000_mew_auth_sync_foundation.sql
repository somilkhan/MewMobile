create table if not exists public.profiles (
  id uuid primary key default gen_random_uuid(), user_id uuid not null references auth.users(id) on delete cascade,
  profile_index integer not null check (profile_index between 1 and 6), name text not null default '',
  avatar_color_hex text not null default '#1E88E5', avatar_id text, avatar_url text, background_url text,
  uses_primary_addons boolean not null default false, uses_primary_plugins boolean not null default false,
  pin_enabled boolean not null default false, pin_locked_until timestamptz, created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(), unique(user_id, profile_index)
);
create table if not exists public.profile_settings_blobs (
 user_id uuid not null references auth.users(id) on delete cascade, profile_id integer not null check (profile_id between 1 and 6),
 platform text not null default 'mobile', settings_json jsonb not null default '{}'::jsonb, updated_at timestamptz not null default now(),
 primary key(user_id, profile_id, platform)
);
create table if not exists public.provider_credentials (
 user_id uuid not null references auth.users(id) on delete cascade, profile_id integer not null check (profile_id between 1 and 6),
 provider text not null, credential_json jsonb not null default '{}'::jsonb, updated_at timestamptz not null default now(),
 primary key(user_id, profile_id, provider)
);
create or replace function public.ensure_default_profile() returns trigger language plpgsql security definer set search_path=public as $$
begin insert into public.profiles(user_id,profile_index,name) values(new.id,1,'Mew') on conflict(user_id,profile_index) do nothing; return new; end $$;
drop trigger if exists on_auth_user_created_mew on auth.users;
create trigger on_auth_user_created_mew after insert on auth.users for each row execute function public.ensure_default_profile();
create or replace function public.sync_pull_profiles() returns setof public.profiles language sql stable security invoker set search_path='' as $$ select * from public.profiles where user_id=auth.uid() order by profile_index $$;
create or replace function public.sync_push_profiles(p_profiles jsonb,p_client_max_profiles integer default 6,p_origin_client_id text default null) returns void language plpgsql security invoker set search_path='' as $$
declare v_uid uuid:=auth.uid(); item jsonb;
begin
if v_uid is null then raise exception 'Not authenticated'; end if;
if jsonb_typeof(p_profiles)<>'array' then raise exception 'Profiles payload must be an array'; end if;
for item in select value from jsonb_array_elements(p_profiles) loop
insert into public.profiles(user_id,profile_index,name,avatar_color_hex,avatar_id,avatar_url,uses_primary_addons,uses_primary_plugins,updated_at)
values(v_uid,greatest(1,least(coalesce((item->>'profile_index')::int,1),least(coalesce(p_client_max_profiles,6),6))),coalesce(item->>'name',''),coalesce(item->>'avatar_color_hex','#1E88E5'),nullif(item->>'avatar_id',''),nullif(item->>'avatar_url',''),coalesce((item->>'uses_primary_addons')::boolean,false),coalesce((item->>'uses_primary_plugins')::boolean,false),now())
on conflict(user_id,profile_index) do update set name=excluded.name,avatar_color_hex=excluded.avatar_color_hex,avatar_id=excluded.avatar_id,avatar_url=excluded.avatar_url,uses_primary_addons=excluded.uses_primary_addons,uses_primary_plugins=excluded.uses_primary_plugins,updated_at=now();
end loop; end $$;
create or replace function public.sync_pull_profile_settings_blob(p_profile_id integer,p_platform text default 'mobile') returns table(profile_id integer,settings_json jsonb,updated_at timestamptz) language sql stable security invoker set search_path='' as $$ select s.profile_id,s.settings_json,s.updated_at from public.profile_settings_blobs s where s.user_id=auth.uid() and s.profile_id=p_profile_id and s.platform=coalesce(p_platform,'mobile') $$;
create or replace function public.sync_push_profile_settings_blob(p_profile_id integer,p_settings_json jsonb,p_platform text default 'mobile',p_origin_client_id text default null) returns void language plpgsql security invoker set search_path='' as $$
begin if auth.uid() is null then raise exception 'Not authenticated'; end if; if not exists(select 1 from public.profiles where user_id=auth.uid() and profile_index=p_profile_id) then raise exception 'Profile not found'; end if;
insert into public.profile_settings_blobs(user_id,profile_id,platform,settings_json,updated_at) values(auth.uid(),p_profile_id,coalesce(p_platform,'mobile'),coalesce(p_settings_json,'{}'::jsonb),now())
on conflict(user_id,profile_id,platform) do update set settings_json=excluded.settings_json,updated_at=now(); end $$;
create or replace function public.sync_pull_provider_credentials(p_profile_id integer) returns setof public.provider_credentials language sql stable security invoker set search_path='' as $$ select * from public.provider_credentials where user_id=auth.uid() and profile_id=p_profile_id order by provider $$;
create or replace function public.sync_seed_provider_credentials(p_profile_id integer,p_credentials jsonb,p_origin_client_id text default null) returns void language plpgsql security invoker set search_path='' as $$
declare item jsonb; begin if auth.uid() is null then raise exception 'Not authenticated'; end if; if not exists(select 1 from public.profiles where user_id=auth.uid() and profile_index=p_profile_id) then raise exception 'Profile not found'; end if;
for item in select value from jsonb_array_elements(coalesce(p_credentials,'[]'::jsonb)) loop insert into public.provider_credentials(user_id,profile_id,provider,credential_json,updated_at) values(auth.uid(),p_profile_id,item->>'provider',coalesce(item->'credential_json','{}'::jsonb),now()) on conflict(user_id,profile_id,provider) do nothing; end loop; end $$;
create or replace function public.sync_push_provider_credentials(p_profile_id integer,p_credentials jsonb,p_origin_client_id text default null) returns void language plpgsql security invoker set search_path='' as $$
declare item jsonb; begin if auth.uid() is null then raise exception 'Not authenticated'; end if;
for item in select value from jsonb_array_elements(coalesce(p_credentials,'[]'::jsonb)) loop insert into public.provider_credentials(user_id,profile_id,provider,credential_json,updated_at) values(auth.uid(),p_profile_id,item->>'provider',coalesce(item->'credential_json','{}'::jsonb),now()) on conflict(user_id,profile_id,provider) do update set credential_json=excluded.credential_json,updated_at=now(); end loop; end $$;
alter table public.profiles enable row level security;
alter table public.profile_settings_blobs enable row level security;
alter table public.provider_credentials enable row level security;
drop policy if exists profiles_owner on public.profiles; create policy profiles_owner on public.profiles for all to authenticated using(user_id=(select auth.uid())) with check(user_id=(select auth.uid()));
drop policy if exists profile_settings_owner on public.profile_settings_blobs; create policy profile_settings_owner on public.profile_settings_blobs for all to authenticated using(user_id=(select auth.uid())) with check(user_id=(select auth.uid()));
drop policy if exists provider_credentials_owner on public.provider_credentials; create policy provider_credentials_owner on public.provider_credentials for all to authenticated using(user_id=(select auth.uid())) with check(user_id=(select auth.uid()));
revoke all on function public.ensure_default_profile() from public,anon,authenticated;
revoke all on function public.sync_pull_profiles() from public,anon; grant execute on function public.sync_pull_profiles() to authenticated;
revoke all on function public.sync_push_profiles(jsonb,integer,text) from public,anon; grant execute on function public.sync_push_profiles(jsonb,integer,text) to authenticated;
revoke all on function public.sync_pull_profile_settings_blob(integer,text) from public,anon; grant execute on function public.sync_pull_profile_settings_blob(integer,text) to authenticated;
revoke all on function public.sync_push_profile_settings_blob(integer,jsonb,text,text) from public,anon; grant execute on function public.sync_push_profile_settings_blob(integer,jsonb,text,text) to authenticated;
revoke all on function public.sync_pull_provider_credentials(integer) from public,anon; grant execute on function public.sync_pull_provider_credentials(integer) to authenticated;
revoke all on function public.sync_seed_provider_credentials(integer,jsonb,text) from public,anon; grant execute on function public.sync_seed_provider_credentials(integer,jsonb,text) to authenticated;
revoke all on function public.sync_push_provider_credentials(integer,jsonb,text) from public,anon; grant execute on function public.sync_push_provider_credentials(integer,jsonb,text) to authenticated;