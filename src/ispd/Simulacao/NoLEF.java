package ispd.Simulacao;
import ispd.RedesDeFilas.*;

public class NoLEF implements Comparable<NoLEF>
{   
    private int idEvento;
    private int tipoEvento; 
	/*  Tipos de Eventos (para a variavel acima):
	    0: Chegada de uma tarefa em uma fila; 
		1: Entrada de uma tarefa em um mestre;
		2: Escalonamento de tarefa;
		3: Saida de uma tarefa de um mestre em direcao a uma fila do sistema;
		4: Entrada de uma tarefa em um no escravo (seja de comunicacao, seja de processamento);
		5: Envio (comunicacao) de tarefa;
		6: Saida de uma tarefa de um servidor em direcao a um servidor;
		7: Processamento 'computacional' de tarefa;
		8: Impressao dos resultados finais e finalizacao do sistema;
		
	*/
	private Double tempoOcorrencia;
	private int idCS;
	private int idFila;
	private int idServidor;
	private int idTarefa;
	private NoFila tarefa;

	public NoLEF( int idEvento, int tpEvento, Double tOcor, int idCS, int idRecurso, int idTarefa )
	{   setIdEvento( idEvento );
	    setTipoEvento( tpEvento );
		setTempoOcorrencia( tOcor );
		setIdCS( idCS );
		if( tpEvento == 0 || tpEvento == 5 )  //Se o tipo do evento envolver uma fila
		   setIdFila( idRecurso );
		else  //Se nao, envolve um servidor
		   setIdServidor( idRecurso );
		setIdTarefa( idTarefa );
	}
	public NoLEF( int idEvento, int tpEvento, NoFila tar )
	{   setIdEvento( idEvento );
	    setTipoEvento( tpEvento );
		setTarefa( tar );
	    setTempoOcorrencia( tar.getTempoChegadaFila() );
	}	
	public NoLEF( int idEvento, int tpEvento, NoFila tar, Double tOcor )
	{   setIdEvento( idEvento );
	    setTipoEvento( tpEvento );
		setTarefa( tar );
	    setTempoOcorrencia( tOcor );
	}	

    public void setIdEvento( int idEv )
	{   idEvento = ( idEv >= 0 ) ? idEv : 0; 
	}

	public int getIdEvento( )
	{   return idEvento; 
	}
	
    public void setTipoEvento( int tpEv )
	{   tipoEvento = ( tpEv >= 0 ) ? tpEv : 0; 
	}

	public int getTipoEvento( )
	{   return tipoEvento; 
	}
	
    public void setTempoOcorrencia( Double tOcor )
	{   tempoOcorrencia = ( tOcor >= 0.0 ) ? tOcor : 0.0; 
	}

	public Double getTempoOcorrencia( )
	{   return tempoOcorrencia; 
	}
	
    public void setIdCS( int iCS )
	{   idCS = ( iCS >= 0 ) ? iCS : 0; 
	}
	
	public int getIdCS( )
	{   return idCS; 
	}

	public void setIdFila( int iFila )
	{   idFila = ( iFila >= 0 ) ? iFila : 0; 
	}	

	public int getIdFila()
	{   return idFila;
	}
	
    public void setIdServidor( int idServ )
	{   idServidor = ( idServ >= 0 ) ? idServ : 0; 
	}	

	public int getIdServidor()
	{   return idServidor;
	}	
	
    public void setIdTarefa( int idTar )
	{   idTarefa = ( idTar >= 0 ) ? idTar : 0; 
	}

	public int getIdTarefa()
	{   return idTarefa;
	}

    public void setTarefa( NoFila tar )
	{  this.tarefa = tar;
	}	

	public NoFila getTarefa()
	{   return tarefa;
	}	
	
	/*Eh por esse metodo que a PriorityQueue lef (da classe ListaEventosFuturos) sabe que a prioridade tem que ser
	  dada pelo tempo de ocorrencia (aux.tempoOcorrencia) da tarefa/no */	
	public int compareTo( NoLEF aux )
	{   return tempoOcorrencia.compareTo( aux.tempoOcorrencia );
	}	
}