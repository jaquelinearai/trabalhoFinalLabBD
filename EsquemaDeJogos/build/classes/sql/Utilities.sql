CREATE OR REPLACE PACKAGE db_Utilities_pkg AS

--Text for the operation
sql_text VARCHAR2(500);

PROCEDURE select_DDL(c_return OUT SYS_REFCURSOR, tablename VARCHAR2);
PROCEDURE getImage(image OUT BLOB, gameName VARCHAR2);
PROCEDURE insertImage(gameName VARCHAR2, image BLOB);

END db_Utilities_pkg;

CREATE OR REPLACE PACKAGE BODY db_Utilities_pkg AS
PROCEDURE select_DDL(c_return OUT SYS_REFCURSOR, tablename VARCHAR2) AS
BEGIN
	sql_text := 'select DBMS_METADATA.GET_DDL(object_type,object_name) from user_objects where object_type = ''TABLE'' AND object_name = ''' ||tablename||'''';
	OPEN c_return FOR sql_text;
END select_DDL;

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
	
	j := dbms_random.value(1,i+1);		
	i := 0;
	
	OPEN c_imgId;
		LOOP 
			FETCH c_imgId INTO imgId;
			EXIT WHEN c_imgId%NOTFOUND OR j = i;
			i:= i + 1;
		END LOOP;
	CLOSE c_imgId;
	
	SELECT imagem INTO image FROM IMAGEM WHERE imagemId = imgId;
END getImage;

PROCEDURE insertImage(gameName VARCHAR2, image BLOB) AS
BEGIN
	INSERT INTO IMAGEM VALUES (SEQIMAGEMID.NEXTVAL, image, gameName);
END insertImage;

END db_Utilities_pkg;