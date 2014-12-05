ALTER TABLE JOGO ADD (numcopiasvendidas NUMBER);
ALTER TABLE JOGO ADD (valorarrecadadocopiasvendidas NUMBER);
ALTER TABLE JOGO ADD (valorarrecadadoconteudo NUMBER);
ALTER TABLE JOGO ADD (numusersdist NUMBER);
ALTER TABLE JOGO ADD (sumtotaltempo NUMBER);
ALTER TABLE JOGO ADD (numconteudosdist NUMBER);
ALTER TABLE JOGO ADD (mediaitenscomprados NUMBER);

ALTER TABLE JOGO ADD (varianciaitenscomprados NUMBER);
ALTER TABLE JOGO ADD (mediatotalcompra NUMBER);
ALTER TABLE JOGO ADD (varianciatotalcompra NUMBER);

ALTER TABLE JOGO ADD (pontuacao NUMBER);


SET SERVEROUTPUT ON;

DECLARE

  CURSOR c_jogos IS SELECT nome FROM jogo;
  nomeJogo1 jogo.nome%TYPE;
  v_count NUMBER;
  v_aux NUMBER;
  v_pontuacao NUMBER;

  v_ncopiasvendidas NUMBER;
  v_valorarrecadadocopias NUMBER;
  v_sumtotaltempo NUMBER;
  v_valorarrecadadoconteudo NUMBER;

BEGIN

  OPEN c_jogos;

  LOOP 

    FETCH c_jogos INTO nomeJogo1;
    EXIT WHEN c_jogos%NOTFOUND;
    
	-- numcopiasvendidas

    SELECT SUM(I.quantidade) INTO v_count FROM item I, produto P WHERE P.nome = nomeJogo1 AND P.nome = I.nomeProduto;
    
    IF(v_count > 0) THEN
      UPDATE jogo SET numcopiasvendidas = v_count WHERE nome = nomeJogo1;
    ELSE UPDATE jogo SET numcopiasvendidas = 0 WHERE nome = nomeJogo1;
    END IF;
    
    -- valorarrecadadocopiasvendidas

    SELECT numcopiasvendidas INTO v_count FROM jogo WHERE nome = nomeJogo1;

    IF(v_count > 0) THEN
    	SELECT DISTINCT I.preco INTO v_aux FROM item I, produto P WHERE P.nome = nomeJogo1 AND P.nome = I.nomeProduto;
    	UPDATE jogo SET valorarrecadadocopiasvendidas = v_count*v_aux WHERE nome = nomeJogo1;
    ELSE UPDATE jogo SET valorarrecadadocopiasvendidas = 0 WHERE nome = nomeJogo1;
    END IF;

    -- valorarrecadadoconteudo

    SELECT SUM(C.preco*I.quantidade) INTO v_count FROM conteudo C, item I WHERE C.nomejogo = nomeJogo1 AND I.nomeProduto = C.nome;

    IF(v_count > 0) THEN
    	UPDATE jogo SET valorarrecadadoconteudo = v_count WHERE nome = nomeJogo1;
    ELSE UPDATE jogo SET valorarrecadadoconteudo = 0 WHERE nome = nomeJogo1;
    END IF;

    -- numusersdist

    SELECT COUNT(DISTINCT cadLogin) INTO v_count FROM joga WHERE instnome = nomeJogo1;
    
    IF(v_count > 0) THEN
      UPDATE jogo SET numusersdist = v_count WHERE nome = nomeJogo1;
    ELSE
      UPDATE jogo SET numusersdist = 0 WHERE nome = nomeJogo1;
    END IF;
    
    -- sumtotaltempo

    SELECT SUM(J.tempo) INTO v_count FROM joga J WHERE J.instnome = nomeJogo1;
    
    IF(v_count > 0) THEN
      UPDATE jogo SET sumtotaltempo = v_count WHERE nome = nomeJogo1;
    ELSE
      UPDATE jogo SET sumtotaltempo = 0 WHERE nome = nomeJogo1;
    END IF;
    
    -- numconteudosdist

    SELECT COUNT(C.nome) INTO v_count FROM conteudo C WHERE C.nomejogo = nomeJogo1;

    IF(v_count > 0) THEN
    	UPDATE jogo SET numconteudosdist = v_count WHERE nome = nomeJogo1;
    ELSE UPDATE jogo SET numconteudosdist = 0 WHERE nome = nomeJogo1;
    END IF;

    -- mediaitenscomprados

   select quant2 + quant1 into v_count from (select nvl(sum(i.quantidade),0) as quant1 from item i where (I.nomeProduto = nomeJogo1)), (SELECT nvl(sum(i.quantidade),0) as quant2 FROM conteudo C, item I WHERE (C.nomejogo = nomeJogo1 AND C.nome = I.nomeProduto));

    IF(v_count > 0) THEN
    	SELECT COUNT(DISTINCT I.compraid) INTO v_aux FROM item I, conteudo C WHERE I.nomeProduto = nomeJogo1 OR (C.nomejogo = nomeJogo1 AND C.nome = I.nomeProduto);
    	UPDATE jogo SET mediaitenscomprados = v_count/v_aux WHERE nome = nomeJogo1;
    ELSE UPDATE jogo SET mediaitenscomprados = 0 WHERE nome = nomeJogo1;
    END IF;

    -- varianciaitenscomprados

    -- mediatotalcompra

    -- varianciatotalcompra

    -- pontuacao

	SELECT nvl(numcopiasvendidas, 0), nvl(valorarrecadadocopiasvendidas,0), nvl(sumtotaltempo,0), nvl(valorarrecadadoconteudo,0)
		INTO v_ncopiasvendidas, v_valorarrecadadocopias, v_sumtotaltempo, v_valorarrecadadoconteudo FROM jogo WHERE nome = nomeJogo1;
		
	v_pontuacao := 0.3*v_ncopiasvendidas + 0.3*v_valorarrecadadocopias+ 0.2*v_sumtotaltempo + 0.2*v_valorarrecadadoconteudo;
	UPDATE jogo SET pontuacao = v_pontuacao WHERE nome = nomeJogo1;

  END LOOP;

  CLOSE c_jogos;

END;





DECLARE

  CURSOR c_jogos IS SELECT * FROM jogo;
  jogo1 jogo%ROWTYPE;
  v_min NUMBER;
  v_max NUMBER;
  v_atual NUMBER;
  v_pontuacao NUMBER;
  
BEGIN
    
    SELECT max(pontuacao) into v_max from jogo;
	SELECT min(pontuacao) into v_min from jogo;

  OPEN c_jogos;

  LOOP 

    FETCH c_jogos INTO jogo1;
    EXIT WHEN c_jogos%NOTFOUND;


	v_pontuacao := ((jogo1.pontuacao - v_min) / (v_max - v_min))*100;

	dbms_output.put_line(jogo1.nome || ' ' || v_pontuacao);

	UPDATE jogo SET pontuacao = v_pontuacao WHERE jogo1.nome = nome;

  END LOOP;

  CLOSE c_jogos;

END;