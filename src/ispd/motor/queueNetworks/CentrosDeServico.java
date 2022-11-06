package ispd.motor.queueNetworks;

import ispd.motor.statistics.MetricaCS;

import java.util.HashSet;

public class CentrosDeServico {
    private int idCS;
    private int tipo; // 0: maquina; 1: cluster; 2: conexao de rede simples;
	// 3: Internet
    private int numMaxServidores;  //Capacidade maxima de servidores que
	// podem ser adicionados ao CS.
    //Esse numero considera apenas servidores de processamento (mestre e
	// escravos)
    //ou seja, se esse numero for 100, o servidor tera 101 servidores, sendo:
    //1 servidor de comunicacao, 1 mestre e 99 escravosm (os dois ultimos sao
	// do tipo "processamento!")
    private int numAtualServidores;  //Quantidade de servidores ja
	// adicionados ao CS OBS: Em qualquer momento numAtualServidores e no
	// maximo igual a numMaxServidores
    private int numServidoresLivres;
    private int numMaxFilas;
    private int numAtualFilas;
    private int indiceEscravoAtualRR;
    private int indiceEscravoAtualRRCluster;
    private boolean ehEscalonador;
    private Double tServicoProctoTotal;
    private HashSet<Servidores> servidores;
    private HashSet<Filas> filas;
    private int vetorEscravos[];
    private int matrizCS[][];
    private int numEscravos;

    private MetricaCS metCSCDS;

    //Adiciona CSs Homogeneos ( maquinas (servidores de processamento),
	// conexao de rede simples ou
    //Internet(servidores de comunicacao) ).
    public CentrosDeServico(int identCS, int tp, int nMaxServ, int nMaxFilas,
							int nEscravos, int vetEscravos[]) {
        setIdCS(identCS);
        setTipo(tp);
        setNumMaxServidores(nMaxServ);
        setNumAtualServidores(0); //Na inicializacao do CS nao ha adicao de
		// servidores.
        setNumServidoresLivres(getNumAtualServidores());  //Inicialmente, nao
		// ha servidor, entao nao ha servidor livre
        setNumMaxFilas(nMaxFilas);
        setNumAtualFilas(0);
        setNumEscravos(nEscravos);
        setVetorEscravos(vetEscravos);
        setIndiceEscravoAtualRR(0);
        setIndiceEscravoAtualRRCluster(1);
        servidores = new HashSet<Servidores>();
        filas = new HashSet<Filas>();

    }

    public int getNumAtualServidores() {
        return numAtualServidores;
    }

    public void setNumAtualServidores(int nAtualServ) {
        numAtualServidores = (nAtualServ >= 0) ? nAtualServ : 0;
    }

    //Adiciona CSs Heterogeneos (que tenham servidores de processamento e
	// servidores de comunicacao
    //ou seja, CSs do tipo 1 (clusters) ).
    public CentrosDeServico(int identCS, int tp, int nMaxServ, int nMaxFilas,
							int servCom, int nEscravos, int vetEscravos[]) {
        setIdCS(identCS);
        setTipo(tp);
        setNumMaxServidores(nMaxServ);
        setNumAtualServidores(0); //Na inicializacao do CS nao ha adicao de
		// servidores.
        setNumServidoresLivres(getNumAtualServidores());  //Inicialmente, nao
		// ha servidor, entao nao ha servidor livre
        setNumMaxFilas(nMaxFilas);
        setNumAtualFilas(0);
        setNumEscravos(nEscravos);
        setVetorEscravos(vetEscravos);
        setIndiceEscravoAtualRR(0);
        setIndiceEscravoAtualRRCluster(1);
        servidores = new HashSet<Servidores>();
        filas = new HashSet<Filas>();
    }

    public CentrosDeServico(int identCS, int tp, int nMaxServ, int nMaxFilas) {
        setIdCS(identCS);
        setTipo(tp);
        setNumMaxServidores(nMaxServ);
        setNumAtualServidores(0); //Na inicializacao do CS nao ha adicao de
		// servidores.
        setNumServidoresLivres(getNumAtualServidores());  //Inicialmente, nao
		// ha servidor, entao nao ha servidor livre
        setNumMaxFilas(nMaxFilas);
        setNumAtualFilas(0);
        if (tp == 1)
            setIndiceEscravoAtualRRCluster(1);
        servidores = new HashSet<Servidores>();
        filas = new HashSet<Filas>();
    }

    public CentrosDeServico(int identCS, int tp, int nMaxServ, int nMaxFilas,
							int servCom) {
        setIdCS(identCS);
        setTipo(tp);
        setNumMaxServidores(nMaxServ);
        setNumAtualServidores(0); //Na inicializacao do CS nao ha adicao de
		// servidores.
        System.out.printf("getNumAtualServidores() = %d",
				getNumAtualServidores());
        setNumServidoresLivres(getNumAtualServidores());  //Inicialmente, nao
		// ha servidor, entao nao ha servidor livre
        setNumMaxFilas(nMaxFilas);
        setNumAtualFilas(0);
        if (tp == 1)
            setIndiceEscravoAtualRRCluster(1);
        servidores = new HashSet<Servidores>();
        filas = new HashSet<Filas>();
    }

    public int getNumServidoresLivres() {
        return numServidoresLivres;
    }

    public void setNumServidoresLivres(int nServLivres) {
        numServidoresLivres = (nServLivres >= 0) ? nServLivres : 0;
    }

    public int getNumMaxFilas() {
        return numMaxFilas;
    }

    public void setNumMaxFilas(int nMaxFilas) {
        numMaxFilas = (nMaxFilas >= 0) ? nMaxFilas : 0;
    }

    public int getNumAtualFilas() {
        return numAtualFilas;
    }

    public void setNumAtualFilas(int nAtualFilas) {
        numAtualFilas = (nAtualFilas >= 0) ? nAtualFilas : 0;
    }

    public int getIndiceEscravoAtualRR() {
        return indiceEscravoAtualRR;
    }

    public void setIndiceEscravoAtualRR(int escAtual) {
        indiceEscravoAtualRR = (escAtual >= 0) ? escAtual : 0;
    }

    public int getIndiceEscravoAtualRRCluster() {
        return indiceEscravoAtualRRCluster;
    }

    public void setIndiceEscravoAtualRRCluster(int escAtual) {
        indiceEscravoAtualRRCluster = (escAtual >= 1) ? escAtual : 1;
    }

    public int getNumEscravos() {
        return numEscravos;
    }

    public void setNumEscravos(int nEscravos) {
        numEscravos = (nEscravos >= 0) ? nEscravos : 0;
    }

    public int[] getVetorEscravos() {
        return vetorEscravos;
    }

    public void setVetorEscravos(int vetEscravos[]) {
        vetorEscravos = vetEscravos;
        this.setNumEscravos(this.vetorEscravos.length);
        System.out.printf("\nthis.vetorEscravos.length - " + this.vetorEscravos.length);
        for (int i = 0; i < this.vetorEscravos.length; i++) {
            System.out.printf("\nthis.vetorEscravos[" + i + "] - " + this.vetorEscravos[i]);
        }
    }

    public Double getTServicoProctoTotal() {
        return tServicoProctoTotal;
    }

    public void setTServicoProctoTotal(Double tServProctoTotal) {
        tServicoProctoTotal = (tServProctoTotal >= 0.0) ? tServProctoTotal :
				0.0;
    }

    public void adicionaServidorProcto(int tpServ, boolean msOuEsc,
									   Double tServProcto, MetricaCS atualCS) {
        Servidores serv = new Servidores(getNumAtualServidores(), tpServ,
				msOuEsc, tServProcto);

        //MARCO
        metCSCDS = atualCS;

        if (msOuEsc == true)
            ehEscalonador = true;
        else
            ehEscalonador = false;

        if (getEhEscalonador() == false)
            System.out.printf("o CS %d NAO eh escalonador\n", getIdCS());
        else
            System.out.printf("o CS %d eh escalonador\n", getIdCS());
        servidores.add(serv); // Adiciona no HashSet, ou seja, "add" eh um
		// metodo da classe HashSet

        //MARCO
        atualCS.addMedidasServidor(getNumAtualServidores(), 0);

        System.out.printf("|\tServidor ID: %2d adicionado ao Centro de " +
						  "Servico ID: %2d |\n",
                getNumAtualServidores(), getIdCS());
        alteraNumAtualServidores(1); //Soma 1 ao numero atual de servidores
        alteraNumAtualServidoresLivres(1);// Soma 1 ao numero de servidores
		// livres

    }

    public boolean getEhEscalonador() {
        return ehEscalonador;
    }

    public int getIdCS() {
        return idCS;
    }

    public void setIdCS(int id) {
        idCS = (id >= 0) ? id : 0;
    }

    public void alteraNumAtualServidores(int aSomar) {
        numAtualServidores += aSomar;
    }

    public void alteraNumAtualServidoresLivres(int aSomar) {
        numServidoresLivres += aSomar;
    }

    public void adicionaServidorCom(int tpServ, Double tServRede, double ltnc
			, double txOcup, MetricaCS atualCS) {
        Servidores serv = new Servidores(getNumAtualServidores(), tpServ,
				tServRede, ltnc, txOcup);

        //MARCO
        metCSCDS = atualCS;

        ehEscalonador = false;
        if (getEhEscalonador() == false)
            System.out.printf("o CS %d NAO eh escalonador\n", getIdCS());
        else
            System.out.printf("o CS %d eh escalonador\n", getIdCS());
        servidores.add(serv); // Adiciona no HashSet, ou seja, "add" eh um
		// metodo da classe HashSet

        //MARCO
        atualCS.addMedidasServidor(getNumAtualServidores(), 1);

        System.out.printf("|\tServidor ID: %2d adicionado ao Centro de " +
						  "Servico ID: %2d |\n",
                getNumAtualServidores(), getIdCS());
        alteraNumAtualServidores(1); //Soma 1 ao numero atual de servidores
        alteraNumAtualServidoresLivres(1);// Soma 1 ao numero de servidores
		// livres

    }

    public void adicionaServidoresInt(int tpServ, Double tServRede,
									  double ltnc, double txOcup,
									  MetricaCS atualCS) {
        ehEscalonador = false;

        //MARCO
        metCSCDS = atualCS;

        if (getEhEscalonador() == false)
            System.out.printf("o CS %d NAO eh escalonador\n", getIdCS());
        else
            System.out.printf("o CS %d eh escalonador\n", getIdCS());
        while (getNumAtualServidores() < getNumMaxServidores()) {
            Servidores serv = new Servidores(getNumAtualServidores(), tpServ,
					tServRede, ltnc, txOcup);
            servidores.add(serv); // Adiciona no HashSet, ou seja, "add" eh
			// um metodo da classe HashSet

            //MARCO
            atualCS.addMedidasServidor(getNumAtualServidores(), 1);

            System.out.printf("|\tServidor ID: %2d adicionado ao Centro de " +
							  "Servico ID: %2d |\n",
                    getNumAtualServidores(), getIdCS());
            alteraNumAtualServidores(1); //Soma 1 ao numero atual de
			// servidores
            alteraNumAtualServidoresLivres(1);// Soma 1 ao numero de
			// servidores livres
        }
    }

    public int getNumMaxServidores() {
        return numMaxServidores;
    }

    public void setNumMaxServidores(int nMaxServ) {
        numMaxServidores = (nMaxServ >= 0) ? nMaxServ : 0;
    }

    //+--------------------------------+
    public void adicionaServidoresClr(int tpServ, Double tServProcto,
									  Double tServRede, double ltnc,
									  double txOcup, MetricaCS atualCS) {
        Double tServInd;
        //MARCO
        metCSCDS = atualCS;

        ehEscalonador = true;
        if (getEhEscalonador() == false)
            System.out.printf("o CS %d NAO eh escalonador\n", getIdCS());
        else
            System.out.printf("o CS %d eh escalonador\n", getIdCS());
        tServInd = tServProcto / getNumMaxServidores();

        //Adiciona o mestre
        Servidores mestre = new Servidores(getNumAtualServidores(), tpServ,
				true, tServInd);
        //O "true" indica que os servidores sao escravos;
        servidores.add(mestre); // Adiciona no HashSet, ou seja, "add" eh um
		// metodo da classe HashSet

        //MARCO
        atualCS.addMedidasServidor(getNumAtualServidores(), 0);

        System.out.printf("|\tServidor ID: %2d adicionado ao Centro de " +
						  "Servico ID: %2d |\n",
                getNumAtualServidores(), getIdCS());
        alteraNumAtualServidores(1); //Soma 1 ao numero atual de servidores
        alteraNumAtualServidoresLivres(1);// Soma 1 ao numero de servidores
		// livres

        //Adiciona os escravos
        while (getNumAtualServidores() < getNumMaxServidores()) {
            Servidores escravo = new Servidores(getNumAtualServidores(),
					tpServ, false, tServInd);
            //O "false" indica que os servidores sao escravos;
            servidores.add(escravo); // Adiciona no HashSet, ou seja, "add"
			// eh um metodo da classe HashSet

            //MARCO
            atualCS.addMedidasServidor(getNumAtualServidores(), 0);

            System.out.printf("|\tServidor ID: %2d adicionado ao Centro de " +
							  "Servico ID: %2d |\n",
                    getNumAtualServidores(), getIdCS());
            alteraNumAtualServidores(1); //Soma 1 ao numero atual de
			// servidores
            alteraNumAtualServidoresLivres(1);// Soma 1 ao numero de
			// servidores livres
        }
        //Apos adicionar todos os servidores de processamento, adiciono o
		// servidor de comunicacao
        //que, pelo menos por enquanto, e unico (no cluster - barramento)

        //O id do servidor de comunicacao eh getNumAtualServidores()
        //pois os servidores de comunicacao sao numerados de 0 a
		// getNumAtualServidores()-1
        //Ou seja, o proximo numero inteiro e getNumAtualServidores()
        System.out.printf("|\tServidor DE COMUNICACAO ID: %2d adicionado ao " +
						  "Centro de Servico ID: %2d |\n",
                getNumAtualServidores(), getIdCS());
        Servidores serv = new Servidores(getNumAtualServidores(), 1,
				tServRede, ltnc, txOcup);
        servidores.add(serv);

        //MARCO
        atualCS.addMedidasServidor(getNumAtualServidores(), 1);

        //Nao precisa adicionar valor a  numAtualServidores, pois o servidor
		// e de comunicacao e essa
        //variavel controla o numero de servidores de processamento (ate pq,
		// como dito logo acima, o
        //servidor de comunicacao e unitario!

        //O "setamento" da variavel tServicoProctoTotal e so para "ter", pq
		// ela soh e usada para "impressao"
        setTServicoProctoTotal(tServProcto);

    }

    //+--------------------------------Metodo que adiciona uma fila ao centro
	// de servico-------------------------------+
    public void adicionaFila() {
        Filas fila = new Filas(numAtualFilas);

        filas.add(fila);
        System.out.printf("|\tFila ID: %2d adicionada ao Centro de Servico " +
						  "ID: %2d     |\n", numAtualFilas, idCS);
        alteraNumAtualFilas(1);
    }

    public void alteraNumAtualFilas(int aSomar) {
        numAtualFilas += aSomar;
    }

    public Double adicionaTarefaFila(NoFila tarefa) {
        Double retornoFuncao = 0.0;  //Eh o tempo que o sistema levou para
		// adicionar uma tarefa na fila.
        //ver metodo adicionaTarefa da classe Filas.java!

        for (Filas temp : filas)   //Percorre todo o HashSet filas
        { //  System.out.printf("temp.getIdFila() = %d\n", temp.getIdFila() );
            //  System.out.printf("tarefa.getIdFila() = %d\n", tarefa
			//  .getIdFila() );
            if (temp.getIdFila() == tarefa.getIdFila())   //Encontra-se a
				// fila na qual se deseja adicionar a tarefa
            { //  System.out.printf("if( temp.getIdFila() == tarefa.getIdFila
				// () )" );
                retornoFuncao = temp.adicionaTarefaFila(tarefa);   //Adiciona
				// -se a tarefa
            }
        }
        return retornoFuncao;
    }

    public int determinaProximoEscravoRR() {
        int retornoFuncao = 0;
        int indiceEscravoAtualRRLocal = 0;
        if ((0 <= indiceEscravoAtualRR) && (indiceEscravoAtualRR <= numEscravos - 1)) {
            indiceEscravoAtualRRLocal = indiceEscravoAtualRR;
        } else if (indiceEscravoAtualRR >= numEscravos) {
            indiceEscravoAtualRRLocal = 0;
            indiceEscravoAtualRR = 0;
        }
        retornoFuncao = vetorEscravos[indiceEscravoAtualRRLocal];
        indiceEscravoAtualRR++;

        return retornoFuncao;
    }

    public NoFila peekFilaMestre() {
        for (Filas temp : filas) {
            if (temp.getIdFila() == 0)  //A fila do Mestre de processamento e
				// sempre a fila de id 0
            {
                return temp.peek();
            }
        }
        return null;
    }

    public NoFila peekFilaComun() {
        System.out.printf("getTipo() = %d \n", getTipo());
        System.out.printf("getIdCS() = %d \n", getIdCS());
        if (getTipo() == 1)   //Se for um cluster, pegar a tarefa na fila de
			// comunicacao (id 1)
        {
            for (Filas temp : filas) {
                if (temp.getIdFila() == 1)  //A fila de comunicacao de um
					// cluster e sempre a fila de id 1
                {
                    return temp.peek();
                }
            }
        } else //if( getTipo() == 2 || getTipo() == 3)
        {
            NoFila tarefaRetorno = null;
            for (Filas temp : filas) {
                if (temp.getIdFila() == 0)  //A fila de comunicacao de CSs
					// tanto de conexao de rede (tipo 2)
                {
                    System.out.printf("temp.getIdFila() == 0\n");
                    tarefaRetorno = temp.peek();
                    System.out.printf("id da tarefa peek %d\n",
							tarefaRetorno.getIdTarefa());
                    return tarefaRetorno;      //quanto de Internet (tipo 3)
					// e a fila principal, ou seja, id 0
                }
            }
        }
        return null;
    }

    public int getTipo() {
        return tipo;
    }

    private void setTipo(int tp) {
        tipo = (tp >= 0 && tp <= 3) ? tp : 0;
    }

    public void pollFila(NoFila tarefa) {
        for (Filas temp : filas) {
            if (temp.getIdFila() == tarefa.getIdFila()) {
                System.out.printf("Tarefa %d retirada da fila %d do CS id %d " +
								  "\n", tarefa.getIdTarefa(),
						tarefa.getIdFila(), tarefa.getIdCSAtual());
                temp.poll();  // Nao eh uma chamada recursiva (ou seja, para
				// o proprio metodo), mas
                //sim para o metodo de mesmo nome, MAS DA CLASSE FILA
            }
        }
    }

    public Double atribuiTarefaServComun(NoFila tarefa, MetricaCS atualCS) {
        Double retornoFuncao = 0.0;

        if (getTipo() == 2 || getTipo() == 3) {
            for (Servidores servTemp : servidores) {
                if (servTemp.getIdServidor() == 0) {
                    retornoFuncao = servTemp.atribuiTarefaServComun(tarefa);
                    System.out.printf("A tarefa %d teria que ser alocada no " +
									  "servidor %d do CS %d\n",
                            tarefa.getIdTarefa(), 0, tarefa.getIdCSAtual());
                    System.out.printf("e foi alocada no CS %d\n", idCS);
                    tarefa.setIdServidorAtual(0); // Motivo do argumento ser
					// 0: o id do servidor escravo
                    // um cluster e sempre 0!
                    //MARCO
                    atualCS.addNoMedidasServidor(0, 1,
							tarefa.getTamanhoComTarefa() / servTemp.getTServicoRede() + servTemp.getLatencia());
                }
            }
        } else if (getTipo() == 1) {
            for (Servidores servTemp : servidores) {
                if (servTemp.getIdServidor() == getNumMaxServidores()) {
                    retornoFuncao = servTemp.atribuiTarefaServComun(tarefa);
                    System.out.printf("A tarefa %d teria que ser alocada no " +
									  "servidor %d do CS %d\n",
                            tarefa.getIdTarefa(), getNumMaxServidores(),
							tarefa.getIdCSAtual());
                    System.out.printf("e foi alocada no CS %d\n", idCS);
                    tarefa.setIdServidorAtual(getNumMaxServidores()); //
					// Motivo do argumento ser 0: o id do servidor escravo
                    // um cluster e sempre 0!
                    //MARCO
                    atualCS.addNoMedidasServidor(servTemp.getIdServidor(), 1,
							tarefa.getTamanhoComTarefa() / servTemp.getTServicoRede() + servTemp.getLatencia());
                }
            }
        }
        return retornoFuncao;
    }

    public Double envioTarefa(NoFila tarefa) {
        Double retornoFuncao = 0.0;

        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                retornoFuncao =
						tarefa.getTamanhoComTarefa() / servTemp.getTServicoRede() + servTemp.getLatencia();
            }
        }
        System.out.printf("retornoFuncao(envio) = %f\n", retornoFuncao);
        return retornoFuncao;
    }

    public Double processamentoTarefa(NoFila tarefa) {
        Double retornoFuncao = 0.0;

        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                System.out.printf("O servidor %d do CS %d esta processando a " +
								  "tarefa %d\n", servTemp.getIdServidor(),
						getIdCS(), tarefa.getIdTarefa());
                retornoFuncao =
						tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto();
                System.out.printf("Processamento feito em %f\n", retornoFuncao);
            }
        }
        return retornoFuncao;
    }

    public Double saidaTarefaServidor(NoFila tarefaAtual) {
        Double retornoFuncao = 0.0;

        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefaAtual.getIdServidorAtual()) {
                servTemp.setIdTarefaAtual(-1);
                tarefaAtual.setIdServidorAtual(-1);
                servTemp.liberaServidor();
            }
        }

        return retornoFuncao;
    }

    public Double atribuiTarefaServProc(NoFila tarefa,
										Double instanteOcupacaoServidor,
										MetricaCS atualCS) {
        Double retornoFuncao = 0.0;

        if (getTipo() == 0) {
            for (Servidores servTemp : servidores) {
                if (servTemp.getIdServidor() == 0) {
                    retornoFuncao = servTemp.atribuiTarefaServProc(tarefa,
							instanteOcupacaoServidor);
                    System.out.printf("A tarefa %d foi alocada no servidor %d" +
									  " do CS %d\n",
                            tarefa.getIdTarefa(), tarefa.getIdServidorAtual()
							, tarefa.getIdCSAtual());
                    servTemp.setTamanhoProcTarefaAtual(tarefa);
                    servTemp.setInstanteOcupacao(instanteOcupacaoServidor);
                    servTemp.setTempoPrevistoLiberacao(instanteOcupacaoServidor + tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto());

                    //MARCO
                    atualCS.addNoMedidasServidor(servTemp.getIdServidor(), 0,
							tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto());
                }
            }
        } else if (getTipo() == 1) {
            for (Servidores servTemp : servidores) {
                if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                    retornoFuncao = servTemp.atribuiTarefaServProc(tarefa,
							instanteOcupacaoServidor);
                    System.out.printf("A tarefa %d foi alocada no servidor %d" +
									  " do CS %d\n",
                            tarefa.getIdTarefa(), tarefa.getIdServidorAtual()
							, tarefa.getIdCSAtual());
                    servTemp.setTamanhoProcTarefaAtual(tarefa);
                    servTemp.setInstanteOcupacao(instanteOcupacaoServidor);
                    servTemp.setTempoPrevistoLiberacao(tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto());

                    //MARCO
                    atualCS.addNoMedidasServidor(servTemp.getIdServidor(), 0,
							tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto());
                }
            }
        }
        return retornoFuncao;
    }

    public Double atribuiTarefaServProc(NoFila tarefa, MetricaCS atualCS) {
        Double retornoFuncao = 0.0;

        if (getTipo() == 0) {
            for (Servidores servTemp : servidores) {
                if (servTemp.getIdServidor() == 0) {
                    retornoFuncao = servTemp.atribuiTarefaServProc(tarefa);
                    System.out.printf("A tarefa %d foi alocada no servidor %d" +
									  " do CS %d\n",
                            tarefa.getIdTarefa(), tarefa.getIdServidorAtual()
							, tarefa.getIdCSAtual());
                    servTemp.setTamanhoProcTarefaAtual(tarefa);

                    //MARCO
                    atualCS.addNoMedidasServidor(servTemp.getIdServidor(), 0,
							tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto());
                }
            }
        } else if (getTipo() == 1) {
            for (Servidores servTemp : servidores) {
                if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                    retornoFuncao = servTemp.atribuiTarefaServProc(tarefa);
                    System.out.printf("A tarefa %d foi alocada no servidor %d" +
									  " do CS %d\n",
                            tarefa.getIdTarefa(), tarefa.getIdServidorAtual()
							, tarefa.getIdCSAtual());
                    servTemp.setTamanhoProcTarefaAtual(tarefa);

                    //MARCO
                    atualCS.addNoMedidasServidor(servTemp.getIdServidor(), 0,
							tarefa.getTamanhoProcTarefa() / servTemp.getTServicoProcto());
                }
            }
        }
        return retornoFuncao;
    }

    public Double getTamanhoProcTarefaAtual(NoFila tarefa) {
        Double retornoFuncao = 0.0;

        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                retornoFuncao = servTemp.getTamanhoProcTarefaAtual();
            }
        }

        return retornoFuncao;
    }

    public boolean verificaSeServEMestre(NoFila tarefa) {
        boolean retornoFuncao = false;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                retornoFuncao = servTemp.getMestreEscravo();
            }
        }
        return retornoFuncao;
    }

    public boolean verificaSeServEMestre(int idServ) {
        boolean retornoFuncao = false;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == idServ) {
                retornoFuncao = servTemp.getMestreEscravo();
            }
        }
        return retornoFuncao;
    }

    /* A versao abaixo do metodo VerificaMestreLivre( ) e chamada para um CS
    que contem um cluster (neste caso o mestre sempre
    e o servidor de id 0 )
    */
    public boolean verificaMestreLivre() {
        boolean retornoFuncao = false;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == 0) {
                retornoFuncao = servTemp.getServidorLivre();
            }
        }
        return retornoFuncao;
    }


    public boolean verificaFilaComunVazia(int tipoCS) {
        boolean retornoFuncao = false;
        int tamFila;
        boolean servZeroEMestre = false;

        if (tipoCS == 1) {
            for (Filas temp : filas) {
                if (temp.getIdFila() == 1) {
                    tamFila = temp.size();
                    if (tamFila == 0)
                        retornoFuncao = true;
                    else
                        retornoFuncao = false;
                }
            }
        } else {
            for (Filas temp : filas) {
                if (temp.getIdFila() == 0) {
                    tamFila = temp.size();
                    if (tamFila == 0)
                        retornoFuncao = true;
                    else
                        retornoFuncao = false;
                }
            }
        }

        return retornoFuncao;
    }

    public boolean verificaFilaMestreVazia(int tipoCS) {
        boolean retornoFuncao = false;
        int tamFila;
        boolean servZeroEMestre = false;


        if (tipoCS == 0 || tipoCS == 1) {
            for (Filas temp : filas) {
                if (temp.getIdFila() == 0) {
                    tamFila = temp.size();
                    if (tamFila == 0)
                        retornoFuncao = true;
                    else
                        retornoFuncao = false;
                }
            }
        }

        return retornoFuncao;
    }


    public Double atribuiTarefaMestre(NoFila tarefa, int idCS,
									  MetricaCS atualCS) {
        Double retornoFuncao = 0.0;

        System.out.printf("A tarefa %d teria que ser alocada no MESTRE do CS " +
						  "%d\n",
                tarefa.getIdTarefa(), tarefa.getIdCSAtual());
        System.out.printf("e foi alocada no CS %d\n", idCS);
        tarefa.setIdServidorAtual(0); // Motivo do argumento ser 0: o id do
		// servidor mestre de
        // um cluster e sempre 0!

        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == 0) {
                retornoFuncao = servTemp.atribuiTarefaMestre(tarefa);

                //MARCO
                atualCS.addNoMedidasServidor(0, 1, retornoFuncao);
            }
        }

        return retornoFuncao;
    }

    public Double getTServicoProcto(NoFila tarefa) {
        Double retornoFuncao = 0.0;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorDestino()) {
                retornoFuncao = servTemp.getTServicoProcto();
            }
        }
        return retornoFuncao;
    }

    public int buscaIdTarefaAlocadaMestre() {
        int retornoFuncao = -1;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == 0) {
                retornoFuncao = servTemp.getIdTarefaAtual();
            }
        }
        return retornoFuncao;
    }

    public int buscaIdTarefaAlocadaServidor(NoFila tarefa) {
        int retornoFuncao = -1;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                retornoFuncao = servTemp.getIdTarefaAtual();
            }
        }
        return retornoFuncao;
    }

    public Double calculaDeltaAtualizacaoLEF(NoFila tarefa) {
        Double retornoFuncao = 0.0;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {
                retornoFuncao =
						servTemp.getTempoPrevistoLiberacao() - tarefa.getTempoChegadaFila();
            }
        }
        return retornoFuncao;
    }

    //Essa funcao praticamente ja faz o escalonamento RR sozinha
    public int verificaSeHaEscravoLivre() {
        int retornoFuncao = -1;
        int indiceEscravoAtualRRLocal = 1;
        boolean escravoLivre = false;
        System.out.printf("indiceEscravoAtualRRCluster(fora) = %d\n",
				indiceEscravoAtualRRCluster);
        do {
            if ((1 <= indiceEscravoAtualRRCluster) && (indiceEscravoAtualRRCluster <= numMaxServidores - 1)) {
                indiceEscravoAtualRRLocal = indiceEscravoAtualRRCluster;
                indiceEscravoAtualRRCluster++;
                System.out.printf("indiceEscravoAtualRRCluster (dentro)= " +
								  "%d\n", indiceEscravoAtualRRCluster);
            }

            escravoLivre =
					verificaSeServidorEstaLivre(indiceEscravoAtualRRLocal);
        }
        while ((escravoLivre == false) && (indiceEscravoAtualRRLocal < numMaxServidores - 1));

        if (indiceEscravoAtualRRCluster > numMaxServidores - 1) {
            indiceEscravoAtualRRCluster = 1;
        }

        if (escravoLivre == true) {
            retornoFuncao = indiceEscravoAtualRRLocal;
        } else
            retornoFuncao = -1;
        System.out.printf("escravo indicado: %d\n", retornoFuncao);
        return retornoFuncao;
    }

    /*O metodo abaixo e sobrescrito. Uma versao quando se da o id do Servidor
     diretamente e a outra
    quando se da a tarefa pra ver se o servidor que ela vai (ou esta) esta
    livre.
    */
    public boolean verificaSeServidorEstaLivre(int idServ) {
        boolean retornoFuncao = false;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == idServ) {
                retornoFuncao = servTemp.getServidorLivre();
            }
        }
        return retornoFuncao;
    }

    public Double saidaTarefaMestre(NoFila tarefaAtual) {
        Double retornoFuncao = 0.0;

        for (Servidores servTemp : servidores) {   //A segunda condicao eh
			// soh por garantia
            if (servTemp.getIdServidor() == 0 && tarefaAtual.getIdServidorAtual() == 0) {
                servTemp.setIdTarefaAtual(-1);
                tarefaAtual.setIdServidorAtual(-1);
                servTemp.liberaServidor();
            }
        }

        return retornoFuncao;
    }

    public boolean verificaSeServidorEstaLivre(NoFila tarefa) {
        boolean retornoFuncao = false;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == tarefa.getIdServidorAtual()) {   //System.out.printf("********servTemp.getIdServidor() = %d\n", servTemp.getIdServidor() );
                retornoFuncao = servTemp.getServidorLivre();
            }
        }
        return retornoFuncao;
    }

    public void confereCS() {
        int i, j;
        int idOrigem = -1;
        int idDestino = -1;

        System.out.println("\tImpressao dos parametros de cada servidor de cada CS...");
        for (Servidores servTemp : servidores) {
            System.out.printf("\tDados do servidor%d\n", servTemp.getIdServidor());
            switch (servTemp.getTipoServidor()) {
                case 0:
                    System.out.printf("\tTipo: Processamento\n");
                    //No printf abaixo, para o caso de uma unica maquina,
                    //o tServicoTotal do CS todo eh soh o tServico dessa
                    //maquina
                    System.out.printf("\tMestre (true) Escravo(false): %s\n", servTemp.getMestreEscravo());
                    System.out.printf("\ttServico%f\n", servTemp.getTServicoProcto());
                    System.out.printf("\tO Servidor esta livre? %s\n", servTemp.getServidorLivre());
                    System.out.printf("\n");
                    break;
                case 1:
                    System.out.printf("\tTipo: Comunicacao\n");
                    System.out.printf("\ttServico %f\n", servTemp.getTServicoRede());
                    System.out.printf("\tLatencia %f\n", servTemp.getLatencia());
                    System.out.printf("\tTxOcupacao %f\n", servTemp.getTxOcupacao());
                    System.out.printf("\tO Servidor esta livre? %s\n", servTemp.getServidorLivre());
                    System.out.printf("\n");
                    break;
            }
        }
    }

    public int servidorAtual() {
        return getNumAtualServidores();
    }

    public double valorServidor(int id) {
        double retorno = 0.0;
        for (Servidores servTemp : servidores) {
            if (servTemp.getIdServidor() == id) {
                retorno = servTemp.getTServicoProcto();
                break;
            }
        }
        return retorno;
    }

}// fim de public class CentroDeServico