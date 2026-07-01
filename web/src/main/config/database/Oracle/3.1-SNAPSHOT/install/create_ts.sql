set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
Rem
DEFINE ts_name = 'clinica'
Rem
prompt If the tablespace is new, please specify the following
prompt Tablespace Size ( (S)mall - 32MB, (M)edium - 256MB, (L)arge -512MB
prompt
DEFINE ts_type = 'L'
Rem
prompt Please make sure there is enough space available on the disk.
DEFINE ts_file = '/path/to/clinica.dbf'
Rem
Rem
column ts_size new_value ts_size;
set heading off
set termout off
select
  case
    when upper('&ts_type') = 'S' then '32 M'
    when upper('&ts_type') = 'M' then '256 M'
    when upper('&ts_type') = 'L' then '512 M'
    else '32 M'
  end ts_size
  from dual;
set heading on
set termout on
Rem
Rem
create tablespace &ts_name
datafile '&ts_file' size &&ts_size;
Rem
Rem
