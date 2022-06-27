package ispd.Estatistica;

import java.util.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;
import ispd.Simulacao.*;

public class TarefasFila
{   
	private int tipoMetrica; // 0 - Comunicacao      1 - Processamento
	private int numeroTarefasTotal;
	private int numeroTarefasConcluidas;
	private int numeroTarefasFila;
	private Double tempoMedioFila;
	private Double tempoMedioServidor;
	private Double tempoMedioSistema;
	private Double cargaTotal;
	private List<NoTarefasFila> fila;


	public TarefasFila( int tipo)
	{	fila = new ArrayList<NoTarefasFila>();
		setTipoMetrica( tipo );
		numeroTarefasTotal = 0;
		numeroTarefasConcluidas = 1;
		numeroTarefasFila = 0;
		setNumeroTarefasTotal( 0 ); 
		tempoMedioFila = 0.0;
		tempoMedioServidor = 0.0;
		tempoMedioSistema = 0.0;
	}

	public void addNoTarefasFila( int idTarefa, Double tam, Double tempo ){
		Double aux = 0.0;
		int controle = 0;
		for(NoTarefasFila temp:fila){
			if(temp.getIdTarefa() == idTarefa){
				numeroTarefasConcluidas++;
				temp.setTempoChegadaFila( tempo );
				controle = 1;
				break;
			}
		}
		if( controle == 0 )
		{	NoTarefasFila no = new NoTarefasFila(idTarefa, tam, tempo);
			numeroTarefasTotal++;
			numeroTarefasFila++;
			fila.add(no);
		}
	}

	public void imprimeTarefas(){
		System.out.printf("\nFila DO TIPO: %d\n",getTipoMetrica());
		for(NoTarefasFila temp:fila){
			System.out.printf("Tarefa ID: %d --> Tamanho %g --> TempoMedioFila --> %f TempoMedioServidor --> %f TempoMedioSistema --> %f\n", temp.getIdTarefa(), temp.getTamanhoTarefa(), getTempoMedioFila(), getTempoMedioServidor(), getTempoMedioSistema());
		}
	}
		
	public void setTempoChegadaServidor(int idTarefa, Double tempo)
	{	Double aux = 0.0;
		for(NoTarefasFila no:fila)
		{	if( no.getIdTarefa() == idTarefa )
			{	if( no.getTempoChegadaServidor() == 0.0 )
				{	no.setTempoChegadaServidor( tempo );
					setTempoMedioFila( idTarefa );
				}	
				else
				{	no.setTempoChegadaServidor( tempo );
				}
				break;
			}
		}
	}
	
	public void setTempoSaidaSistema(int idTarefa, Double tempo)
	{	Double aux = 0.0;
		for(NoTarefasFila no:fila)
		{	if( no.getIdTarefa() == idTarefa )
			{	if( no.getTempoSaidaSistema() == 0.0 )
				{	no.setTempoSaidaSistema( tempo );
					setTempoMedioServidor( idTarefa );
					setTempoMedioSistema( idTarefa );
				}
				else
				{	//setTempoMedioSistema( idTarefa );
					//no.setTempoSaidaSistema( tempo );
					System.out.printf("ENTRA NO ELSE DA SAIDA DO SISTEMA A TAREFA %d DO TIPO DE FILA %d !\n", idTarefa, getTipoMetrica());
				}
				break;
			}
		}
	}

	public void setTipoMetrica( int tipo )
	{   tipoMetrica = ( tipo >= 0 ) ? tipo : -1; 
	}

	public int getTipoMetrica( )
	{   return tipoMetrica; 
	}

	public void setNumeroTarefasTotal( int tarefasTotal )
	{   numeroTarefasTotal = ( tarefasTotal >= 0 ) ? tarefasTotal : 0; 
	}

	public int getNumeroTarefasTotal()
	{   return numeroTarefasTotal;
	}

	public void setNumeroTarefasConcluidas( int tarefasConcluidas )
	{   numeroTarefasConcluidas = ( tarefasConcluidas >= 0 ) ? tarefasConcluidas : 0; 
	}

	public int getNumeroTarefasConcluidas()
	{   return numeroTarefasConcluidas;
	}

	public void setNumeroTarefasFila( int tarefasFila )
	{   numeroTarefasFila = ( tarefasFila >= 0 ) ? tarefasFila : 0; 
	}

	public int getNumeroTarefasFila()
	{   return numeroTarefasFila;
	}

	public void setTempoMedioFila( int id )
	{	Double aux = 0.0;
		aux = getTempoMedioFila();
		for(NoTarefasFila no:fila)
		{	if( no.getIdTarefa() == id )
			{	tempoMedioFila = tempoMedioFila + ( no.getTempoChegadaServidor() - no.getTempoChegadaFila() );
			break;
			}
		}
		tempoMedioFila = tempoMedioFila / numeroTarefasConcluidas;
	}

	public Double getTempoMedioFila()
	{   return tempoMedioFila;
	}

	public void setTempoMedioServidor( int id )
	{	Double aux = 0.0;
		aux = getTempoMedioServidor();
		for(NoTarefasFila no:fila)
		{	if( no.getIdTarefa() == id )
			{	tempoMedioServidor = tempoMedioServidor + ( no.getTempoSaidaSistema() - no.getTempoChegadaServidor() );
				break;
			}
		}
		tempoMedioServidor = tempoMedioServidor / numeroTarefasConcluidas;
	}

	public Double getTempoMedioServidor()
	{   return tempoMedioServidor;
	}

	public void setTempoMedioSistema( int id )
	{	Double aux = 0.0;
		for(NoTarefasFila no:fila)
		{	if( no.getIdTarefa() == id )
			{	tempoMedioSistema = tempoMedioSistema + ( no.getTempoSaidaSistema() - no.getTempoChegadaFila() );
				break;
			}
		}
		tempoMedioSistema = tempoMedioSistema / numeroTarefasConcluidas;
	}

	public Double getTempoMedioSistema()
	{   return tempoMedioSistema;
	}

	public void setCargaTotal()
	{   for(NoTarefasFila no:fila)
		{	cargaTotal = cargaTotal + no.getTamanhoTarefa();
		}
	}

	public Double getCargaTotal()
	{   return cargaTotal;
	}

	public double mediaCargasNormalizadas()
	{	double aux = 0.0;
		double media = 0.0;
		for(NoTarefasFila no:fila) // Encontra maior tarefa da fila
		{	if( no.getTamanhoTarefa() > aux ) 
			{	aux = no.getTamanhoTarefa();
			}
		}
		for(NoTarefasFila no:fila) //Normaliza as tarefas
		{	media = media + ( no.getTamanhoTarefa() / aux ) * 100;
		}
		media = media / numeroTarefasTotal;
		return media;
	}
	
	public void imprimeTar(){
		for(NoTarefasFila no:fila){
			System.out.printf("TAREFA ID %d    TEMP CHEGADA %f   TEMP CHEGADA SERV %f   TEMP SAIDA %f   \n", no.getIdTarefa(), no.getTempoChegadaFila(), no.getTempoChegadaServidor(), no.getTempoSaidaSistema());
		}
	}
	
	public int contaTarefa(){
		int retorno = 0;
		for(NoTarefasFila no:fila){
			retorno++;
		}
		return retorno;
	}
	
} // fim de public class Fila