package ispd.RedesDeFilas;

import java.util.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import ispd.Simulacao.*;

public class Servidores
{   
    private int idServidor;
	private int tipoServidor; //0: Servidor de processamento, 1: Servidor de comunicacao 
    private boolean mestreEscravo; // true = mestre; false = escravo.
	private boolean ServidorLivre;
	private int idTarefaAtual; // identificador que est atualmente alocada no servidor 
	private Double tServicoProcto; //Poder Computacional (variavel utilizada para servidores do tipo 0)
	                               //Usada em maquinas de processamento e clusters
	private Double tServicoRede;   //Largura de banda (variavel utilizada para servidores do tipo 1)
	
	private double latencia;         // As variaveis tServicoRede, tipoDistribuicaoCom, latencia
	private double txOcupacao;       // e txOcupacao sao utilizadas em servidores "comuns" de comunicacao
	                                 // clusters e Internet
		
	private Double tempoAtual;  //Essa variavel armazena o tempo de execucao de uma tarefa no servidor, ou seja,
	                           //eh a soma do tempo (instante) que ela chega ao servidor com o tempo que o servidor
							  // gasta para executa-la.
							  // Observacao: nas outras classes, serve para dizer qual o proximo servidor a executar uma tarefa
	private Double tamanhoProcTarefaAtual;
	private Double instanteOcupacao;
	private Double tempoPrevistoLiberacao;
	
	
//+------------------------------------------Inicio dos metodos da classe------------------------------------------+	

//+-------------------------------------------------Metodos construtores----------------------------------------------+	
	//Adiciona um servidor de processamento unico. (Para CSs do tipo "maquina")
	public Servidores( int id, int tpServ, boolean msOuEsc, Double tServProcto )
	{   setIdServidor( id );
	    setTipoServidor( tpServ );
		setMestreEscravo( msOuEsc );
		liberaServidor( );
		setTServicoProcto( tServProcto );
		
	}
	//Adiciona um servidor de comunicacao unico. (Para CSs dos tipos "conexao de rede comum" e "cluster")
	public Servidores( int id, int tpServ, Double tServRede, double ltnc, double txOcup )
	{   setIdServidor( id );
	    setTipoServidor( tpServ );
		liberaServidor( );
		setTServicoRede( tServRede );
		setLatencia( ltnc );
		setTxOcupacao( txOcup );
		
	}

    public Double atribuiTarefaMestre( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
	    /*    A ocupacao de um servidor do tipo "mestre" e feita exclusivamente para escalonamento e
		   no momento, considera-se que o escalonamento eh feito instantamente, por isso o 
		   incremento que ele causa no tempo da simulacao e 0.0 (que e o retorno da funcao ).
		   OBSERVACAO: Essa funcao nao faz o escalonamento propriamente dito, apenas ocupa um servidor
		   com uma tarefa, o que o ocupa com uma determinada tarefa com uma tarefa
		*/
		System.out.printf("tarefa.getIdTarefa()= %d\n", getIdTarefaAtual() );
		setIdTarefaAtual( tarefa.getIdTarefa() );
		System.out.printf("ALOCACAO DA TAREFA %d NO SERVIDOR %d\n",
		getIdTarefaAtual(), getIdServidor() );
		ocupaServidor(); //Bloqueio do servidor, ou seja, ele eh colocado no estado "ocupado"
		
		return retornoFuncao = 0.0; 
	}	

    public Double atribuiTarefaServComun( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
		
		//System.out.printf("tarefa.getIdTarefa()= %d\n", getIdTarefaAtual() );
		setIdTarefaAtual( tarefa.getIdTarefa() );
		System.out.printf("ALOCACAO DA TAREFA %d NO SERVIDOR de Comunicacao %d\n",
		getIdTarefaAtual(), getIdServidor() );
		ocupaServidor(); //Bloqueio do servidor, ou seja, ele eh colocado no estado "ocupado"
		
		return retornoFuncao = 0.0; 
	}	

    public Double atribuiTarefaServProc( NoFila tarefa, Double instanteOcupacaoServidor )
	{   Double retornoFuncao = 0.0;
		
	//	System.out.printf("tarefa.getIdTarefa()= %d\n", getIdTarefaAtual() );
		setIdTarefaAtual( tarefa.getIdTarefa() );
		setInstanteOcupacao( instanteOcupacaoServidor );
//		System.out.printf("ALOCACAO DA TAREFA %d NO SERVIDOR de PROCESSAMENTO %d\n",
//		getIdTarefaAtual(), getIdServidor() );
		ocupaServidor(); //Bloqueio do servidor, ou seja, ele eh colocado no estado "ocupado"
		
		return retornoFuncao = 0.0; 
	}	

	public Double atribuiTarefaServProc( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
		
	//	System.out.printf("tarefa.getIdTarefa()= %d\n", getIdTarefaAtual() );
		setIdTarefaAtual( tarefa.getIdTarefa() );
//		System.out.printf("ALOCACAO DA TAREFA %d NO SERVIDOR de PROCESSAMENTO %d\n",
//		getIdTarefaAtual(), getIdServidor() );
		ocupaServidor(); //Bloqueio do servidor, ou seja, ele eh colocado no estado "ocupado"
		
		return retornoFuncao = 0.0; 
	}	
	
//+--------------------------Metodo que "seta" o identificador do servidor-----------------------------------------+
	public void setIdServidor( int id )
	{   idServidor = ( id >= 0 ) ? id : 0; 
	}
//+-------------------------- Metodo que retorna o identificador do servidor---------------------------------------+
	public int getIdServidor( )
	{   return idServidor; 
	}
//+-----------------Metodo que "seta" o tipo do servidor (processamento ou comunicacao)---------------+
	public void setTipoServidor( int tpServ )
	{   tipoServidor = ( tpServ >= 0 && tpServ <= 1 ) ? tpServ : 0; 
	}
//+----------------Metodo que retorna o tipo do servidor (processamento ou comunicacao)---------------+
	public int getTipoServidor( )
	{   return tipoServidor; 
	}
//+-----------------Metodo que "seta" o tipo do servidor (processamento ou comunicacao)---------------+
	public void setMestreEscravo( boolean msOuEsc )
	{   // true = mestre; false = escravo.
	    mestreEscravo = msOuEsc; 
	}
//+----------------Metodo que retorna o tipo do servidor (processamento ou comunicacao)---------------+
	public boolean getMestreEscravo( )
	{   // true = mestre; false = escravo.
	    return mestreEscravo; 
	}

//+-------------------------- Metodo que torna o estado do servidor "ocupado"--------------------------------------+
	public void ocupaServidor( )
	{   ServidorLivre = false; 
	}
//+-------------------------- Metodo que torna o estado do servidor "livre"----------------------------------------+
	public void liberaServidor( )
	{   ServidorLivre = true; 
	}
//+-------------------------- Metodo que retorna o estado atual do servidor----------------------------------------+
	public boolean getServidorLivre()
	{   return ServidorLivre;
	}
//+--------------------------Metodo que "seta" o tempo de servico total do servidor-------------------+	
    public void setTServicoProcto( Double tServProcto )
	{   tServicoProcto = ( tServProcto >= 0.0 ) ? tServProcto : 0.0; 
	}

//+---------------------------Metodo que retorna o tempo de servico do servidor------------------------------------------+	
    public Double getTServicoProcto()
	{   return tServicoProcto;
	}
//+--------------------------Metodo que "seta" o tempo de servico de rede do servidor-------------------+	
    public void setTServicoRede( Double tServRede )
	{   tServicoRede = ( tServRede >= 0.0 ) ? tServRede: 0.0; 
	}

//+---------------------------Metodo que retorna o tempo de servico do servidor------------------------------------------+	
    public Double getTServicoRede()
	{   return tServicoRede;
	}

	public void setLatencia( double ltnc )
	{   latencia = ( ltnc >= 0.0 ) ? ltnc : 0.0; 
	}

	public double getLatencia( )
	{   return latencia; 
	}

	public void setTxOcupacao( double txOcup )
	{   txOcupacao = ( txOcup >= 0.0 && txOcup <= 1.0 ) ? txOcup : 0.0; 
	}

	public double getTxOcupacao( )
	{   return txOcupacao; 
	}		

	public Double getTempoAtual()
	{   return tempoAtual;  //Ver definicao/comentario sobre esta variavel na declaracao de atributos da classe Servidor
	}

//-1 e flag pra marcar que nao ha tarefa no servidor, ou seja, que ele esta vazio.
	public void setIdTarefaAtual( int id )
	{   idTarefaAtual = ( id >= -1 ) ? id : 0; 
	}

	public int getIdTarefaAtual( )
	{   return idTarefaAtual; 
	}
	
	public void setTamanhoProcTarefaAtual( NoFila tarefa )
	{   tamanhoProcTarefaAtual = tarefa.getTamanhoProcTarefa();
	}

	public Double getTamanhoProcTarefaAtual()
	{   return tamanhoProcTarefaAtual;
	}
	
	public void setInstanteOcupacao( Double iOS )
	{   instanteOcupacao = iOS;
	}

	public Double getInstanteOcupacao( )
	{   return instanteOcupacao;
	}
	
	public void setTempoPrevistoLiberacao( Double tPL )
	{   tempoPrevistoLiberacao = tPL;
	}
	
	public Double getTempoPrevistoLiberacao( )
	{   return tempoPrevistoLiberacao;
    }
	
} // fim de public class Servidores