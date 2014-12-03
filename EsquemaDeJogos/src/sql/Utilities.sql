CREATE OR REPLACE PACKAGE db_Utilities_pkg AS

--Text for the operation
sql_text VARCHAR2(500);

PROCEDURE select_DDL(c_return OUT SYS_REFCURSOR, tablename VARCHAR2);

END db_Utilities_pkg;

CREATE OR REPLACE PACKAGE BODY db_Utilities_pkg AS

PROCEDURE select_DDL(c_return OUT SYS_REFCURSOR, tablename VARCHAR2);
BEGIN
	sql_text := 'select DBMS_METADATA.GET_DDL(object_type,object_name) from user_objects where object_type = ''TABLE'' AND object_name = ''' ||tablename||'''';
	OPEN c_return FOR sql_text;

END db_Utilities_pkg;