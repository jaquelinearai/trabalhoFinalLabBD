CREATE OR REPLACE
PACKAGE pkg_atualizaJogo AS

PROCEDURE procedure_updatePontuacao;
PROCEDURE procedure_descricaoJogo(nomeJogo1 VARCHAR2, str OUT VARCHAR2);

END pkg_atualizaJogo;



CREATE OR REPLACE
PACKAGE BODY pkg_atualizaJogo AS

	PROCEDURE procedure_updatePontuacao AS

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

			UPDATE jogo SET pontuacao = v_pontuacao WHERE jogo1.nome = nome;

	  	END LOOP;

		CLOSE c_jogos;

	END procedure_updatePontuacao;


	PROCEDURE procedure_descricaoJogo(nomeJogo1 VARCHAR2, str OUT VARCHAR2) AS
	  
	  usaType usa%ROWTYPE;
	  jogoType jogo%ROWTYPE;
	  trofeuType trofeu%ROWTYPE;

	  generoType genero.genero%TYPE;
	  legendaType legenda.idioma%TYPE;
	  linguagemType linguagem.idioma%TYPE;
	  conquistaType conquista.loginConta%TYPE;

	  CURSOR c_usa IS SELECT * FROM usa;
	  CURSOR c_trofeu IS SELECT * FROM trofeu;
	  CURSOR c_genero IS
	    SELECT genero FROM genero G, generosJogo GJ WHERE GJ.nomeJogo = jogoType.nome AND GJ.codGenero = G.codGenero;
	  CURSOR c_legenda IS
	    SELECT idioma FROM legenda L, legendasJogo LJ WHERE LJ.nomeJogo = jogoType.nome AND LJ.codLegenda = L.codLegenda;
	  CURSOR c_linguagem IS
	    SELECT idioma FROM linguagem L, linguagensJogo LJ WHERE LJ.nomeJogo = jogoType.nome AND LJ.codLinguagem = L.codLinguagem;
	  CURSOR c_conquista IS
	    SELECT loginConta FROM conquista WHERE nomeJogo = jogoType.nome GROUP BY loginConta;
	  
	  first NUMBER(1);
	  found NUMBER(1);

	  countOuro NUMBER;
	  countPrata NUMBER;
	  countBronze NUMBER;

	  strOuro VARCHAR2(1000);
	  strPrata VARCHAR2(1000);
	  strBronze VARCHAR2(1000);
	  strConquista VARCHAR2(1000);

	BEGIN

	  -- Jogo e preco

	  	SELECT * into jogoType from jogo where nome = nomeJogo1;

	    str := concat (concat ('Jogo ', jogoType.nome), ': ');
            str := concat (str, chr(10));

	    if(jogoType.preco >= 0) then
	      str := concat (concat (concat (str, 'vendido por R$ '), jogoType.preco), '. ');
	    else
	      str := concat (str, 'preco indisponivel. ');
	    end if;
      str := concat (str, chr(10));
	    
	    -- Plataforma e versao

	    OPEN c_usa;
	    
	    first := 0;
	    found := 0;
	    
	    LOOP

	      FETCH c_usa INTO usaType;
	      EXIT WHEN c_usa%NOTFOUND;
	      
	      if (usaType.nomeJogo = jogoType.nome) then

	        found := 1;
	        
	        if (first= 0) then
	          str := concat (concat (concat (concat (str, 'Disponivel para a plataforma: '), usaType.nomePlataforma), ' '), usaType.versaoPlataforma);
	          first:= 1;
	        else
	          str := concat (concat (concat (concat (str, ', '), usaType.nomePlataforma), ' '), usaType.versaoPlataforma);

	        end if;

	      end if;
	    
	    END LOOP;
    
	    if (found = 0) then
	      str := concat (str, 'Plataforma indisponivel, ');
	    else
	      str := concat (str, ', ');
	    end if;

	    CLOSE c_usa;
      
      str := concat (str, chr(10));

	    -- Generos

	    OPEN c_genero;

	    found := 0;
	    str := concat (str, 'de genero ');

	    LOOP

	      FETCH c_genero INTO generoType;
	      EXIT WHEN c_genero%NOTFOUND;

	      found := 1;
	      str := concat (concat (str, generoType), ', ');

	    END LOOP;

	      if (found = 0) then
	        str := concat (str, 'indisponivel, ');
	      end if;

	    CLOSE c_genero;
      str := concat (str, chr(10));
      
	    -- Legendas

	    OPEN c_legenda;

	    found := 0;
	    str := concat (str, 'com legendas em ');

	    LOOP

	      FETCH c_legenda INTO legendaType;
	      EXIT WHEN c_legenda%NOTFOUND;

	      found := 1;
	      str := concat (concat (str, legendaType), ', ');

	    END LOOP;

	      if (found = 0) then
	        str := concat (str, 'indisponivel, ');
	      end if;

	    CLOSE c_legenda;
      str := concat (str, chr(10));
      
	    -- Linguagens

	    OPEN c_linguagem;

	    found := 0;
	    first := 0;
	    
	    LOOP

	      FETCH c_linguagem INTO linguagemType;
	      EXIT WHEN c_linguagem%NOTFOUND;

	      found := 1;
	      if (first = 0) then
	        str := concat (str, 'linguagens ');
	        first := 1;
	      elsif (first = 1) then
	        str := concat (str, linguagemType);
	        first := 2;
	      elsif (first = 2) then
	        str := concat (concat (str, ', '), linguagemType);
	      end if;

	    END LOOP;
    
	      if (found = 0) then
	        str := concat (str, 'linguagem indisponivel. ');
	      else 
	        str := concat (str, '. ');
	      end if;

	    CLOSE c_linguagem;
      str := concat (str, chr(10));
      
	    -- Trofeu

	    OPEN c_trofeu;

	    countOuro := 0;
	    countPrata := 0;
	    countBronze := 0;

	    strOuro := '';
	    strPrata := '';
	    strBronze := '';

	    str := concat(str, 'Trofeis disponiveis: ');

	    LOOP

	      FETCH c_trofeu INTO trofeuType;
	      EXIT WHEN c_trofeu%NOTFOUND;

	      if (trofeuType.nomeJogo = jogoType.nome AND trofeuType.tipo = 'BRONZE') then
	        countBronze := countBronze + 1;
	        strBronze := concat (concat (strBronze, ' - '), trofeuType.objetivo);
	      elsif (trofeuType.nomeJogo = jogoType.nome AND trofeuType.tipo = 'PRATA') then
	        countPrata := countPrata + 1;
	        strPrata := concat (concat (strPrata, ' - '), trofeuType.objetivo);
	      elsif (trofeuType.nomeJogo = jogoType.nome AND trofeuType.tipo = 'OURO') then
	        countOuro := countOuro + 1;
	        strOuro := concat (concat (strOuro, ' - '), trofeuType.objetivo);
	      end if;

	    END LOOP;

	    str := concat (concat (concat (concat (str, to_char(countBronze)), ' de BRONZE'), strBronze), ', ');
      str := concat (str, chr(10));
	    str := concat (concat (concat (concat (str, to_char(countPrata)), ' de PRATA'), strPrata), ', ');
      str := concat (str, chr(10));
	    str := concat (concat (concat (concat (str, to_char(countOuro)), ' de OURO'), strOuro), '. ');
      str := concat (str, chr(10));

	    CLOSE c_trofeu;
      str := concat (str, chr(10));
      
	    -- Conquista

	    OPEN c_conquista;

	    found := 0;
	    
	    LOOP

	      FETCH c_conquista INTO conquistaType;
	      EXIT WHEN c_conquista%NOTFOUND;

	      found:= 1;

	      SELECT count(*) INTO countBronze
	        FROM Trofeu T, Conquista C WHERE T.nomeJogo = jogoType.nome AND C.idTrofeu = T.idTrofeu
	        AND T.tipo = 'BRONZE' AND C.loginConta = conquistaType;
	      SELECT count(*) INTO countPrata
	        FROM Trofeu T, Conquista C WHERE T.nomeJogo = jogoType.nome AND C.idTrofeu = T.idTrofeu
	        AND T.tipo = 'PRATA' AND C.loginConta = conquistaType;
	      SELECT count(*) INTO countOuro
	        FROM Trofeu T, Conquista C WHERE T.nomeJogo = jogoType.nome AND C.idTrofeu = T.idTrofeu
	        AND T.tipo = 'OURO' AND C.loginConta = conquistaType;

	      if (countBronze + countPrata + countOuro > 0) then
	        str := concat (concat (concat (concat (concat (concat (concat (concat (concat (str, 'O usuario '), conquistaType), ' conquistou '), countBronze), ' de BRONZE, '), countPrata), ' de PRATA, '), countOuro), ' de OURO. ');
	      end if;
        str := concat (str, chr(10));
	    END LOOP;

	    if (found = 0) then
	      str := concat (str, 'Nenhum usuario ganhou trofeus no jogo.');
	    end if;

	    CLOSE c_conquista;
	    
	    str := concat (str, chr(10));
	  
	  --

	END procedure_descricaoJogo;


END pkg_atualizaJogo;