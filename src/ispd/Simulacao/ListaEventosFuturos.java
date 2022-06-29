package ispd.Simulacao;

import java.util.PriorityQueue;
import ispd.RedesDeFilas.*;
import ispd.NumerosAleatorios.*;

public class ListaEventosFuturos
{
	private PriorityQueue< NoLEF > lef;
	private int numEventosNaLista;
	private int controleIdsEventos;
	private Double tempoAtual;


	public ListaEventosFuturos()
	{   lef = new PriorityQueue< NoLEF >();
		controleIdsEventos = 0;
		numEventosNaLista = 0;
		controleIdsEventos = 0;
		tempoAtual = 0.0;

	}

    public void adicionaEvento( int tpEvento, NoFila tarefa )
	{ /*  
	    System.out.printf( "\nnumEventos = %d\n", numEventosNaLista );
	    System.out.printf( "Adicao de evento na LEF\n" );
	    System.out.printf( "id do Evento: %d\n", controleIdsEventos );
	    System.out.printf( "Tipo do Evento: %d\n", tpEvento );
		System.out.printf( "id Tarefa: %d\n", tarefa.getIdTarefa() );
		System.out.printf( "Tempo de ocorrencia: %f\n\n", tarefa.getTempoChegadaFilaProc() );
	  */
    	NoLEF eventoAdicionado = new NoLEF( controleIdsEventos, tpEvento, tarefa );
	    lef.offer( eventoAdicionado );
		numEventosNaLista++;
		controleIdsEventos++;
	}

	public void adicionaEvento( int tpEvento, NoFila tarefa, Double tOcor )
	{  /* 
	    System.out.printf( "\nnumEventos = %d\n", numEventosNaLista );
	    System.out.printf( "Adicao de evento na LEF\n" );
	    System.out.printf( "id do Evento: %d\n", controleIdsEventos );
	    System.out.printf( "Tipo do Evento: %d\n", tpEvento );
		System.out.printf( "id Tarefa: %d\n", tarefa.getIdTarefa() );
		System.out.printf( "Tempo de ocorrencia: %f\n\n", tOcor );
	   */	
		NoLEF eventoAdicionado = new NoLEF( controleIdsEventos, tpEvento, tarefa, tOcor );
	    lef.offer( eventoAdicionado );
		numEventosNaLista++;
		controleIdsEventos++;
	}

	public void adicionaEvento( NoLEF evento )
	{   NoLEF eventoAdicionado = new NoLEF( evento.getIdEvento( ), evento.getTipoEvento(), evento.getTarefa(), evento.getTempoOcorrencia()  );
	    lef.offer( eventoAdicionado );
		numEventosNaLista++;
		controleIdsEventos++;
	}	
	public void Lista()
	{   while( lef.size() > 0 )
	    {   NoLEF temp = lef.peek();
			lef.poll();  
		}
	}
	
	public int size()
	{   return numEventosNaLista;
	}

	public NoLEF peek()
	{   NoLEF temp;
	    temp = lef.peek();
		return temp;
	}
	
	public void poll()
	{   NoLEF temp;
	    temp = lef.poll();
		/*
		System.out.printf("\nEXCLUSAO DE EVENTO DA LEF\n");
		System.out.printf("id Evento retirado = %d\n", temp.getIdEvento() );
		System.out.printf("T.O. no retirado: %f\n", temp.getTempoOcorrencia() );
		*/
		numEventosNaLista--;
		//System.out.printf( "numEventos = %d\n\n", numEventosNaLista );
	}
	
	public int getnumEventosNaLista()
	{	return numEventosNaLista;
	}

    public ListaEventosFuturos atualizaLEF( NoLEF eventoAtual, Double delta, int idTarefaAlocadaServProc )
	{   ListaEventosFuturos lefAtualizada = new ListaEventosFuturos();
	   
	    while( size() > 0 )
	    {   NoLEF temp = peek();
			if( ( temp.getTipoEvento() == eventoAtual.getTipoEvento() ) && (temp.getIdCS() == eventoAtual.getIdCS() ) && (temp.getIdServidor() == eventoAtual.getIdServidor() ) )
			{   temp.setTempoOcorrencia( temp.getTempoOcorrencia() + delta );
			}
			lefAtualizada.adicionaEvento(temp);
			poll(); 
		}
	    return lefAtualizada;
	}

}
