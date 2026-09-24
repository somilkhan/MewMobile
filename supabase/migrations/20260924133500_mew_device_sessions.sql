create table if not exists public.user_session_devices (
 session_id uuid not null, user_id uuid not null references auth.users(id) on delete cascade,
 client_name text not null check(char_length(client_name)<=80), client_version text check(char_length(client_version)<=40),
 platform text not null check(char_length(platform)<=80), device_name text check(char_length(device_name)<=160),
 created_at timestamptz not null default now(), last_seen_at timestamptz not null default now(),
 installation_id text not null check(char_length(installation_id) between 16 and 96 and installation_id ~ '^[A-Za-z0-9_-]+$'),
 primary key(user_id,installation_id), unique(session_id)
);
alter table public.user_session_devices enable row level security;
drop policy if exists user_session_devices_owner on public.user_session_devices;
create policy user_session_devices_owner on public.user_session_devices for select to authenticated using(user_id=(select auth.uid()));
create or replace function public.register_current_device(p_installation_id text,p_client_name text,p_client_version text default null,p_platform text default null,p_device_name text default null) returns boolean language plpgsql security definer set search_path='' as $$
declare v_user_id uuid:=auth.uid(); v_session_id uuid:=nullif(auth.jwt()->>'session_id','')::uuid;
begin if v_user_id is null or v_session_id is null then raise exception using errcode='42501',message='Authentication required'; end if;
if p_installation_id is null or char_length(btrim(p_installation_id)) not between 16 and 96 or btrim(p_installation_id) !~ '^[A-Za-z0-9_-]+$' then raise exception using errcode='22023',message='Invalid installation identifier'; end if;
if p_client_name not in ('Mew Web','Mew Mobile','Mew TV','Mew Desktop','Nuvio Web','Nuvio Mobile','Nuvio TV','Nuvio Desktop') then raise exception using errcode='22023',message='Unsupported client'; end if;
if not exists(select 1 from auth.sessions where id=v_session_id and user_id=v_user_id) then raise exception using errcode='42501',message='Session is no longer active'; end if;
delete from public.user_session_devices where session_id=v_session_id;
insert into public.user_session_devices(session_id,user_id,installation_id,client_name,client_version,platform,device_name,last_seen_at)
values(v_session_id,v_user_id,btrim(p_installation_id),p_client_name,left(nullif(btrim(p_client_version),''),40),left(coalesce(nullif(btrim(p_platform),''),'Unknown'),80),left(nullif(btrim(p_device_name),''),160),now())
on conflict(user_id,installation_id) do update set session_id=excluded.session_id,client_name=excluded.client_name,client_version=excluded.client_version,platform=excluded.platform,device_name=excluded.device_name,last_seen_at=excluded.last_seen_at;
return true; end $$;
revoke all on function public.register_current_device(text,text,text,text,text) from public,anon;
grant execute on function public.register_current_device(text,text,text,text,text) to authenticated;