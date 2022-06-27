package ispd.RedesDeFilas;

import java.util.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import ispd.Simulacao.*;
import java.io.*;
import ispd.Estatistica.*;

public class RedesDeFilas
{   
    private int numCS;
    private List<Integer> escalonadores;
	private HashSet<CentrosDeServico> centroDeServicos;
    private int matrizRF[][];
	private int tam_mat;
    private int vetorElementosRF[];
	private int posicaoAtualInsercao = 0;
	private Estatistica estatRedeDeFilas;
	private MetricaCS atualCS;
	
//+------------------------------------------Inicio dos metodos da classe------------------------------------------+	
/* OBSERVACAO: os metodos dessa classe sao analogos aos metodos da classe CentroDeServico, soh que eles
funcionam em outra instancia (nivel). Aqui, funcionam para a rede de filas toda, ou seja, eh necessario determinar
(pelo id) o centro de servico que se deseja trabalhar. Jah na classe CentroDeServico, o centro de servico nao 
precisa ser determinado (pelo id), pois isso jah eh feito chamando o metodo pro objeto (da classe CentroDeServico) 
que esta em questao. Nao serao feitos comentarios nos metodos desta classe, pois ou o codigo pode ser facilmente
entendido (desde que se tenha conhecimento do projeto) ou os comentarios feitos na classe CentroDeServico podem, por
si, ajudar no entendimento do codigos daqui. 
*/
//+-------------------------------------------------Metodo construtor----------------------------------------------+	
	public RedesDeFilas()
	{   numCS = 0;
	    escalonadores = new ArrayList<Integer>();
		
		centroDeServicos = new HashSet<CentrosDeServico>();
		
		estatRedeDeFilas = new Estatistica( 0.0 );
	}
//+----------------------------Metodos que adicionam um centro de servico a rede de filas----------------------------+	
    /* EXPLICACAO SOBRE A DIFERENCA DO NUMERO DE PARAMETROS ENTRE O METODO adicionaCentroServico 
	DA CLASSE RedesDeFilas E O CONSTRUTOR DA CLASSE CentrosDeServico:
	O metodo adicionaCentroServico da classe RedesDeFilas possui um argumento a menos do que o 
	construtor da classe CentrosDeServico pois apesar desta ultima necessitar do parametro idCS, este
	parametro nao e lido pelo interpretador/arquivo, mas sim controlado pela classe RedesDeFilas (numCS)
	Ou seja, o construtor da classe CentrosDeServico recebe TODOS os parametros do metodo adicionaCentroServico
    da classe RedesDeFilas (que recebe/le esses argumentos do interpretador/arquivo) MAIS o idCS que
    e a variavel numCS controlada pela propria classe RedesDeFilas (ou seja NAO e recebido/lido do
    arquivo/interpretador)	
	*/
	
	//Para servidores do tipo maquina ou cluster 'heterogeneos'
	public int adicionaCentroServico( int tp, int nMaxServ, int nMaxFilas, int nEscravos, int vetEscravos[] )
	{   switch( tp ) //Se for maquina
	    {   case 0:   {   CentrosDeServico CS = new CentrosDeServico( numCS, tp, nMaxServ, nMaxFilas, nEscravos, vetEscravos );
		                  centroDeServicos.add( CS );
						  estatRedeDeFilas.addMetricaCS(numCS);

						  System.out.printf( "|\tCS (tipo0) ID: %2d adicionado                    |\n", numCS );
						  break;
					  }
			
			case 1:   {   System.out.printf( "\n nMaxServ = %d\n", nMaxServ );
			              CentrosDeServico CS = new CentrosDeServico( numCS, tp, nMaxServ, nMaxFilas, 1, nEscravos, vetEscravos ); //O "1" na chamada e apenas uma flag. Poderia ser um valor qualquer
					      centroDeServicos.add( CS );
						  estatRedeDeFilas.addMetricaCS(numCS);
						  System.out.printf( "|\tCS ID (tip 1): %2d adicionado                    |\n", numCS );
						  break;
					  }
					  
		}
		System.out.printf( "|\tCS ID: %2d adicionado                    |\n", numCS );
        numCS++;
		return (numCS-1);
	}
	
	//Para servidores do tipo rede (ponto-a-ponto ou internet) ou cluster 'homogeneo'
	public int adicionaCentroServico( int tp, int nMaxServ, int nMaxFilas )
	{   switch( tp ) //Se for maquina
	    {  /* case 0:   {   CentrosDeServico CS = new CentrosDeServico( numCS, tp, nMaxServ, nMaxFilas );   
			              centroDeServicos.add( CS );
						  estatRedeDeFilas.addMetricaCS(numCS);
						  break;
					  }	  
			*/		  
			case 1:	  {   CentrosDeServico CS = new CentrosDeServico( numCS, tp, nMaxServ, nMaxFilas, 1 );
			              centroDeServicos.add( CS );
						  estatRedeDeFilas.addMetricaCS(numCS);
						  System.out.printf( "|\tCS ID (tp 1): %2d adicionado                    |\n", numCS );
						  break;
					  }
		    
			case 2:   {   CentrosDeServico CS = new CentrosDeServico( numCS, tp, nMaxServ, nMaxFilas );   
			              centroDeServicos.add( CS );
						  estatRedeDeFilas.addMetricaCS(numCS);
						  break;
					  }
			case 3:	  {	  CentrosDeServico CS = new CentrosDeServico( numCS, tp, nMaxServ, nMaxFilas );
			              centroDeServicos.add( CS );
						  estatRedeDeFilas.addMetricaCS(numCS);
						  break;
					  }
		}
		System.out.printf( "|\tCS ID: %2d adicionado                    |\n", numCS );
        numCS++;
		return (numCS-1);
	}
//+-------------Metodo que adiciona um servidor de processamento unico. (Para CSs do tipo "maquina")----------------------------+
	public void adicionaServidorProcto( int idCS, int tpServ, boolean msOuEsc, Double tServProcto )
	{	if(msOuEsc){
			escalonadores.add(idCS);
		}
	    for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == idCS )
		    {   
			    //MARCO
				atualCS = estatRedeDeFilas.descobreServidorCS( idCS );
				
				csTemp.adicionaServidorProcto( tpServ, msOuEsc, tServProcto, atualCS );
			}
		}
	}

	public void adicionaServidorCom( int idCS, int tpServ, Double tServRede, double ltnc, double txOcup )
	{   for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == idCS )
		    {	
			//MARCO
				atualCS = estatRedeDeFilas.descobreServidorCS( idCS );
				
				csTemp.adicionaServidorCom( tpServ, tServRede, ltnc, txOcup, atualCS );
			}
		}
	}

	/*	public void adicionaServidoresInt( int idCS, int tpServ, Double tServRede, int tpDistCom, double ltnc, double txOcup )	{   for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == idCS )
		    {   csTemp.adicionaServidoresInt( tpServ, tServProctoTotal, csTemp.getNumMaxServidores(), tpDistrProcto );
			}
		}
	}
*/	

	public void adicionaServidoresClr( int idCS, int tpServ, Double tServProcto, Double tServRede, double ltnc, double txOcup )
	{   for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == idCS )
		    {   //MARCO
				atualCS = estatRedeDeFilas.descobreServidorCS( idCS );
				System.out.printf("CS PASSADO %d", idCS);
				
				csTemp.adicionaServidoresClr( tpServ, tServProcto, tServRede, ltnc, txOcup, atualCS );
			}
		}
	}

	public void adicionaFila( int idCS )
	{   for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == idCS )
		    {   csTemp.adicionaFila();
			}
		}
	}

	public Double adicionaTarefaFila( NoFila tarefa )
	{   Double retornoFuncao = 0.0;  //Eh o tempo que o sistema levou para adicionar uma tarefa na fila.
                                     //ver metodo adicionaTarefa da classe Filas.java!	
	    
	    for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.adicionaTarefaFila( tarefa );
			}
		}
		
		return retornoFuncao;
	}

	public boolean verificaSeServEMestre( NoFila tarefa )
	{   boolean retornoFuncao = false;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.verificaSeServEMestre( tarefa ) ;
                				
			}
		}
		
		return retornoFuncao;
	}

	public boolean verificaMestreLivre( NoFila tarefa )
	{   boolean retornoFuncao = false;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {  if( csTemp.getEhEscalonador() == true ) 
			   retornoFuncao = csTemp.verificaMestreLivre() ;
                				
			}
		}
		
		return retornoFuncao;
	}

	public boolean verificaFilaComunVazia( NoFila tarefa )
	{   boolean retornoFuncao = false;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   System.out.printf("verificaFilaComunVazia para o CS: %d\n", csTemp.getIdCS() );
			    System.out.printf("tipo CS: %d\n", csTemp.getTipo() );
			    retornoFuncao = csTemp.verificaFilaComunVazia( csTemp.getTipo() );
                				
			}
		}
		
		return retornoFuncao;
	}

	public boolean verificaFilaComunCSAnteriorVazia( int idCSAnterior )
	{   boolean retornoFuncao = false;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == idCSAnterior )
		    {   System.out.printf("verificaFilaComunVazia para o CS: %d\n", csTemp.getIdCS() );
			    System.out.printf("tipo CS: %d\n", csTemp.getTipo() );
			    retornoFuncao = csTemp.verificaFilaComunVazia( csTemp.getTipo() );
                				
			}
		}
		
		return retornoFuncao;
	}
	
	public boolean verificaFilaMestreVazia( NoFila tarefa )
	{   boolean retornoFuncao = false;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.verificaFilaMestreVazia( csTemp.getTipo() );
                				
			}
		}
		
		return retornoFuncao;
	}
	
	public boolean verificaSeServidorEstaLivre( NoFila tarefa )
	{   boolean retornoFuncao = false;

		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.verificaSeServidorEstaLivre( tarefa ) ;				
			}
		}
		
		return retornoFuncao;
	}

	public boolean verificaSeServidorComunAntigoEstaLivre( int idCSAnterior )
	{   boolean retornoFuncao = false;

		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == idCSAnterior  )
		    {  if( csTemp.getTipo() == 2 || csTemp.getTipo() == 3 )
			   {   retornoFuncao = csTemp.verificaSeServidorEstaLivre( 0 ) ;
               }
			   else if( csTemp.getTipo() == 1 )
               {   retornoFuncao = csTemp.verificaSeServidorEstaLivre( csTemp.getNumMaxServidores() ) ;
               }			   
			}
		}
		
		return retornoFuncao;
	}
	
	public boolean csAtualEhEscalonador( int iCS )
	{   boolean retornoFuncao = false;

		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == iCS )
		    {   retornoFuncao = csTemp.getEhEscalonador(  ) ;				
			}
		}
		
		return retornoFuncao;
	}
	
	public Double escalonaTarefa( NoFila tarefa )
    {   Double retornoFuncao = 0.0;
	    int idCSDestinoTarefa = 0;
		int vetorPrecedentesInvertido[] = new int [tam_mat];
		int vetorCaminho[] = new int [tam_mat]; //Dijsktra retorna os precedentes, mas eu ainda tenho que 
		                                      //'minerar' ('desenverter') os prededentes para saber o caminho.
        boolean eMestreTipo0 = false;
		boolean eTipo1 = false;
		int idEscravoDestinoTarefa = -1;											  
	    for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   if( csTemp.getTipo( ) == 0 )  //Se CS for do tipo maquina, faz escalonamento externo
			    {   System.out.printf("Escalonamento externo\n");
				    idCSDestinoTarefa = csTemp.determinaProximoEscravoRR();
				    tarefa.setIdCSDestino( idCSDestinoTarefa );
					tarefa.setIdServidorDestino( 0 );  //Se foi feito escalonamento externo, entao a tarefa
					 //vai pra um servidor de processamento (escravo) do tipo maquina e neste caso o id do Servidor e igual a 0
					vetorPrecedentesInvertido = Dijkstra( tarefa.getIdCSAtual(), tarefa.getIdCSDestino() );
					vetorCaminho = determinaCaminho( tarefa.getIdCSAtual(), tarefa.getIdCSDestino(), vetorPrecedentesInvertido );
				    tarefa.setDistanciaProximoMestre( vetorCaminho.length-1 );
					tarefa.setVetorCaminhoTarefa( vetorCaminho );               
					tarefa.setTtl(vetorCaminho.length-1);	
                    					   
				}
				else if( csTemp.getTipo( ) == 1 )  //Se CS for do tipo cluster, faz escalonamento interno
				{   System.out.printf("Escalonamento intra-cluster\n");
				    idEscravoDestinoTarefa = csTemp.verificaSeHaEscravoLivre( );
					//System.out.printf("idEscravoDestinoTarefa = %d\n", idEscravoDestinoTarefa);
					/*Se idEscravoDestinoTarefa for igual a -1, entao nao ha escravo livre no momento
					e a tarefa continua na fila do cluster. Como o id de um servidor mestre e sempre
					0, os ids dos escravos sao maiores ou iguais a 1. Logo, so faco escalonamento se
					idEscravoDestinoTarefa >= 1
					*/
					if( idEscravoDestinoTarefa >= 1 )
					{   tarefa.setIdServidorDestino( idEscravoDestinoTarefa );
					    tarefa.setIdFila( 1 );
						
					}
				}
            }					
		}
		
		return retornoFuncao;
    }	

	public Double entradaTarefaMestre( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
        
		
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   csTemp.pollFila( tarefa );
			    retornoFuncao = csTemp.atribuiTarefaMestre( tarefa, csTemp.getIdCS(), estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
            }					
		}	
	    
		return retornoFuncao;
	}

	public int buscaIdTarefaAlocadaMestre( NoFila tarefa )
	{   int retornoFuncao = -1;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.buscaIdTarefaAlocadaMestre();
                	
			}
		}
		
		return retornoFuncao;
	}
	
	public int buscaIdTarefaAlocadaServidor( NoFila tarefa )
	{   int retornoFuncao = -1;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.buscaIdTarefaAlocadaServidor( tarefa );
                	
			}
		}
		
		return retornoFuncao;
	}

	public Double calculaDeltaAtualizacaoLEF( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.calculaDeltaAtualizacaoLEF( tarefa );
                	
			}
		}
		
		return retornoFuncao;
	}
	
	public Double saidaTarefaMestre( NoFila tarefa )  //Saida do mestre eh sempre em direcao a um servidor de comunicacao,
	{   Double retornoFuncao = 0.0;      //servidor de comunicacao, nunca para servidor de processamento
	    CentrosDeServico csTempAux = null;
		int indiceProximoHop;
		
		tarefa.decrementaTtl();
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.saidaTarefaMestre( tarefa );
			    csTempAux = csTemp;
            }
		}

		if( csTempAux.getTipo() == 0 )  			
		{  
		   indiceProximoHop = tarefa.getDistanciaProximoMestre() - tarefa.getTtl();
		   tarefa.setIdCSAtual( tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) );
		   tarefa.setIdServidorAtual( 0 ); //Se o mestre esta em CS tipo 0, o proximo destino eh um 
		}                                  //CS diferente (de comunicacao, mas cujo servidor e sempre id 0)
		
		else if( csTempAux.getTipo() == 1 ) //Se a tarefa estive em um cluster, a saida do mestre se dara para uma fila do proprio cluster
		{   //No caso do cluster (CS tipo 1), nao preciso nem incrementar proximoHop e nem "setar"
            // o IdCSAtual da tarefa, pois a tarefa sai do mestre, mas nao do CS. ver comentarios
		    // na funcao saidaTarefaServComunHopFinal( NoFila tarefa ) para entender mais.
       	    
			tarefa.setIdServidorAtual( csTempAux.getNumMaxServidores() );  
			//poe o idServidorAtual da tarefa como o id do Servidor de comunicacao do cluster 
			//(sempre igual a csTemp.getNumMaxServidores() )			
			}
        
		return retornoFuncao;
	}
	
	public NoFila peekFilaMestre( int iCS )
	{   	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   System.out.printf("Entrou no peekFilaMestre\n");  
		    System.out.printf("iCS = %d\n", iCS ); 
		    if( csTemp.getIdCS() == iCS )
		    {   return csTemp.peekFilaMestre( );
            }					
		}
		
		return null;
	}

	public NoFila peekFilaComun( int iCS )
	{   	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == iCS )
		    {   System.out.printf("Entrou no peekFilaComum\n");  
			    System.out.printf("O CS %d eh do tipo %d\n", csTemp.getIdCS(), csTemp.getTipo() );  
				return csTemp.peekFilaComun( );
            }					
		}
		
		return null;
	}	
	public Double entradaTarefaServComun( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
        int iCs;
		int indiceProximoHop;

		for( CentrosDeServico csTemp : centroDeServicos )
		{    if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		     {   if( csTemp.getTipo() == 2 || csTemp.getTipo() == 3 || csTemp.getTipo() == 0 )
			        csTemp.pollFila( tarefa );  //Em um cluster, a fila de comunicacao tem id 1;
				 else if( csTemp.getTipo() == 1 )
                    csTemp.pollFila( tarefa );
		     }
        }
		
		if( tarefa.getTtl() > 1 )
		{  for( CentrosDeServico csTemp : centroDeServicos )
		   {   indiceProximoHop = tarefa.getDistanciaProximoMestre() - tarefa.getTtl();
			   if( csTemp.getIdCS() == tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) )
		       { 
				   if( csTemp.getTipo() == 2 || csTemp.getTipo() == 3 )
			       { //  csTemp.pollFila( 0 ); //Em CSs tipo 2 ou 3, a fila de comunicacao tem id 0
				       tarefa.setIdCSAtual( tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) );
					   tarefa.setIdServidorAtual( 0 );
					   retornoFuncao = csTemp.atribuiTarefaServComun( tarefa, estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
				   }
				}
            }					
		}
        else if( tarefa.getTtl() <= 1 )
        {   
			for( CentrosDeServico csTemp : centroDeServicos )
		    {  if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		       {   tarefa.setIdServidorAtual( csTemp.getNumMaxServidores() ); //Em um CS do tipo 1 (cluster) 
			//o id do servidor de comunicacao eh sempre o numero maximo de servidores
			       retornoFuncao = csTemp.atribuiTarefaServComun( tarefa, estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
			   }
            }
        }		
	
		return retornoFuncao;
	}

	public Double entradaTarefaServProc( NoFila tarefa, Double instanteOcupacaoServidor )
	{   Double retornoFuncao = 0.0;
	    
		if( tarefa.getTtl() <= 1 )
		{  for( CentrosDeServico csTemp : centroDeServicos )
		   {   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		       {   switch( csTemp.getTipo() )
			       {   
					   case 0:   tarefa.setIdServidorAtual( 0 );
							     retornoFuncao = csTemp.atribuiTarefaServProc( tarefa, instanteOcupacaoServidor, estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
								 break;
					   
					   case 1:   tarefa.setIdServidorAtual( tarefa.getIdServidorAtual() );
							     retornoFuncao = csTemp.atribuiTarefaServProc( tarefa, instanteOcupacaoServidor, estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
								 break;
				   }
               }
            }
        }			
	    return retornoFuncao;
	}

	public Double entradaTarefaServProc( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
	    
		if( tarefa.getTtl() <= 1 )
		{  for( CentrosDeServico csTemp : centroDeServicos )
		   {   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		       {   switch( csTemp.getTipo() )
			       {   
					   case 0:   tarefa.setIdServidorAtual( 0 );
							     retornoFuncao = csTemp.atribuiTarefaServProc( tarefa, estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
								 
								 break;
					   case 1:   tarefa.setIdServidorAtual( tarefa.getIdServidorAtual() );
							     retornoFuncao = csTemp.atribuiTarefaServProc( tarefa, estatRedeDeFilas.descobreServidorCS( csTemp.getIdCS() ) );
								 break;
				   }
               }
            }
        }			
	    return retornoFuncao;
	}
	
	public Double envioTarefa( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.envioTarefa( tarefa );
            }
		}

		return retornoFuncao;
	}

	public Double processamentoTarefa( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
	    
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.processamentoTarefa( tarefa );
            }
		}
        tarefa.setFinalizada();
		System.out.printf("Finalizacao da tarefa %d\n", tarefa.getIdTarefa() );
	
		return retornoFuncao;
	}
	
	public Double saidaTarefaServComunHopIntermediario( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
	    CentrosDeServico csTempAux = null;
		// O for abaixo "desliga" a tarefa do servidor e o servidor da tarefa
		tarefa.decrementaTtl();
		
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.saidaTarefaServidor( tarefa );
			    csTempAux = csTemp;
            }					
		}

		//O if abaixo "prepara" a tarefa para entrar em seu proximo hop (no caso, um intermediario).
		//Essa preparacao e  feita "setando-se" os novos valores nas devidas variaveis
		int indiceProximoHop = tarefa.getDistanciaProximoMestre() - tarefa.getTtl();
		if( tarefa.getTtl() >= 1 )
		{   if( csTempAux.getTipo() == 2 || csTempAux.getTipo() == 3 )  			
		    {  tarefa.setIdCSAtual( tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) );
		       tarefa.setIdServidorAtual( 0 );  
		    }
		
		}
		return retornoFuncao;
	}

	public Double saidaTarefaServComunHopFinal( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
        CentrosDeServico csTempAux = null;
        CentrosDeServico csTempProx = null;
		tarefa.decrementaTtl();
		// O for abaixo "desliga" a tarefa do servidor e o servidor da tarefa	
	    for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.saidaTarefaServidor( tarefa );
            }					
		}

		//O if abaixo "prepara" a tarefa para entrar em seu proximo hop (no caso, o final).
		//Essa preparacao e  feita "setando-se" os novos valores nas devidas variaveis
		if( tarefa.getTtl() <= 1 )
		{   
		    for( CentrosDeServico csTemp : centroDeServicos )
		    {   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		        {   //System.out.printf("tarefa.getIdCSAtual() = %d\n", tarefa.getIdCSAtual() );
				    csTempAux = csTemp;
					//System.out.printf("csTempAux = %d\n", csTempAux.getIdCS() );
                }					
		    }
            /*
			   Esse if tem que ser feito, pois como ha dois tipos de mestre - (um de cluster de
CS tipo 1 e outro de cluster de maquinas (CSs tipo 0 para processamento e CSs tipo 2 ou 3 para rede) ) -,
a saida da tarefa de um mestre pode, tambm, ocorrer de duas formas:
            - Se o mestre e de um CS tipo 1, a tarefa sai do mestre, mas continua no CS, ou seja
			nao se executa incrementaIndiceProximoHop() na funcao saidaTarefaMestre() (no trecho getTipoCS == 1)
			- Se o mestre e de um CS tipo 0, a tarefa sai do mestre E do CS, entao e necessario
			se executar incrementaIndiceProximoHop() na funcao saidaTarefaMestre() (no trecho getTipoCS == 0)
			Isso pode acarretar uma "defasagem" no indiceProximoHop que, se ocorrer, e desfeita pelo if abaixo.
			*/
		    int indiceProximoHop = tarefa.getDistanciaProximoMestre() - tarefa.getTtl();
		    for( CentrosDeServico csTemp : centroDeServicos )
		    {   if( csTemp.getIdCS() == tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) )
		        {   //System.out.printf("tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) = %d\n", tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) );
				    csTempProx = csTemp;
					//System.out.printf("csTempProx = %d\n", csTempProx.getIdCS() );
                }					
		    }
		    if( csTempAux.getTipo() == 2 || csTempAux.getTipo() == 3 )   			
    	    {  //Se o penultimo hop for CS tipo 2 ou 3, e necessario desfazer a defasagem 
		       tarefa.setIdCSAtual( tarefa.getElementoVetorCaminhoTarefa( indiceProximoHop ) );
			}
			else if( csTempAux.getTipo() == 1 )  //Se o CS final for tipo 1, a tarefa nao saiu do CS e nao e necessario desfazer a defasagem 
    		{
            }
            if( csTempProx.getTipo() == 0 && csTempProx.getEhEscalonador() == true )
            {   tarefa.inicializaTarefa();
            }		
			/*ATENCAO: perceber que o resultado do if abaixo sera diferente do resultado do if anterior
			pois, devido a preparacao comentada no comentario anterior, eu mudei, na linha acima,
			o IdCSAtual da tarefa! Antes, dessa mudanca a variavel continha o Id do CS antigo, 
			agora contem o id do proximo CS. Apos a "chamada de atencao anterior, e conveniente explicar
			a funcao/proposito do for/if abaixo. Para "setar" o servidor que a tarefa sera alocada e necessario
			saber se o servidor de processamento eh uma maquina (CS tipo 0) ou eh um no (CS tipo 1), pois
            se for uma maquina, o id deste servidor sera 0 (pois, apesar de ser um escravo, eh o unico 
			na maquina), se for de um cluster, o id do servidor estara armazenado na variavel idServidorDestino
            da tarefa.			
            */	
			for( CentrosDeServico csTemp : centroDeServicos )
			{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )  
		        {   if( csTemp.getTipo() == 0 ) //Se for maquina
                       tarefa.setIdServidorAtual( 0 );
                    else if( csTemp.getTipo() == 1 )
					{   tarefa.setIdServidorAtual( tarefa.getIdServidorDestino() );
						tarefa.setIdServidorDestino( -1 );
					}
                }
            }	
		}
		
		
		return retornoFuncao;
	}

	public Double saidaTarefaServidor( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
        
		tarefa.decrementaTtl();  
		// O for abaixo "desliga" a tarefa do servidor e o servidor da tarefa	
	    for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.saidaTarefaServidor( tarefa );
            }					
		}
		
		return retornoFuncao;
	}	

	public Double getTamanhoProcTarefaAtual( NoFila tarefa )
	{   Double retornoFuncao = 0.0;
        	
	    for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {   retornoFuncao = csTemp.getTamanhoProcTarefaAtual( tarefa );
            }					
		}
		
		return retornoFuncao;
	}	
	
	public int localizaCSanterior( NoFila tarefa )
	{   int retornoFuncao = 0;
	    int indiceIdCSAtual;
        System.out.printf("localizaCSanterior\n");
		if( tarefa.getVetorCaminhoTarefa() != null )
		{   System.out.printf("tarefa.getIdCSAtual() = %d\n", tarefa.getIdCSAtual());
		    for( indiceIdCSAtual = 0 ; indiceIdCSAtual < tarefa.getTamanhoVetorCaminhoTarefa() ; indiceIdCSAtual++ )
		    {   if( tarefa.getElementoVetorCaminhoTarefa(indiceIdCSAtual) == tarefa.getIdCSAtual() )
		           break;
		    
		    }
	        if( indiceIdCSAtual > 0 )
		    {   indiceIdCSAtual--;
			    return tarefa.getElementoVetorCaminhoTarefa( indiceIdCSAtual );
		    }
		    else
		    {   return tarefa.getIdCSAtual();
		    }	        
		}
        return tarefa.getIdCSAtual();		
	}

    public Double getTServicoProcto( NoFila tarefa )
	{	Double retornoFuncao = 0.0;
	    for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == tarefa.getIdCSAtual() )
		    {  retornoFuncao = csTemp.getTServicoProcto( tarefa );
			}
		}
		return retornoFuncao;
	}

	public void instanciaMatrizVetor( int tam_matriz )
	{  this.tam_mat = tam_matriz; 
	   matrizRF = new int[tam_mat][tam_mat];
	}
	
    public void inteligaCSs( int origem, int destino )
	{   matrizRF[origem][destino] = 1;
    }
 
    public void confereRF()
	{   int i, j, k;
	    int idOrigem = -1;
		int idDestino = -1;
   
		   System.out.println( "Impressao dos parametros de cada CS..." );
		   for( i = 0  ;  i <= numCS  ;  i++ )
		   {   for( CentrosDeServico csTemp : centroDeServicos )
	           {   if( csTemp.getIdCS() == i )
		           {  System.out.printf( "Dados do CS%d\n", csTemp.getIdCS() );
				      switch( csTemp.getTipo() )
					  {   case 0: System.out.printf( "Tipo: maquina\n" );
					              break;
					      case 1: System.out.printf( "Tipo: cluster\n" );
					              break;
						  case 2: System.out.printf( "Tipo: Conexao rede\n" );
								  break;
						  case 3: System.out.printf( "Tipo: Internet\n" );
								  break;							  
					  }
					  int vEscravos[] = new int[ csTemp.getNumEscravos() ]; 
					  if( csTemp.getNumEscravos() > 0 )
					  {   vEscravos = csTemp.getVetorEscravos();
					      System.out.printf( "Lista de escravos:\n" );
						  for( k = 0 ; k < csTemp.getNumEscravos() ; k++ )
						  {   System.out.printf( "%d ", vEscravos[k] );
						  }
						  System.out.printf( "\n" );
					  }
					  System.out.printf( "Numero Max Servidores: %d\n", csTemp.getNumMaxServidores() );
					  System.out.printf( "Numero de Servidores ja adicionados: %d\n", csTemp.getNumAtualServidores() );
					  if( csTemp.getTipo() == 1 ) //Se for cluster
					  {   System.out.printf( "tServico Total %f\n", csTemp.getTServicoProctoTotal() );
					  }
					  csTemp.confereCS();
					  System.out.printf( "Numero Servidores Livres: %d\n", csTemp.getNumServidoresLivres() );
					  System.out.printf( "Numero Max Filas: %d\n", csTemp.getNumMaxFilas() );
					  System.out.printf( "Numero de Filas ja adicionadas: %d\n", csTemp.getNumAtualFilas() );
					  System.out.printf( "\n" );
				   }
		       }
		   }
		   System.out.println( "Impressao da matriz..." );
		   System.out.println( "tam_mat - "+tam_mat );
		   System.out.println( "matrizRF.length - "+matrizRF.length );
	       for ( i = 0  ;  i < matrizRF.length  ;  i++ )      
           {   for ( j = 0  ;  j < matrizRF[i].length  ;  j++ ) 
			   {   System.out.print( matrizRF[i][j] + " " );   
			   }
			   System.out.println( );
		   }
		   System.out.println();
	       System.out.println( "Ligacoes" );
		   for ( i = 0; i < matrizRF.length; i++ )      
           {   for ( j = 0; j < matrizRF[i].length ; j++ ) 
			   {   if( matrizRF[i][j] == 1 )
                   {   for( CentrosDeServico csTemp : centroDeServicos )
	                   {   if( csTemp.getIdCS() == i )
		                   {   idOrigem = csTemp.getIdCS();
		                   }
	                       if( csTemp.getIdCS() == j )
		                   {   idDestino = csTemp.getIdCS();
			               }
		               }
				       System.out.printf( "O CS%d tem ligacao com o CS%d\n", idOrigem, idDestino );   
                   }				   
			   }
		   }		   
    }
	
	public int[] Dijkstra( int or, int dest )
   	{   int origem = or;
	    int destino = dest;
		int recente;
		int rotulo;
		int i;
		int j;
		int y;
		
		boolean fim[] = new boolean[tam_mat];
		int distancia[] = new int[tam_mat];
		int soTemporarios[] = new int[tam_mat];
		int precedentes[] = new int[tam_mat];
		
		for( i = 0; i < tam_mat ; i++ )
        {   distancia[i] = Integer.MAX_VALUE;;
		    fim[i] = false;
			precedentes[i] = -1;
        }
        distancia[origem] = 0;
        fim[origem] = true;		
		recente = origem;
		
		while ( fim[destino] == false )
        {   for( j = 0 ; j < tam_mat ; j++ )
			{   if( matrizRF[recente][j] != 0 && matrizRF[recente][j] != Integer.MAX_VALUE && (fim[j] == false) )
			    {   
				    rotulo = distancia[recente] + matrizRF[recente][j];
                    if( rotulo < distancia[j] )
                    {   distancia[j] = rotulo;
					    precedentes[j] = recente;
                    }					
				}
			}

			for( i = 0 ; i < tam_mat ; i++ )
               if( fim[i] == true )
                  soTemporarios[i] = Integer.MAX_VALUE;
               else
                  soTemporarios[i] = distancia[i];			   
			
			y = indiceMenorRotulo( soTemporarios, tam_mat );
			
			fim[y] = true;
			recente = y;
        }
        
       return precedentes;	
       		   
	}
	
	public int indiceMenorRotulo( int dist[], int tam_mat )
	{   int retorno = -12;
	    int i;
		int menor;
		
		menor = Integer.MAX_VALUE;
		for( i = 0; i < tam_mat ; i++ )
		{   if( (dist[i] != Integer.MAX_VALUE ) && dist[i] < menor )
		    {   menor = dist[i];
	            retorno = i;
			}
		}
	    return retorno;
	}
	public int[] determinaCaminho( int csOrigem, int csDestino, int vetPrecedentes[] )
	{   int i = csDestino;  //Comeca a minerar de tras para frente, ou seja, do destino para a origem
	    int j = 0;
		int caminhoInverso[] = new int [tam_mat];
		int numHops;
		
		caminhoInverso[j] = csDestino;
		j++;
	    while( vetPrecedentes[i] != -1 )
	    {   caminhoInverso[j] = vetPrecedentes[i];
		    j++;
			i = vetPrecedentes[i];
		}
		
		numHops = j;
		int retornoFuncao[] = new int [numHops];
		for( i = numHops-1, j = 0 ; i >= 0 ; i--, j++ )
        {   retornoFuncao[j] = caminhoInverso[i];
        }
        
        return retornoFuncao;		
	}
	
	public void setNumEscravos( int nEscravos, int idCs )
	{   for(CentrosDeServico temp:centroDeServicos){
			if(temp.getIdCS() == idCs){
				temp.setNumEscravos(nEscravos);
				break;
			}
		}
	}
	
	public void setVetorEscravos( int vetEscravos[], int idCs  )
	{	for(CentrosDeServico temp:centroDeServicos){
			if(temp.getIdCS() == idCs){
				temp.setVetorEscravos(vetEscravos);
				break;
			}
		}
	}
	
	public int getNumCs(){
		return numCS;
	}
	
	public List<Integer> getListaEscalonadores(){
		return escalonadores;
	}
	
	public Estatistica getEstatistica( ){
		return estatRedeDeFilas;
	}

	public int getTipoCS( int idCS )
	{	for( CentrosDeServico csTemp : centroDeServicos )
	    {   if( csTemp.getIdCS() == idCS )
				return csTemp.getTipo();
		}
		return -1;
	}
	
	public double valorServ( int cs, int id )
	{	double retorno = 0.0;
		for( CentrosDeServico csTemp : centroDeServicos )
		{   if( csTemp.getIdCS() == cs )
		    {	retorno = csTemp.valorServidor( id );
				break;
			}
		}
		return retorno;
	}

}
