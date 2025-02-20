C:\Users\ashfa>docker exec -it 98 bash
root@98853e447ea1:/# su - postgres

postgres@98853e447ea1:~$ psql -h localhost -p 5432 -U postgres -d mainschema
psql (17.0 (Debian 17.0-1.pgdg120+1))
Type "help" for help.

mainschema=# CREATE ROLE anon;
CREATE ROLE
mainschema=# GRANT USAGE ON SCHEMA public TO anon;
GRANT
mainschema=# GRANT SELECT ON TABLE public.employee TO anon;
GRANT
mainschema=# \dp public.employee
                                   Access privileges
 Schema |   Name   | Type  |     Access privileges      | Column privileges | Policies
--------+----------+-------+----------------------------+-------------------+----------
 public | employee | table | postgres=arwdDxtm/postgres+|                   |
        |          |       | anon=r/postgres            |                   |
(1 row)

mainschema=# \q
postgres@98853e447ea1:~$
