-- numcopiasvendidas

CREATE OR REPLACE TRIGGER trigger_numcopiasvendidas
AFTER INSERT OR UPDATE OR DELETE ON ITEM
FOR EACH ROW
DECLARE

oldnumcopiasvendidas NUMBER;

BEGIN


 	IF INSERTING THEN
 		
		UPDATE Jogo SET Jogo.numcopiasvendidas = Jogo.numcopiasvendidas+:new.quantidade WHERE Jogo.nome = :new.nomeproduto;
	
		
	ELSIF UPDATING THEN

		IF (:new.quantidade IS NOT NULL) THEN
			
			UPDATE Jogo SET Jogo.numcopiasvendidas = Jogo.numcopiasvendidas + :new.quantidade - :old.quantidade WHERE Jogo.nome = :old.nomeproduto;
			
		END IF;
		
	ELSIF DELETING THEN  
		
		UPDATE Jogo SET Jogo.numcopiasvendidas = Jogo.numcopiasvendidas-:old.quantidade WHERE Jogo.nome = :old.nomeproduto;
		
	END IF; 
END;


-- valorarrecadadocopiasvendidas

CREATE OR REPLACE TRIGGER trigger_valorcopiasvendidas
AFTER INSERT ON ITEM
FOR EACH ROW
DECLARE

BEGIN

 	IF INSERTING THEN 	
		UPDATE Jogo SET Jogo.valorarrecadadocopiasvendidas = Jogo.valorarrecadadocopiasvendidas+:new.quantidade*:new.preco WHERE Jogo.nome = :new.nomeproduto;
	
	END IF; 
END;


-- valorarrecadadoconteudo

CREATE OR REPLACE TRIGGER trigger_valorconteudo
AFTER INSERT ON ITEM
FOR EACH ROW
DECLARE

BEGIN

 	IF INSERTING THEN

		UPDATE Jogo SET Jogo.valorarrecadadoconteudo = Jogo.valorarrecadadoconteudo + :new.quantidade*:new.preco
			WHERE Jogo.nome = (SELECT C.nomejogo FROM Jogo J, Conteudo C WHERE C.nome = :new.nomeproduto AND J.nome = C.nomejogo);

	END IF; 
END;


-- numusersdist

CREATE OR REPLACE TRIGGER trigger_numusersdist

BEFORE INSERT OR DELETE ON joga 
FOR EACH ROW

DECLARE
   
BEGIN

   	IF INSERTING THEN
   		UPDATE jogo set numusersdist = numusersdist + 1 WHERE nome = :new.instNome;
   	ELSIF DELETING THEN
   		UPDATE jogo set numusersdist = numusersdist - 1 WHERE nome = :old.instNome;
   	END IF;

END trigger_numusersdist;


-- sumtotaltempo

CREATE OR REPLACE TRIGGER trigger_sumtotaltempo

BEFORE INSERT OR UPDATE OR DELETE ON joga 
FOR EACH ROW

DECLARE
   
BEGIN

   	IF INSERTING THEN
   		UPDATE jogo set sumtotaltempo = sumtotaltempo + :new.tempo WHERE nome = :new.instNome;
   	ELSIF UPDATING THEN
   		IF (:new.tempo IS NOT NULL AND :new.instNome IS NULL) THEN
   			UPDATE jogo set sumtotaltempo = sumtotaltempo + (:new.tempo - :old.tempo) WHERE nome = :old.instNome;
   		ELSIF (:new.tempo IS NOT NULL AND :new.instNome IS NOT NULL) THEN
   			UPDATE jogo set sumtotaltempo = sumtotaltempo - :old.tempo WHERE nome = :old.instNome;
   			UPDATE jogo set sumtotaltempo = sumtotaltempo + :new.tempo WHERE nome = :new.instNome;
   		END IF;
   	ELSIF DELETING THEN
   		UPDATE jogo set sumtotaltempo = sumtotaltempo - :old.tempo WHERE nome = :old.instNome;
   	END IF;

END trigger_sumtotaltempo;


-- numconteudosdist

CREATE OR REPLACE TRIGGER trigger_numconteudosdist

BEFORE INSERT OR DELETE ON conteudo 
FOR EACH ROW

DECLARE
   
BEGIN

   	IF INSERTING THEN
   		UPDATE jogo set jogo.numconteudosdist = jogo.numconteudosdist + 1
   			WHERE Jogo.nome = :new.nomejogo;

   	ELSIF DELETING THEN
   		UPDATE jogo set jogo.numconteudosdist = jogo.numconteudosdist - 1
   			WHERE Jogo.nome = :old.nomejogo;
   	END IF;

END trigger_sumtotaltempo;


-- mediaitenscomprados

-- varianciaitenscomprados

-- mediatotalcompra

-- varianciatotalcompra

-- pontuacao

