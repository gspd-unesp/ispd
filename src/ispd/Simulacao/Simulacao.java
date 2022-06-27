package ispd.Simulacao;

import java.util.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.LinkedList;
import javax.swing.*;
import java.awt.*;
import ispd.RedesDeFilas.*;
import ispd.NumerosAleatorios.*;
import java.io.*;
import ispd.Estatistica.*;
import ispd.Interface.*;

public class Simulacao
{
    
	private PriorityQueue< NoFila > filaInicial;
	private int numTarefasCriadas;
	private int numServidoresOcupados;
	private int numEscalonadores;
	private int escalonadores[];
	private int indiceAtualEscalonadorReceptor;
	private int numTotalCSs;
	private int qtdadeTarefasCadaCSEscalonador[];
	private RedesDeFilas redeDeFilas;
	private ListaEventosFuturos listaEF;
	private GeracaoNumAleatorios gerador;
	private Relogio relogioSimulacao;
	private List<String> listaNomeId;
	private String char253 = new Character((char)253).toString();
	private String char252 = new Character((char)252).toString();
	private String char254 = new Character((char)254).toString();	
	private boolean precisaAtualizarLEF = false;
	private Estatistica estatistica;

	public Simulacao(RedesDeFilas fila, String tarefa,List<String> listaNomeId)
	{	gerador = new GeracaoNumAleatorios(199);
		filaInicial = new PriorityQueue< NoFila >();
		listaEF = new ListaEventosFuturos();
		redeDeFilas = fila;
		relogioSimulacao = new Relogio( 0.0 );
		this.listaNomeId = listaNomeId;
		
		//MARCO
		estatistica = redeDeFilas.getEstatistica();
		estatistica.setTempoInicial( relogioSimulacao.getRelogioSimulacao() );
		estatistica.addTarefasFila(0);
		estatistica.addTarefasFila(1);
		estatistica.setNomeId( this.listaNomeId );
		
		numServidoresOcupados = 0;
		setNumTarefasCriadas( 0 );
		setNumTotalCSs(redeDeFilas.getNumCs());
		List<Integer> listaEscalonadores = redeDeFilas.getListaEscalonadores();
		setNumEscalonadores(listaEscalonadores.size());
		int i=0;
		for(Integer temp:listaEscalonadores){
			escalonadores[i] = temp;
			i++;
		}
		redeDeFilas.confereRF();
	}
	
	public void montagemInfraEstrutura()
	{
	    /*
		    Apos a integracao da GUI com o motor (atraves do modelo simulavel), a montagem da infraestrutura
			a ser simulada � feita no arquivo Interpretador.jj do pacote ModeloSimulavel.
			ver: Repositorio\branches\plataforma\src\InterpretadorInterno\ModeloSimulavel\Interpretador.jj
		*/
	}
	
	public void criacaoInicialTarefas( String tarefas,AguardaSimulacao contador )
	{ 	String vetor[] = tarefas.split(char252);
		int tipo_entrada = Integer.parseInt(vetor[0]);
		switch( tipo_entrada )
        {   //Aleatorio
		    case 0:   
					  criacaoAleatoriaTarefas(vetor[1], Integer.parseInt(vetor[2]), contador);
		              break;
		    //Usuario
			case 1:   criacaoTarefasUsuario(vetor[1],contador);
			          //criacaoTarefasUsuario(20);
					  break;		
        }
	}
	
	public void criacaoAleatoriaTarefas(String tarefas, int num_tarefas,AguardaSimulacao contador)
	{   Double tamProc = 0.0;
     	Double tempoCFProc = 0.0;
		Double tamCom = 0.0;
		Double tempoCFCom = 0.0;
		int idCSReceptorAtual = 0;
		int idInicialTarefasEscalonadorAtual = 0;
		int idInicialTarefasProximoEscalonador = 0;  //IdFinalTarefasEscalonadorAtual = IdInicialTarefasProximoEscalonador - 1. 
		                           //Apenas optei pela outra nomenclatura (e fica mais facil de "ver" no "for")
		int qtdadeTarefasCadaCSEscalonador [] = new int[getNumTotalCSs()];
		String vetor[] = tarefas.split(char253);
		for( int i = 0 ; i < getNumTotalCSs() ; i++ )
		{   qtdadeTarefasCadaCSEscalonador[i] = 0;
		}
		
        for( int i = 0 ; i < getNumEscalonadores() ; i++ )
		{   /* Se a quantidade de tarefas ( num_tarefas / getNumEscalonadores() ) que cada escalonador 
		    receber nao for inteira, os n-1 escalonadores recebem quantidades inteiras ("then do if")
            e o n-esimo escalonador ("else do if") receber a quantidade que os outros escalonadores
            receberam MAIS o resto da divisao. Por exemplo, 100 tarefas para dois escalonadores, cada
            um recebe 50 tarefas. 100 tarefas para 3 escalonares, o primeiro recebe 33 tarefas,
            o segundo outras 33 e o terceiro recebe 34 ( 33 + 1(resto) ) tarefas.			
		    */
		    if( i != ( getNumEscalonadores() - 1 ) )
		    {   qtdadeTarefasCadaCSEscalonador[ escalonadores[i] ] = num_tarefas / getNumEscalonadores(); 
		    }
		    else
		    {  qtdadeTarefasCadaCSEscalonador[ escalonadores[i] ] = ( ( num_tarefas / getNumEscalonadores() ) + ( num_tarefas % getNumEscalonadores() ) );
		    }
		}	
	    
/*		System.out.printf("qtdadeTarefasCadaCSEscalonador\n");
		for( int i = 0 ; i < getNumTotalCSs() ; i++ ) 
		{   System.out.printf("%d ", qtdadeTarefasCadaCSEscalonador[ i ] );
		}
		System.out.printf("\n");
*/
		System.out.printf("\tCriacao Aleatoria de tarefas\n");
		
		int tamanho = num_tarefas;
		Double razao = 20.0/(double)tamanho;
		//contador.setPorcentagem(razao);
		for( int j = 0; j < getNumEscalonadores() ; j++ )
		{   System.out.printf(" j - "+j+"\n");
		    idCSReceptorAtual = escalonadores[j];
			
			if( j == 0 )
				idInicialTarefasEscalonadorAtual = 0;
			else
				idInicialTarefasEscalonadorAtual = idInicialTarefasProximoEscalonador;
			
			idInicialTarefasProximoEscalonador = idInicialTarefasEscalonadorAtual + qtdadeTarefasCadaCSEscalonador[ escalonadores[j] ];
			
			Double p0 = Double.parseDouble(vetor[0]);
			Double p1 = Double.parseDouble(vetor[1]);
			Double p2 = Double.parseDouble(vetor[2]);
			Double p3 = Double.parseDouble(vetor[3]);
			Double p4 = Double.parseDouble(vetor[4]);
			Double p5 = Double.parseDouble(vetor[5]);
			Double p6 = Double.parseDouble(vetor[6]);
			Double p7 = Double.parseDouble(vetor[7]);
			Double p8 = Double.parseDouble(vetor[8]);
			Double p9 = Double.parseDouble(vetor[9]);
			Double p10 = Double.parseDouble(vetor[10]);
			
			for( int i = idInicialTarefasEscalonadorAtual ; i < idInicialTarefasProximoEscalonador ; i++ )
			{	System.out.printf(" i - "+i+"\n");
				tamProc = (double) gerador.twoStageUniform( p0, p1, p2, p3 );
				tempoCFProc = (double) gerador.exponencial(p9);
				tamCom = (double) gerador.twoStageUniform( p4, p5, p6,p7 );
				tempoCFCom = (double) gerador.exponencial(p9);
				 
					/*Na criacao aleatoria de tarefas, todas as tarefas sao inicialmente colocadas na fila
					0 (terceiro parametro do metodo abaixo) do CS 0 (segundo parametro do metodo abaixo)
					*/
				constroiFilaInicialTarefas( i, idCSReceptorAtual, 0, 0, tamProc , tempoCFProc, tamCom, tempoCFCom );
				contador.setPorcentagem(razao);
			}	
		}	
	}
	
	public void criacaoTarefasUsuario(String tarefas,AguardaSimulacao contador)
	{   Double tamProc = 0.0;
     	Double tempoCFProc = 0.0;
		Double tamCom = 0.0;
		Double tempoCFCom = 0.0;
		int idCSReceptorAtual = 0;
		System.out.printf("\tCriacao Tarefas pelo Usuario\n");

		System.out.printf("tarefas"+tarefas+"\n");
		String vetor[] = tarefas.split(char254);
		System.out.printf("vetor"+vetor[0]+"\n");
		int tamanhoVetor = vetor.length;
		System.out.printf("tamanhoVetor"+tamanhoVetor+"\n");

		int tamanho = 0;
		for( int j = 0; j < tamanhoVetor ; j++ )
		{	String vetor2[] = vetor[j].split(char253);
			int numeroTarefas = Integer.parseInt(vetor2[1]);
			tamanho += numeroTarefas;
		}
		Double razao = 20.0/(double)tamanho;
		//contador.setPorcentagem(razao);
		
		for( int j = 0; j < tamanhoVetor ; j++ )
		{	System.out.printf("vetor[j]"+vetor[j]+"\n");
			String vetor2[] = vetor[j].split(char253);
			System.out.printf("vetor2"+vetor2[0]+"\n");
			int idCentroServico = Integer.parseInt(vetor2[0]);
			int numeroTarefas = Integer.parseInt(vetor2[1]);
			Double minComp = Double.parseDouble(vetor2[2]);
			Double maxComp = Double.parseDouble(vetor2[3]);
			Double mediaComp = Double.parseDouble(vetor2[4]);
			Double minComc = Double.parseDouble(vetor2[5]);
			Double maxComc = Double.parseDouble(vetor2[6]);
			Double mediaComc = Double.parseDouble(vetor2[7]);

		    idCSReceptorAtual = idCentroServico;
        	for( int i = 0; i < numeroTarefas ; i++ )
		    {   
				tamProc = (double) gerador.twoStageUniform( minComp, mediaComp, maxComp, 1 );
				tempoCFProc = (double) gerador.exponencial(mediaComp);
				tamCom = (double) gerador.twoStageUniform( minComc, mediaComc, maxComc, 1);
				tempoCFCom = (double) gerador.exponencial(mediaComp);
				/*Na criacao aleatoria de tarefas, todas as tarefas sao inicialmente colocadas na fila
				0 (terceiro parametro do metodo abaixo) do CS 0 (segundo parametro do metodo abaixo)
				*/			
				constroiFilaInicialTarefas( i, idCSReceptorAtual, 0, 0, tamProc , tempoCFProc, tamCom, tempoCFCom );
				contador.setPorcentagem(razao);
			}	
		}
	}
	
	/* Pela logica, constroiFilaInicialTarefas( Double tamanho, Double tempo ) deveria receber tambem o id da tarefa a ser criada, mas a variavel que representa
esse id (numTarefasCriadas) e variavel de instancia, ou seja, e controlada e pode ser manipulada pela
propria classe, ou seja, nao precisa ser passada como parametro	
	*/
	public void constroiFilaInicialTarefas( int id, int idCS, int idFila, int idServ, Double tamProc, Double tempoCFProc, Double tamCom, Double tempoCFCom )
	{   NoFila tarefa = new NoFila( numTarefasCriadas, idCS, idFila, idServ, tamProc, tempoCFProc, tamCom, tempoCFCom );
        
		tarefa.imprimeDadosTarefa();
    	
		filaInicial.offer(tarefa);
		    
		alteraNumAtualTarefasCriadas( 1 );
		
	}

    public Double iniciaSimulacao(AguardaSimulacao contador)
 	{   System.out.printf("AGENDAMENTO DE EVENTO - Adicao de tarefa numa fila" );
		NoFila primeiraTarefa = filaInicial.peek();
		filaInicial.poll();
		listaEF.adicionaEvento( 0, primeiraTarefa, primeiraTarefa.getTempoChegadaFila() );


		int tamanho = filaInicial.size();
		Double razao = 30.0/(double)tamanho;

		while ( listaEF.size() > 0 ) 
		{   while( filaInicial.size() > 0 )
		    {   NoFila tarefa = filaInicial.peek();
		        NoLEF eventoAtual = listaEF.peek();
			    
		        if( eventoAtual.getTipoEvento() == 0 )   //Se o evento atual eh uma insercao e outra 
				{   System.out.printf("1 if: AGENDAMENTO DE EVENTO - Adicao de tarefa numa fila\n" );
				    listaEF.adicionaEvento( 0, tarefa, tarefa.getTempoChegadaFila() ); //insercao sera feita, entao essa nova insercao
    				filaInicial.poll();					//tem que ser feita diretamente, pois o tempo
					contador.setPorcentagem(razao);
				}                                   //de ocorrencia dela eh sempre maior que o do evento atual
			
				if( tarefa.getTempoChegadaFila() <= eventoAtual.getTempoOcorrencia() )   
		        {   System.out.printf("2 if: AGENDAMENTO DE EVENTO - Adicao de tarefa numa fila\n" );
				    listaEF.adicionaEvento( 0, tarefa, tarefa.getTempoChegadaFila() );
				    filaInicial.poll();
					contador.setPorcentagem(razao);
			    }
				else    
			    {  executaEventoLEF( eventoAtual ) ;
			    }
			}
			
			//A partir daqui, nao ha mais tarefas na filaInicial (todas ja estao no sistema)	
            NoLEF eventoAtual = listaEF.peek();
			//System.out.printf("VAi entrar\n");
			executaEventoLEF( eventoAtual ) ;
			
			if(contador.getCancelado()){
				return 0.0;
			}
			
		}
		System.out.printf("#######RelogioSimulacao = %f\n", relogioSimulacao.getRelogioSimulacao() );
		contador.setPorcentagem(12.0);
		contador.addMensagem("OK\n");

    	Double tempo = relogioSimulacao.getRelogioSimulacao();
		return tempo;

 	}
	public void executaEventoLEF( NoLEF eventoAtual )
	{   Double incrementoRelogioEE = 0.0;
	    NoFila tarefaAtual = eventoAtual.getTarefa();
		NoFila novaTarefa;
		int idServ;
	    boolean eMestre;
		boolean mestreLivre;
		boolean filaMestreVazia;
		boolean servComunLivre;
		boolean filaComunVazia;
		int idTarefaAlocadaServComun;
		boolean servProcLivre;
		int idTarefaAlocadaServProc;
		boolean csAtualEhEscalonador;
		int idTarefaAlocadaServidor;
		int idCSAnterior = -1;
		Double tServicoProcto;
		Double tamanhoProcTarefaAtual;
		Double delta;
		
		switch( eventoAtual.getTipoEvento() )
        {   // 0: Chegada de uma tarefa em uma fila; 
		    case 0:
			          System.out.printf("EXECUCAO DE EVENTO - Adicao de tarefa numa fila\n");

			          System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      
					  System.out.printf("Relogio da Simulacao (ANTES de case 0): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  incrementoRelogioEE = redeDeFilas.adicionaTarefaFila( tarefaAtual );
				  
			          if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );

/*					  //MARCO
					  if( redeDeFilas.getTipoCS( tarefaAtual.getIdCSAtual() ) == 0 )
					  {		estatistica.addNoTarefasFila( 1, tarefaAtual.getIdTarefa(), tarefaAtual.getTamanhoProcTarefa(), relogioSimulacao.getRelogioSimulacao() );
					  }
					  if( redeDeFilas.getTipoCS( tarefaAtual.getIdCSAtual() ) == 2 || redeDeFilas.getTipoCS( tarefaAtual.getIdCSAtual() ) == 3 )
					  {		estatistica.addNoTarefasFila( 0, tarefaAtual.getIdTarefa(), tarefaAtual.getTamanhoComTarefa(), relogioSimulacao.getRelogioSimulacao() );
					  }
					  if( redeDeFilas.getTipoCS( tarefaAtual.getIdCSAtual() ) == 1 )
					  {		if( tarefaAtual.getIdServidorAtual() == 0 )
								estatistica.addNoTarefasFila( 0, tarefaAtual.getIdTarefa(), tarefaAtual.getTamanhoComTarefa(), relogioSimulacao.getRelogioSimulacao() );
							else
								estatistica.addNoTarefasFila( 1, tarefaAtual.getIdTarefa(), tarefaAtual.getTamanhoComTarefa(), relogioSimulacao.getRelogioSimulacao() );
					  }
*/					  System.out.printf("Relogio da Simulacao (DEPOIS de case 0): %f\n", relogioSimulacao.getRelogioSimulacao() );
				//	  System.out.printf("Depois\n");
				//	  tarefaAtual.imprimeDadosTarefa();

        			  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Adicao de tarefa numa fila \n");
					  listaEF.poll();

					  //HABILITACAO (Agendamento) dos eventos que dependem da entrada de uma tarefa em uma fila 

					  eMestre = redeDeFilas.verificaSeServEMestre( tarefaAtual );
					  if( eMestre == true && tarefaAtual.getFinalizada() == false ) // Se o servidor no qual a tarefa foi alocada e mestre, entao agendo escalonamento
			    	  {   System.out.printf("MESTRE\n");
					      mestreLivre = redeDeFilas.verificaMestreLivre( tarefaAtual );
						  filaMestreVazia = redeDeFilas.verificaFilaMestreVazia( tarefaAtual );

                          System.out.printf("AGENDAMENTO DE EVENTO - Entrada de tarefa num mestre\n" );
					      listaEF.adicionaEvento( 1, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );   
					      
				      }
                      //Servidor nao eh mestre, entao ele eh um servidor de comunicacao, entao agendo a transmissao de uma tarefa
					  else if( tarefaAtual.getFinalizada() == false )  
                      {       System.out.printf("AGENDAMENTO DE EVENTO - Entrada de tarefa num servidor de comunicacao\n" );
					          listaEF.adicionaEvento( 4, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );						  

                      }					  

					  break;
			// 1: Entrada de uma tarefa em um no mestre;
			case 1:   int idTarefaAlocadaMestre;
			          System.out.printf("EXECUCAO DE EVENTO - Adicao de tarefa no mestre\n");
  
                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (ANTES de case 1): %f\n", relogioSimulacao.getRelogioSimulacao() );
			         
    				  incrementoRelogioEE = redeDeFilas.entradaTarefaMestre( tarefaAtual );      
			          
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );

					  System.out.printf("Relogio da Simulacao (DEPOIS de case 1): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  
			          listaEF.poll();

					  //HABILITACAO (Agendamento) dos eventos que dependem da entrada de uma tarefa no mestre 
					  
					  mestreLivre = redeDeFilas.verificaMestreLivre( tarefaAtual );
					  idTarefaAlocadaMestre = redeDeFilas.buscaIdTarefaAlocadaMestre( tarefaAtual );
					  /*Semantica do if abaixo: se mestre esta ocupado com a tarefaAtual, entao 
					  agendo o escalonamento dela.
					  */

					  if(  mestreLivre == false && idTarefaAlocadaMestre == tarefaAtual.getIdTarefa() && tarefaAtual.getFinalizada() == false )
				      {   System.out.printf("AGENDAMENTO DE EVENTO - Escalonamento\n" );
				          listaEF.adicionaEvento( 2, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );  
				      }
                      break;
			// 2: Escalonamento de tarefa;
            case 2:   System.out.printf("EXECUCAO DE EVENTO - Escalonamento\n");
                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (antes de case 2): %f\n", relogioSimulacao.getRelogioSimulacao() );
                      
					  incrementoRelogioEE = redeDeFilas.escalonaTarefa( tarefaAtual );   
			          
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );
					  //tarefaAtual.setIdCSAtual(1);//ESTOJO
					  System.out.printf("Relogio da Simulacao (DEPOIS de case 2): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  System.out.printf("Depois\n");
					  tarefaAtual.imprimeDadosTarefa();					  

					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Escalonamento\n");
					  listaEF.poll();
                      
					  
					  //HABILITACAO (Agendamento) dos eventos que dependem do escalonamento 
					  
					  if( tarefaAtual.getFinalizada() == false )
					  {   System.out.printf("AGENDAMENTO DE EVENTO - Saida de uma tarefa de um mestre em direcao a uma fila do sistema\n" );
					      listaEF.adicionaEvento( 3, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );
                      }
					  break;
			
			// 3: Saida de uma tarefa de um mestre em direcao a uma fila do sistema
			case 3:	  System.out.printf("EXECUCAO DE EVENTO - Saida de uma tarefa de um mestre em direcao a uma fila do sistema - \n");
	  
					  System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (ANTES de case 3): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  
					  incrementoRelogioEE = redeDeFilas.saidaTarefaMestre( tarefaAtual );   
			          
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );
                      System.out.printf("Relogio da Simulacao (DEPOIS de case 3): %f\n", relogioSimulacao.getRelogioSimulacao() );				  

					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Saida de uma tarefa de um mestre em direcao a uma fila do sistema\n");
					  listaEF.poll();
                      
					  
					  //HABILITACAO (Agendamento) dos eventos que dependem da saida de uma tarefa do mestre
			          //Agendamento da entrada desta tarefa no seu novo CS ou na sua nova fila
					  if( tarefaAtual.getFinalizada() == false )
					  {   System.out.printf("AGENDAMENTO DE EVENTO - Chegada de uma tarefa na fila de um CS\n" );
				          listaEF.adicionaEvento( 0, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE );
					  }
					  //Agendamento de entrada de uma nova tarefa no mestre
					  mestreLivre = redeDeFilas.verificaMestreLivre( tarefaAtual );
					  filaMestreVazia = redeDeFilas.verificaFilaMestreVazia( tarefaAtual );

				      if(  mestreLivre == true && filaMestreVazia == false && tarefaAtual.getFinalizada() == false )
				      {   System.out.printf("65AGENDAMENTO DE EVENTO - Entrada de uma nova tarefa num mestre\n" );
					      novaTarefa = redeDeFilas.peekFilaMestre( tarefaAtual.getIdCSAtual() );
						  //novaTarefa.imprimeDadosTarefa();
						//  listaEF.adicionaEvento( 1, novaTarefa, relogioSimulacao.getTempoAtual()  );   
					  }
                      break;			
            //Entrada de uma tarefa em um no de comunicacao
			case 4:   System.out.printf("Antes\n");
					
					  //MARCO
					  estatistica.addNoTarefasFila( 0, tarefaAtual.getIdTarefa(), tarefaAtual.getTamanhoComTarefa(), tarefaAtual.getTempoChegadaFilaCom() );
					  
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (antes de case 4): %f\n", relogioSimulacao.getRelogioSimulacao() );
			  
         			  incrementoRelogioEE = redeDeFilas.entradaTarefaServComun( tarefaAtual );      

					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );
					 
    				  System.out.printf("Relogio da Simulacao (DEPOIS de case 4): %f\n", relogioSimulacao.getRelogioSimulacao() );

					  listaEF.poll();
					  
					  //HABILITACAO (Agendamento) dos eventos que dependem da entrada de uma tarefa num servidor de comunicaco 

        			  servComunLivre = redeDeFilas.verificaSeServidorEstaLivre( tarefaAtual );
					  idTarefaAlocadaServComun = redeDeFilas.buscaIdTarefaAlocadaServidor( tarefaAtual );
				
				      if(  servComunLivre == false && idTarefaAlocadaServComun == tarefaAtual.getIdTarefa() && tarefaAtual.getFinalizada() == false )
				      {   System.out.printf("AGENDAMENTO DE EVENTO - Envio\n" );
				          listaEF.adicionaEvento( 5, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    ); 
				  
     				  }
					  break;
            // 5: Envio (comunicacao) de tarefa;
            case 5:   //EXECUCAO efetiva de um envio
			          System.out.printf("EXECUCAO DE EVENTO - Envio\n");
 					  //MARCO
					  estatistica.setTempoChegadaServidor( 0, tarefaAtual.getIdTarefa(), relogioSimulacao.getRelogioSimulacao() );
					  
                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (ANTES de case 5): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  			  
        			  incrementoRelogioEE = redeDeFilas.envioTarefa( tarefaAtual );   

		          
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );
					 
 					  System.out.printf("Relogio da Simulacao (DEPOIS de case 5): %f\n", relogioSimulacao.getRelogioSimulacao() );

					  //MARCO
					  estatistica.setTempoSaidaSistema( 0, tarefaAtual.getIdTarefa(), relogioSimulacao.getRelogioSimulacao() );

					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Envio\n");
					  listaEF.poll();
                      

					  //HABILITACAO (Agendamento) dos eventos que dependem do envio 
			          /*   Como a variavel ttl de cada tarefa e decrementada somente depois que ja
					  esta dentro do hop (ou melhor dizendo, ela e decrementada no exato momento
					  em que entra no hop), se getTtl() e maior que um, ela esta indo em direcao
					  a um CS intermediario do caminho (um servidor de comunicacao). Mas se
					  getTtl() == 1, ela esta indo para o destino final.
					  */
                      csAtualEhEscalonador = redeDeFilas.csAtualEhEscalonador( tarefaAtual.getIdCSAtual() );
					  if( tarefaAtual.getTtl() > 1  && tarefaAtual.getFinalizada() == false ) //Habilita saida em direcao a um hop intermediario
					  {   System.out.printf("AGENDAMENTO DE EVENTO - Saida de uma tarefa para um servidor de comunicacao\n" );
				          listaEF.adicionaEvento( 6, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );
					  }
					  else if( tarefaAtual.getTtl() <= 1 && tarefaAtual.getFinalizada() == false ) //Habilita saida ao hop final (servidor de processamento) 
                      {   System.out.printf("AGENDAMENTO DE EVENTO - Saida de uma tarefa para um servidor de processamento\n" );
				          listaEF.adicionaEvento( 7, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );
					  }
		              else if( tarefaAtual.getTtl() <= 1 && csAtualEhEscalonador == true && tarefaAtual.getFinalizada() == false )
                      {   System.out.printf("csAtualEhEscalonador = true\n;");
					  }
					  break;
			          
            // 6: Saida de uma tarefa de um servidor de comunicacao em direcao a um proximo hop;
			case 6:   System.out.printf("EXECUCAO - Saida de uma tarefa de um servidor de comunicacao em direcao a outro servidor de comunicacao - \n");

                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (antes de case 6): %f\n", relogioSimulacao.getRelogioSimulacao() );				

					  incrementoRelogioEE = redeDeFilas.saidaTarefaServComunHopIntermediario( tarefaAtual );   
			     
				      relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );
     				  System.out.printf("Relogio da Simulacao (DEPOIS de case 6): %f\n", relogioSimulacao.getRelogioSimulacao() );

					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Saida de uma tarefa de um servidor de comunicacao em direcao a outro servidor de comunicacao\n");
					  listaEF.poll();
                      
					  
					  //HABILITACAO (Agendamento) dos eventos que dependem da saida de uma tarefa do mestre

					  csAtualEhEscalonador = redeDeFilas.csAtualEhEscalonador( tarefaAtual.getIdCSAtual() );
					  if( tarefaAtual.getTtl() > 1 && tarefaAtual.getFinalizada() == false ) //Habilita saida em direcao a um hop intermediario
					  {   System.out.printf("AGENDAMENTO DE EVENTO - Saida de uma tarefa para um servidor de comunicacao\n" );
				          listaEF.adicionaEvento( 0, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );
					  }
					  else if( tarefaAtual.getTtl() <= 1 && tarefaAtual.getFinalizada() == false ) //Habilita saida ao hop final (servidor de processamento) 
                      {   System.out.printf("AGENDAMENTO DE EVENTO - Saida de uma tarefa para um servidor de processamento\n" );
				          listaEF.adicionaEvento( 0, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );
					  }
		              else if( tarefaAtual.getTtl() <= 1 && csAtualEhEscalonador == true && tarefaAtual.getFinalizada() == false )
                      {   System.out.printf("AGENDAMENTO DE EVENTO - Saida de uma tarefa para um mestre (novo escalonamento)\n" );
					      listaEF.adicionaEvento( 0, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE    );
					  }
    				  //Habilita a chegada desta tarefa a fila de um hop intermediario	  
					  
					  //Habilita, se existir, a entrada de uma tarefa neste servidor de comunicacao
					  idCSAnterior   = redeDeFilas.localizaCSanterior( tarefaAtual );
					  servComunLivre = redeDeFilas.verificaSeServidorComunAntigoEstaLivre( idCSAnterior  );
					  filaComunVazia = redeDeFilas.verificaFilaComunCSAnteriorVazia( idCSAnterior );				  

					  if( filaComunVazia == false && tarefaAtual.getFinalizada() == false )
				      {   System.out.printf("AGENDAMENTO DE EVENTO - Entrada de uma nova tarefa num servidor de comunicacao\n" );
					      novaTarefa = redeDeFilas.peekFilaComun( idCSAnterior );
						  novaTarefa.imprimeDadosTarefa();
						  listaEF.adicionaEvento( 1, novaTarefa, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );   
					  }
					  
                      break;

            // 7: Saida de uma tarefa em direcao ao hop final;
			case 7:   System.out.printf("EXECUCAO - Saida de uma tarefa de um servidor de comunicacao em direcao ao hop final (servidor de processamento) - \n");
                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (ANTES de case 7): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  					  
					  incrementoRelogioEE = redeDeFilas.saidaTarefaServComunHopFinal( tarefaAtual );   
			          
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );
					 
					  System.out.printf("Relogio da Simulacao (DEPOIS de case 7): %f\n", relogioSimulacao.getRelogioSimulacao() );

					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Saida de uma tarefa de um servidor de comunicacao em direcao ao hop final (servidor de processamento)\n");
					  listaEF.poll();
                      
					  
					  //HABILITACAO (Agendamento) dos eventos que dependem da saida de uma tarefa do servidor de comunicacao em direcao ao hop final
                      
				     /*Habilita a chegada desta tarefa ao hop final (lembrar que hop final naum tem 
					 fila, entao o evento abaixo tem que ser do tipo 8 (e nao do tipo 0)
					 */
					  csAtualEhEscalonador = redeDeFilas.csAtualEhEscalonador( tarefaAtual.getIdCSAtual() );					  
					  // "if" abaixo: A proposicao tarefaAtual.getIdServidorDestino() <= 0 mostra que ainda NAO houve escalonamento (pois o servidor de destino ainda esta em -1)
					  if( tarefaAtual.getTtl() <= 1 && csAtualEhEscalonador == true && tarefaAtual.getIdServidorAtual() <= 0 && tarefaAtual.getFinalizada() == false )
                      {   System.out.printf("AGENDAMENTO DE EVENTO - Envio para uma fila de mestre para se fazer um novo escalonamento\n" );
					      listaEF.adicionaEvento( 0, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );
					  }  // "if" abaixo: A proposicao tarefaAtual.getIdServidorAtual() >= 0 mostra que ja houve escalonamento (pois o servidor Atual jah foi "setado")
					  if( tarefaAtual.getTtl() <= 1 && csAtualEhEscalonador == true && tarefaAtual.getIdServidorAtual() > 0 && tarefaAtual.getFinalizada() == false )
                      {   System.out.printf("AGENDAMENTO DE EVENTO - Envio para no de processamento\n" );
					      listaEF.adicionaEvento( 8, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );
					  }
                      if( tarefaAtual.getTtl() <= 1 && csAtualEhEscalonador == false && tarefaAtual.getIdServidorDestino() <= 0 && tarefaAtual.getFinalizada() == false )
                      {   System.out.printf("AGENDAMENTO DE EVENTO - Entrada de uma tarefa em um no de processamento\n" ); 					 
					      listaEF.adicionaEvento( 8, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );
					  }	  
					  
					  //Habilita, se existir, a entrada de uma tarefa neste servidor de comunicacao

					  idCSAnterior = redeDeFilas.localizaCSanterior( tarefaAtual );					  
					  filaComunVazia = redeDeFilas.verificaFilaComunCSAnteriorVazia( idCSAnterior );				  
					  if( filaComunVazia == false )
				      {   System.out.printf("AGENDAMENTO DE EVENTO - Entrada de uma nova tarefa no servidor de comunicacao" );
					      novaTarefa = redeDeFilas.peekFilaComun( idCSAnterior );
						  novaTarefa.imprimeDadosTarefa();
						  listaEF.adicionaEvento( 1, novaTarefa, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );   
					  }
					  
                      break;
            //8: Entrada de uma tarefa em um no de processamento
            case 8:   //EXECUCAO efetiva da adicao de uma tarefa em um no de processamento

					  //MARCO
					  estatistica.addNoTarefasFila( 1, tarefaAtual.getIdTarefa(), tarefaAtual.getTamanhoComTarefa(), tarefaAtual.getTempoChegadaFila() ); 
					  
			          System.out.printf("EXECUCAO DE EVENTO - Adicao de tarefa num no de processamento\n");
                      System.out.printf("id do Evento = %d\n", eventoAtual.getIdEvento() );   
                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (antes de case 8): %f\n", relogioSimulacao.getRelogioSimulacao() );

					  servProcLivre = redeDeFilas.verificaSeServidorEstaLivre( tarefaAtual );
					  tamanhoProcTarefaAtual = redeDeFilas.getTamanhoProcTarefaAtual( tarefaAtual );
                      idTarefaAlocadaServProc = redeDeFilas.buscaIdTarefaAlocadaServidor( tarefaAtual );
					  
					  if( servProcLivre == true )
                      {   incrementoRelogioEE = redeDeFilas.entradaTarefaServProc( tarefaAtual, relogioSimulacao.getRelogioSimulacao() );      
     				      precisaAtualizarLEF = true;
					      relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );

					      System.out.printf("Relogio da Simulacao (DEPOIS de case 8): %f\n", relogioSimulacao.getRelogioSimulacao() );
					 	  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - Case 8\n");
						  
						  idTarefaAlocadaServProc = redeDeFilas.buscaIdTarefaAlocadaServidor( tarefaAtual );
						  servProcLivre = redeDeFilas.verificaSeServidorEstaLivre( tarefaAtual );
						  if(  servProcLivre == false && idTarefaAlocadaServProc == tarefaAtual.getIdTarefa() && tarefaAtual.getFinalizada() == false )
				          {    System.out.printf("AGENDAMENTO DE EVENTO - Processamento\n" );
				               listaEF.adicionaEvento( 9, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );
				          }

                      }
                      else if( precisaAtualizarLEF == true )
                      {   System.out.printf("REAGENDAMENTO DE EVENTO - Processamento\n" );
				          precisaAtualizarLEF = false;
						  listaEF.adicionaEvento( 8, tarefaAtual, eventoAtual.getTempoOcorrencia()  );
						  delta = redeDeFilas.calculaDeltaAtualizacaoLEF( tarefaAtual );
						  listaEF = listaEF.atualizaLEF( eventoAtual, delta, idTarefaAlocadaServProc );
                      }
                      listaEF.poll();
                      break;					  
					  
			//9: Processamento 'computacional' de uma tarefa;
			case 9:   System.out.printf("EXECUCAO - 'Processamento Computacional'\n");

			          System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
                      if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					  System.out.printf("Relogio da Simulacao (antes de case 9): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  
					  //MARCO
					  estatistica.setTempoChegadaServidor( 1, tarefaAtual.getIdTarefa(), relogioSimulacao.getRelogioSimulacao() );			  
						   
					  int servidorEficiencia;
					  servidorEficiencia = tarefaAtual.getIdServidorAtual();
        			  incrementoRelogioEE = redeDeFilas.processamentoTarefa( tarefaAtual );   
                      System.out.printf("Relogio da Simulacao (DEPOIS de case 9): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  
					  //A Partir daqui � o case 10 (foi fundido com o case 9)!
					  
                      System.out.printf("EXECUCAO - 10: Saida de uma tarefa de um no de processamento\n");
                      System.out.printf("id do Evento = %d\n", eventoAtual.getIdEvento() );
                      System.out.printf("Antes\n");
					  tarefaAtual.imprimeDadosTarefa();
		              incrementoRelogioEE  += redeDeFilas.saidaTarefaServidor( tarefaAtual );
					  relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );

					  System.out.printf("Relogio da Simulacao (DEPOIS de case 10): %f\n", relogioSimulacao.getRelogioSimulacao() );

					  //MARCO
					  estatistica.setTempoSaidaSistema( 1, tarefaAtual.getIdTarefa(), relogioSimulacao.getRelogioSimulacao() );
					  System.out.printf("ID CS %d ID SERV %d !!!!", tarefaAtual.getIdCSAtual(), tarefaAtual.getIdServidorAtual() );
					  estatistica.setEficiencia( redeDeFilas.valorServ( tarefaAtual.getIdCSAtual(), servidorEficiencia ), incrementoRelogioEE );
					  
					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - 9'Processamento Computacional'\n");
					  System.out.printf("TERMINO DA EXECUCAO DE UM EVENTO - 10: Saida de uma tarefa de um no de processamento\n");
					  listaEF.poll();
					  
                      if( tarefaAtual.getFinalizada() == true )
					  {   System.out.printf("AGENDAMENTO - 11: Impressao dos resultados finais e finalizacao do sistema\n");   
					      listaEF.adicionaEvento( 11, tarefaAtual, eventoAtual.getTempoOcorrencia( ) + incrementoRelogioEE   );
					  }
                      break;

            
			/* 
			    O CASE 10 FOI FUNDIDO COM O CASE 9!
			//10: Saida de uma tarefa de um no de processamento;
			case 10:   
			           System.out.printf("EXECUCAO - 10: Saida de uma tarefa de um no de processamento\n");
                       System.out.printf("id do Evento = %d\n", eventoAtual.getIdEvento() );
                       System.out.printf("Antes\n");
					   tarefaAtual.imprimeDadosTarefa();
                       if( eventoAtual.getTempoOcorrencia( ) >= relogioSimulacao.getRelogioSimulacao() )
					     relogioSimulacao.atualizaRelogioSimulacao( eventoAtual.getTempoOcorrencia( ) );
					   System.out.printf("Relogio da Simulacao (ANTES de case 10): %f\n", relogioSimulacao.getRelogioSimulacao() );
					  
					   incrementoRelogioEE = redeDeFilas.saidaTarefaServidor( tarefaAtual );   
					 
					   relogioSimulacao.incrementaRelogioSimulacao( incrementoRelogioEE );

    				   System.out.printf("Relogio da Simulacao (DEPOIS de case 10): %f\n", relogioSimulacao.getRelogioSimulacao() );
                  	   
					  
					   listaEF.poll();
            */          
					  
            //11: Impressao dos resultados finais e finalizacao do sistema;
            case 11:  listaEF.poll();
				if( listaEF.getnumEventosNaLista() == 0 )
				{	   System.out.printf("\nACABOU SIMULACAO!\n");
					   //MARCO
					   estatistica.setTempoFinal( relogioSimulacao.getRelogioSimulacao() );
					   estatistica.fimSimulacao();
					   estatistica.imprimeRede();
					   estatistica.imprimeTarefa();
				}
				break;
	    }
	}	

	public void setNumTarefasCriadas( int nTarCriadas )
	{   numTarefasCriadas = ( nTarCriadas >= 0 ) ? nTarCriadas : 0; 
	}

    public void setNumEscalonadores( int nEscalonadores )
    {   numEscalonadores = ( nEscalonadores >= 0 ) ? nEscalonadores : 0;
	    
		//Instancio tambem o vetor escalonadores
		escalonadores = new int[numEscalonadores];
		
		//Inicializo o indice que controlara o vetor escalonadores
		indiceAtualEscalonadorReceptor = 0;
    }
	
	public int getNumEscalonadores()
	{   return numEscalonadores;
	}

    public void setNumTotalCSs( int numCSs )
    {   numTotalCSs = ( numCSs >= 0 ) ? numCSs : 0;
	    
		//Instancio tambem o vetor escalonadores
		qtdadeTarefasCadaCSEscalonador = new int[numCSs];
    } 
 	
	public int getNumTotalCSs()
	{   return numTotalCSs;
	}

	public void alteraNumAtualTarefasCriadas( int aSomar )
	{   numTarefasCriadas += aSomar; 
	}

}