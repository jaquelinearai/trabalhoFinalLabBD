CREATE OR REPLACE PACKAGE db_Utilities_pkg AS

--Text for the operation
sql_text VARCHAR2(800);

PROCEDURE select_DDL_Tables(m_return OUT VARCHAR2, tableName VARCHAR2);
PROCEDURE select_DDL_FKs(m_return OUT VARCHAR2, tableName VARCHAR2);
PROCEDURE getImage(image OUT BLOB, gameName VARCHAR2);
PROCEDURE insertImage(gameName VARCHAR2, image BLOB);
PROCEDURE getMetaData(m_return OUT VARCHAR2, tableName VARCHAR2);
PROCEDURE getTableName(c_return OUT SYS_REFCURSOR);

END db_Utilities_pkg;

CREATE OR REPLACE PACKAGE BODY db_Utilities_pkg AS

PROCEDURE getTableName(c_return OUT SYS_REFCURSOR) AS
tableName VARCHAR2(50);
BEGIN
    sql_text := 'SELECT table_name FROM user_tables';
    OPEN c_return FOR sql_text;
END getTableName;

--Gets metadata from table
PROCEDURE getMetaData(m_return OUT VARCHAR2, tableName VARCHAR2) AS

columnName VARCHAR2(50);
dataType VARCHAR2(50);
dataLength VARCHAR2(50);
numNulls VARCHAR2(50);
numDistinct VARCHAR2(50);
dataDefault VARCHAR2(50);
ID VARCHAR2(50);
nullable_ VARCHAR2(50);

c_return SYS_REFCURSOR;
BEGIN
    sql_text := 'SELECT COLUMN_NAME, DATA_TYPE, DATA_LENGTH, NUM_NULLS, NUM_DISTINCT, DATA_DEFAULT, COLUMN_ID, NULLABLE from USER_TAB_COLUMNS where table_name = ''' || tableName ||'''';
    OPEN c_return FOR sql_text;
    LOOP 
        FETCH c_return INTO columnName, dataType, dataLength, numNulls, numDistinct, dataDefault, ID, nullable_;
        EXIT WHEN c_return%NOTFOUND;
        m_return := m_return || 'ID =' ||ID|| ' |NAME =' || columnName || ' |TYPE =' || dataType || ' |LENGTH=' || dataLength|| ' |NULL? ='|| nullable_ ||
        ' |DEFAULT =' || dataDefault || ' |#NULLS =' || numNulls || ' |#DISTINCT =' || numDistinct;
    END LOOP;
    CLOSE c_return;
END getMetaData;

--Gets DDL info from tables
PROCEDURE select_DDL_Tables(m_return OUT VARCHAR2, tableName VARCHAR2) AS
ddl_text VARCHAR2(300);
c_return SYS_REFCURSOR;
BEGIN
	sql_text := 'select DBMS_METADATA.GET_DDL(object_type,object_name) from user_objects where object_type = ''TABLE'' AND object_name = ''' ||tableName||'''';
	OPEN c_return FOR sql_text;
        LOOP 
            FETCH c_return INTO ddl_text;
            EXIT WHEN c_return%NOTFOUND;
            m_return := m_return || ddl_text;
        END LOOP;
        CLOSE c_return;
END select_DDL_Tables;

--Gets DDL info from FKs
PROCEDURE select_DDL_FKs(m_return OUT VARCHAR2, tableName VARCHAR2) AS
ddl_text VARCHAR2(300);
c_return SYS_REFCURSOR;
BEGIN
    sql_text := 'select dbms_metadata.get_ddl(''REF_CONSTRAINT'', c.constraint_name) from user_constraints c where c.constraint_type = ''R'' AND c.table_name = ''' ||tableName||'''';
    OPEN c_return FOR sql_text;
        LOOP 
            FETCH c_return INTO ddl_text;
            EXIT WHEN c_return%NOTFOUND;
            m_return := m_return || ddl_text;
        END LOOP;
        CLOSE c_return;
END select_DDL_FKS;
--Gets an image from the image table
PROCEDURE getImage(image OUT BLOB, gameName VARCHAR2) AS

i NUMBER;
j NUMBER;
imgId NUMBER;
CURSOR c_imgId IS SELECT imagemId FROM IMAGEM WHERE nomeJogo = gameName;
CURSOR c_countImg IS SELECT count(*) FROM IMAGEM WHERE nomeJogo = gameName;

BEGIN
	
	OPEN c_countImg;
		FETCH c_countImg INTO i;
	CLOSE c_countImg;
	
	j := trunc(dbms_random.value(1,i+1));		
	i := 0;
	
	OPEN c_imgId;
		LOOP 
			FETCH c_imgId INTO imgId;
			i:= i + 1;
			EXIT WHEN c_imgId%NOTFOUND OR j = i;
			
		END LOOP;
	CLOSE c_imgId;
	
	SELECT imagem INTO image FROM IMAGEM WHERE imagemId = imgId;
END getImage;

--Inserts an image into the db
PROCEDURE insertImage(gameName VARCHAR2, image BLOB) AS
BEGIN
	INSERT INTO IMAGEM VALUES (SEQIMAGEMID.NEXTVAL, image, gameName);
END insertImage;

END db_Utilities_pkg;