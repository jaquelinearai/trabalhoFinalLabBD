--Package declaration
CREATE OR REPLACE PACKAGE db_Operations_pkg AS

--Array that will hold the name from the attributes 
TYPE t_attNameArray IS VARRAY(15) OF varchar2(40);
--Array that will hold the values from the attributes
TYPE t_attValueArray IS VARRAY(15) OF varchar2(40);
--Text for the operation
sql_text VARCHAR2(500);
--Counter variable
i NUMBER;
--Names from the keys from the table
TYPE t_keysNamesArray IS VARRAY(15) OF ALL_CONS_COLUMNS.column_name%TYPE;
--Names from all columns
TYPE t_colNamesArray IS VARRAY(20) OF ALL_TAB_COLUMNS.column_name%TYPE;
--Cursor to select the column types from the table
c_pColTypes SYS_REFCURSOR; 

--Procedure declaration
PROCEDURE select_procedure(c_return OUT SYS_REFCURSOR, p_table VARCHAR2, p_keyAttNames t_attNameArray, p_keyValue t_attValueArray, p_name t_attNameArray, p_newValue t_attValueArray);
PROCEDURE insert_procedure(m_return OUT VARCHAR2, p_table VARCHAR2, p_name t_attNameArray, p_newValue t_attValueArray);
PROCEDURE update_procedure(m_return OUT VARCHAR2, p_table VARCHAR2, p_keyAttNames t_attNameArray, p_keyValue t_attValueArray, p_name t_attNameArray, p_newValue t_attValueArray);
PROCEDURE delete_procedure(m_return OUT VARCHAR2, p_table VARCHAR2, p_keyAttNames t_attNameArray, p_keyValue t_attValueArray, p_name t_attNameArray);

END db_Operations_pkg;

--Package Body
CREATE OR REPLACE PACKAGE BODY db_Operations_pkg AS

--Initializing internal varrays
keyTypes t_colNamesArray := t_colNamesArray();
p_types t_colNamesArray := t_colNamesArray();

--Procedure Body
PROCEDURE select_procedure(c_return OUT SYS_REFCURSOR, p_table VARCHAR2, p_keyAttNames t_attNameArray, p_keyValue t_attValueArray, p_name t_attNameArray, p_newValue t_attValueArray);
BEGIN 
	--Get the types from the columns that will be used as search parameters for the rows in the operations
	IF p_keyAttNames.COUNT != 0 THEN
		i := 1;
		LOOP
			sql_text := 'SELECT cols.DATA_TYPE FROM all_tab_columns cols WHERE cols.table_name = ''' || p_table || ''' AND cols.column_name = '''|| p_keyAttNames(i) ||'''';
			OPEN c_pColTypes FOR sql_text;
		keyTypes.extend;
		FETCH c_pColTypes INTO keyTypes(i);
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		END LOOP;
		CLOSE c_pColTypes;
	END IF;
	--Create the select text
	i := 1;
	sql_text := 'SELECT '; 
	--Columns that will be returned
	LOOP
		sql_text := sql_text || p_name(i);
		EXIT WHEN i = p_name.COUNT;
		i := i + 1;
		sql_text := sql_text || ' , ';
	END LOOP;
	
	i := 1;
	sql_text := sql_text || ' FROM ' || p_table || ' WHERE ';
	LOOP
		CASE
			--Add the ' symbol if variable is a varchar(2) or char
			WHEN keyTypes(i) = 'VARCHAR2' OR keyTypes(i) = 'CHAR' OR keyTypes(i) = 'VARCHAR' THEN
				sql_text := sql_text || p_keyAttNames(i) || ' = '''|| p_keyValue(i) || '''';
				
			--If it is DATE, need to adjust the date to the used type in the table, else, the default date conversion will not work
			WHEN keyTypes(i) = 'DATE' THEN
				CASE
					WHEN p_table = 'JOGA' THEN
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''DD/MM/YYYY HH24:MI:SS'')';
			
					WHEN p_table = 'CARTAODECREDITO' THEN
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''MM/YYYY'')';
					
					ELSE
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''DD/MM/YYYY'')';
				END CASE;
			
			--Else, just copy the value
			ELSE
				sql_text := sql_text || p_keyAttNames(i) || ' = '|| p_keyValue(i);
		END CASE;
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		sql_text := sql_text ||' AND ';	
	END LOOP;
	
	--Open the cursor to be returned
	OPEN c_return FOR sql_text;
END select_procedure;

PROCEDURE insert_procedure(m_return OUT VARCHAR2, p_table VARCHAR2, p_name t_attNameArray, p_newValue t_attValueArray);
BEGIN 
	--Get the types from the columns that will be used as search parameters for the rows in the operations
	IF p_keyAttNames.COUNT != 0 THEN
		i := 1;
		LOOP
			sql_text := 'SELECT cols.DATA_TYPE FROM all_tab_columns cols WHERE cols.table_name = ''' || p_table || ''' AND cols.column_name = '''|| p_keyAttNames(i) ||'''';
			OPEN c_pColTypes FOR sql_text;
		keyTypes.extend;
		FETCH c_pColTypes INTO keyTypes(i);
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		END LOOP;
		CLOSE c_pColTypes;
	END IF;
	--Find the types from the data that will be inserted
	i := 1;
	LOOP
		sql_text := 'SELECT cols.DATA_TYPE FROM all_tab_columns cols WHERE cols.table_name = ''' || p_table || ''' AND cols.column_name = '''|| p_name(i) ||'''';
		OPEN c_pColTypes FOR sql_text;
		p_types.extend;
		FETCH c_pColTypes INTO p_types(i);
		EXIT WHEN i = p_name.COUNT;
		i := i + 1;
	END LOOP;
	CLOSE c_pColTypes;
	
	sql_text := 'INSERT INTO ' || p_table || '(';
	--Loop to get inserted column names
	i := 1;
	LOOP
		sql_text := sql_text || p_name(i);
		EXIT WHEN i = p_name.COUNT;
		i := i + 1;
		sql_text := sql_text ||' , '; 
	END LOOP;
	sql_text := sql_text ||') VALUES (';
	
	i := 1;
	--Loop to get all the attributes that will be insert
	LOOP
		CASE
			--Add the ' symbol if variable is a varchar(2) or char
			WHEN p_types(i) = 'VARCHAR2' OR p_types(i) = 'CHAR' OR p_types(i) = 'VARCHAR' THEN
					sql_text := sql_text || '''' || p_newValue(i) || '''';
					
			--If it is DATE, need to adjust the date to the used type in the table, else, the default date conversion will not work
			WHEN p_types(i) = 'DATE' THEN
				CASE
					WHEN p_table = 'JOGA' THEN
						sql_text := sql_text || 'TO_DATE(''' || p_newValue(i) || ''', ''DD/MM/YYYY HH24:MI:SS'')';
			
					WHEN p_table = 'CARTAODECREDITO' THEN
						sql_text := sql_text || 'TO_DATE(''' || p_newValue(i) || ''', ''MM/YYYY'')';
					
					ELSE
						sql_text := sql_text || 'TO_DATE(''' || p_newValue(i) || ''', ''DD/MM/YYYY'')';
				END CASE;
		
			ELSE
				sql_text := sql_text || p_newValue(i);
		END CASE;
		
		EXIT WHEN i = p_name.COUNT;
		i := i + 1;
		sql_text:= sql_text || ' , ';
	END LOOP;
	sql_text := sql_text ||')';
	
	EXECUTE IMMEDIATE sql_text;
	m_return := 'Foram inseridos: ' || SQL%ROWCOUNT || ' dados';

END insert_procedure;		

PROCEDURE update_procedure(m_return OUT VARCHAR2, p_table VARCHAR2, p_keyAttNames t_attNameArray, p_keyValue t_attValueArray, p_name t_attNameArray, p_newValue t_attValueArray);
BEGIN 
	--Get the types from the columns that will be used as search parameters for the rows in the operations
	IF p_keyAttNames.COUNT != 0 THEN
		i := 1;
		LOOP
			sql_text := 'SELECT cols.DATA_TYPE FROM all_tab_columns cols WHERE cols.table_name = ''' || p_table || ''' AND cols.column_name = '''|| p_keyAttNames(i) ||'''';
			OPEN c_pColTypes FOR sql_text;
		keyTypes.extend;
		FETCH c_pColTypes INTO keyTypes(i);
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		END LOOP;
		CLOSE c_pColTypes;
	END IF;		
	--Find the types from the data that will be updated
	i := 1;
	LOOP
		sql_text := 'SELECT cols.DATA_TYPE FROM all_tab_columns cols WHERE cols.table_name = ''' || p_table || ''' AND cols.column_name = '''|| p_name(i) ||'''';
		OPEN c_pColTypes FOR sql_text;
		p_types.extend;
		FETCH c_pColTypes INTO p_types(i);
		EXIT WHEN i = p_name.COUNT;
		i := i + 1;
	END LOOP;
	CLOSE c_pColTypes;
	
	sql_text := 'UPDATE ' || p_table || ' SET ';
	i := 1;
	--Loop to get all the attributes that will be updated
	LOOP
		CASE
			--Add the ' symbol if variable is a varchar(2) or char
			WHEN p_types(i) = 'VARCHAR2' OR p_types(i) = 'CHAR' OR p_types(i) = 'VARCHAR' THEN
					sql_text := sql_text || p_name(i) || ' = '''|| p_newValue(i) || '''';
					
			--If it is DATE, need to adjust the date to the used type in the table, else, the default date conversion will not work
			WHEN p_types(i) = 'DATE' THEN
				CASE
					WHEN p_table = 'JOGA' THEN
						sql_text := sql_text || p_name(i) || ' = TO_DATE(''' || p_newValue(i) || ''', ''DD/MM/YYYY HH24:MI:SS'')';
			
					WHEN p_table = 'CARTAODECREDITO' THEN
						sql_text := sql_text || p_name(i) || ' = TO_DATE(''' || p_newValue(i) || ''', ''MM/YYYY'')';
					
					ELSE
						sql_text := sql_text || p_name(i) || ' = TO_DATE(''' || p_newValue(i) || ''', ''DD/MM/YYYY'')';
				END CASE;
			--Else, just copy the value
			ELSE
				sql_text := sql_text || p_name(i) || ' = '|| p_newValue(i);
		END CASE;
		
		EXIT WHEN i = p_name.COUNT;
		i := i + 1;
	END LOOP;
	
	i := 1;
	sql_text := sql_text || ' WHERE ';
	LOOP
		CASE
			--Add the ' symbol if variable is a varchar(2) or char
			WHEN keyTypes(i) = 'VARCHAR2' OR keyTypes(i) = 'CHAR' OR keyTypes(i) = 'VARCHAR' THEN
				sql_text := sql_text || p_keyAttNames(i) || ' = '''|| p_keyValue(i) || '''';
				
			--If it is DATE, need to adjust the date to the used type in the table, else, the default date conversion will not work
			WHEN keyTypes(i) = 'DATE' THEN
				CASE
					WHEN p_table = 'JOGA' THEN
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''DD/MM/YYYY HH24:MI:SS'')';
			
					WHEN p_table = 'CARTAODECREDITO' THEN
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''MM/YYYY'')';
					
					ELSE
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''DD/MM/YYYY'')';
				END CASE;
			
			--Else, just copy the value
			ELSE
				sql_text := sql_text || p_keyAttNames(i) || ' = '|| p_keyValue(i);
		END CASE;
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		sql_text := sql_text ||' AND ';	
	END LOOP;
	
	EXECUTE IMMEDIATE sql_text;
	m_return := 'Foram atualizados: ' || SQL%ROWCOUNT || ' dados';

END update_procedure;

PROCEDURE delete_procedure(c_return OUT SYS_REFCURSOR, p_table VARCHAR2, p_keyAttNames t_attNameArray, p_keyValue t_attValueArray, p_name t_attNameArray, p_newValue t_attValueArray, p_operation VARCHAR2);

BEGIN
 
	--Get the types from the columns that will be used as search parameters for the rows in the operations
	IF p_keyAttNames.COUNT != 0 THEN
		i := 1;
		LOOP
			sql_text := 'SELECT cols.DATA_TYPE FROM all_tab_columns cols WHERE cols.table_name = ''' || p_table || ''' AND cols.column_name = '''|| p_keyAttNames(i) ||'''';
			OPEN c_pColTypes FOR sql_text;
		keyTypes.extend;
		FETCH c_pColTypes INTO keyTypes(i);
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		END LOOP;
		CLOSE c_pColTypes;
	END IF;
	--Create the SQL code for the desired delete
	sql_text := 'DELETE FROM ' || p_table || ' WHERE ';
	i := 1;
	LOOP
		CASE
			--Add the ' symbol if variable is a varchar(2) or char
			WHEN keyTypes(i) = 'VARCHAR2' OR keyTypes(i) = 'CHAR' OR keyTypes(i) = 'VARCHAR' THEN
				sql_text := sql_text || p_keyAttNames(i) || ' = '''|| p_keyValue(i) || '''';
				
			--If it is DATE, need to adjust the date to the used type in the table, else, the default date conversion will not work
			WHEN keyTypes(i) = 'DATE' THEN
				CASE
					WHEN p_table = 'JOGA' THEN
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''DD/MM/YYYY HH24:MI:SS'')';
			
					WHEN p_table = 'CARTAODECREDITO' THEN
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''MM/YYYY'')';
					
					ELSE
						sql_text := sql_text || p_keyAttNames(i) || ' = TO_DATE(''' || p_keyValue(i) || ''', ''DD/MM/YYYY'')';
				END CASE;
			
			--Else, just copy the value
			ELSE
				sql_text := sql_text || p_keyAttNames(i) || ' = '|| p_keyValue(i);
		END CASE;
		EXIT WHEN i = p_keyAttNames.COUNT;
		i := i + 1;
		sql_text := sql_text ||' AND '; 
	END LOOP;	
	--Execute the delete command
	EXECUTE IMMEDIATE sql_text;
	m_return := 'Foram deletados: ' || SQL%ROWCOUNT || ' dados';
END db_Operations_pkg;