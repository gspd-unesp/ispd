package ispd.RedesDeFilas;

import java.util.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import ispd.Simulacao.*;

public class NoFila implements Comparable <NoFila>
{   
	private int idTarefa;
	private int idCSAtual;                                
	private int idCSDestino;
	private int idFila;                             
	private int idServidorAtual;
	private int idServidorDestino;
    private int distanciaProximoMestre;
	private int ttl;
	private int[] vetorCaminhoTarefa;  //caminho que a tarefa tera que, eventualmente, percorrer devido ao escalonamento/roteamento
	private Double tamanhoProcTarefa;
	private Double tempoChegadaFila;
	private Double tamanhoComTarefa;
	private Double tempoChegadaFilaCom;
    private boolean finalizada; //true = esta finalizada; false = nao esta finalizada

	public NoFila( int idTarefa, int idCS, int idFila, int idServ, Double tamProc, Double tempoCFProc, Double tamCom, Double tempoCFCom )
	{	setIdTarefa( idTarefa );
		setIdCSAtual( idCS ); 
    	setIdCSDestino( -1 ); //Na criacao da tarefa, o CS atual tambem e o CS de destino
		setIdFila( idFila );
		setIdServidorAtual( idServ );
		setIdServidorDestino( -1 );  //Na criacao da tarefa, o servidor atual tambem e o servidor de destino
		setDistanciaProximoMestre( 0 );
		setTtl( 0 );
		vetorCaminhoTarefa = null;
		finalizada = false;
		this.tamanhoProcTarefa = tamProc;    
		this.tempoChegadaFila = tempoCFProc;
		this.tamanhoComTarefa = tamCom;
		this.tempoChegadaFilaCom = tempoCFCom;

	}

	public void setIdTarefa( int id )
	{   idTarefa = ( id >= 0 ) ? id : 0; 
	}

	public int getIdTarefa( )
	{   return idTarefa; 
	}
	
	public void inicializaTarefa( )
	{   setIdCSDestino( -1 );
	    setIdServidorDestino( -1 ); 
		setDistanciaProximoMestre( 0 );
		setTtl( 0 );
		vetorCaminhoTarefa = null;
		finalizada = false;
	}

	public void setIdCSAtual( int iCS )
	{   idCSAtual = ( iCS >= -1 ) ? iCS : 0; 
	}	

	public int getIdCSAtual()
	{   return idCSAtual;
	}

	//-1 e flag pra marcar que o servidor esta vazio
	public void setIdServidorAtual( int idServ )
	{   idServidorAtual = ( idServ >= -1 ) ? idServ : 0; 
	}	

	public int getIdServidorAtual()
	{   return idServidorAtual;
	}

	public void setIdCSDestino( int iCS )
	{   idCSDestino = ( iCS >= -1 ) ? iCS : 0; 
	}	

	public int getIdCSDestino()
	{   return idCSDestino;
	}

	public void setIdServidorDestino( int idServ )
	{   idServidorDestino = ( idServ >= -1 ) ? idServ : 0; 
	}	

	public int getIdServidorDestino()
	{   return idServidorDestino;
	}

	public void setDistanciaProximoMestre( int dPM )
	{   distanciaProximoMestre = ( dPM >= 0 ) ? dPM : 0; 
	}	
	
	public int getDistanciaProximoMestre()
	{   return distanciaProximoMestre;
	}	

	public int getTamanhoVetorCaminhoTarefa()
	{   return vetorCaminhoTarefa.length;
	}

	public void setVetorCaminhoTarefa( int vCaminho[] )
	{   vetorCaminhoTarefa = new int [vCaminho.length];
    	vetorCaminhoTarefa = vCaminho; 
	}	

	public int[] getVetorCaminhoTarefa()
	{   return vetorCaminhoTarefa;
	}
	
	public int getElementoVetorCaminhoTarefa( int i )
	{   if( vetorCaminhoTarefa == null )
		{   return i;
		}
		if( i < vetorCaminhoTarefa.length )
	       return vetorCaminhoTarefa[i];
		
        return 0;		
	}
	public void setFinalizada()
	{   finalizada = true;
	}
	
	public boolean getFinalizada()
	{   return finalizada;
	}

	public void setTtl( int ttllocal )
	{   ttl = ( ttllocal >= 1 ) ? ttllocal : 1; 
	}	

	public void decrementaTtl( )
	{   if( ttl > 0 )
            ttl--;
        else
            ttl = 0;		
	}

	public void crementaTtl( )
	{   ttl = ttl + 1;		
	}	

	public int getTtl()
	{   return ttl;
	}

	public void setIdFila( int iFila )
	{   idFila = ( iFila >= -1 ) ? iFila : 0; 
	}
	
    public int getIdFila()
    {   return idFila;
	}
    
	public void imprimeDadosTarefa()
    {   int i;
	    System.out.printf( "Dados de uma tarefa\n");
	    System.out.printf( "idTarefa = %d\n", getIdTarefa() );
		System.out.printf( "tamanhoProcTarefa = %f\n", getTamanhoProcTarefa() );
		System.out.printf( "tempoChegadaFila = %f\n", getTempoChegadaFila() );
		System.out.printf( "tamanhoComTarefa = %f\n", getTamanhoComTarefa() );
		System.out.printf( "tempoChegadaFilaCom = %f\n", getTempoChegadaFilaCom() );
		System.out.printf( "idCSAtual = %d\n", getIdCSAtual() );
		System.out.printf( "idCSDestino = %d\n", getIdCSDestino() );
		System.out.printf( "idFila = %d\n", getIdFila() );
		System.out.printf( "idServidorAtual = %d\n", getIdServidorAtual() );
		System.out.printf( "idServidorDestino = %d\n", getIdServidorDestino() );
		System.out.printf( "distanciaProximoMestre = %d\n", getDistanciaProximoMestre() );
		System.out.printf( "ttl = %d\n", getTtl() );
		System.out.printf( "Caminho da Tarefa:\n" );
		if( vetorCaminhoTarefa != null )
		{   for( i = 0  ;  i < vetorCaminhoTarefa.length  ;  i++ )
		      System.out.printf( "%d ", vetorCaminhoTarefa[i] );   
		    
        }
		System.out.printf( "\n" );
		if( getFinalizada() == true )
	       System.out.printf( "finalizada = true\n" );
        else
           System.out.printf( "finalizada = false\n" ); 		
		System.out.printf( "\n" );
    } 	

	public Double getTamanhoProcTarefa( )
	{   return tamanhoProcTarefa; 
	}

	public Double getTamanhoComTarefa( )
	{   return tamanhoComTarefa; 
	}

	public Double getTempoChegadaFila()
	{   return tempoChegadaFila; 
	}

	public Double getTempoChegadaFilaCom()
	{   return tempoChegadaFilaCom; 
	}
//+-------------------------- Metodo que sobrescreve o compareTo (da classe Double)--------------------------------+	
	/*Eh por esse metodo que a PriorityQueue inicioFila (da classe Fila) sabe que a prioridade tem que ser dada pelo
	tempo de chegada (aux.tempoChegadaFila) da tarefa/no */
	public int compareTo( NoFila aux )
	{   return tempoChegadaFila.compareTo( aux.tempoChegadaFila );
	}
	
} // fim de public class NoFila