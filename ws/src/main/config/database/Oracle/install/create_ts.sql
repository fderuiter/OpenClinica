set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
DEFINE ts_name = 'clinica'
Rem
prompt Please make sure there is enough space available on the disk for 512M
DEFINE ts_file = '/path/to/clinica.dbf'
Rem
create tablespace &ts_name
datafile '&ts_file' size 512M autoextend on;