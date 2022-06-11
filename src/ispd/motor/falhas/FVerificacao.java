/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.falhas;

/**
 *
 * @author Camila
 * Classe destinada a verificar se o computador possui poder computacional 
 * para a simulação e se a simulação:
 * se ((numeroDeMaquinas >0) &&
 *     (numeroDeNos>0) &&
 *     (numerodeNos <=(numeroDeMaquinas - 1 * numeroDeMaquinas))
 */
/*
public class Verificacao extends CS_Processamento  {
  /*  private List<CS_Comunicacao> conexoesEntrada;
    private List<CS_Comunicacao> conexoesSaida;
    private EscalonadorCloud escalonador;
    private Alocacao alocadorVM;
    private List<Tarefa> filaTarefas;
    private boolean vmsAlocadas;
    private boolean escDisponivel;
    private boolean alocDisponivel;
    private int tipoEscalonamento;
    private int tipoAlocacao;
    private List<CS_VirtualMac> maquinasVirtuais;
*/
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
   /* private List<List> caminhoEscravo;
    private List<List> caminhoVMs;
    private Simulacao simulacao;

    public Verificacao(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao, int numeroMaquina) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, numeroMaquina);
    }*/
//}
//public class Verificacao extends CS_Processamento
    //Teste 1: Imaginar para determinar falhas by Camila
    /*public void determinarFalhas() throws LinkageError {
        List<CS_Processamento> escravos = alocadorVM.getMaquinasFisicas(); //lista de maquinas fisicas
        //numero de nós = PoderComputacional
        Lis<CS_Processamento> escravosIntermediario =alocadorVM.getMaquinasFisicas();
        Lista<CS_Processamento> escravosFinal = escavosIntermediario * escravos;

        //Verificação do poder computacional
        if ((numeroProcessadores > 0) && (PoderComputacional > 0) && (PoderComputacional <= escravosFinal)){
            System.out.println("Há poder computacional para a simulação");
            System.out.println("Listagem de máquinas físicas: "+escravos);
            //Instancia objetos
           // caminhoEscravo = new ArrayList<List>(escravos.size());
            //Busca pelo melhor caminho
           // for (int i = 0; i < escravos.size(); i++) {
                caminhoEscravo.add(i, CS_VMM.getMenorCaminho(this, escravos.get(i)));
            }//for
            //verifica se todos os escravos são alcansaveis
            for (int i = 0; i < escravos.size(); i++) {
                if (caminhoEscravo.get(i).isEmpty()) {
                    throw new LinkageError();
                }//
                else{
                    alocadorVM.setCaminhoMaquinas(caminhoEscravo);
                    escalonador.setMaqFisicas(escravos);
                    escalonador.setCaminhoMaquinas(caminhoEscravo);
                }

            }
        }//if
      //  else
        //    System.out.println("Não Há poder computacional");
}//public void determinarFalhas()

    

    /*@Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        throw new UnsupportedOperationException("Erros na 'chegadaDeCliente'."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        throw new UnsupportedOperationException("Erros no 'atendimento'."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        throw new UnsupportedOperationException("Erros na 'saidaDeCliente'."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        throw new UnsupportedOperationException("Erros na 'requisicao'."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getConexoesSaida() {
        throw new UnsupportedOperationException("Erros no 'getConexoesSaida'."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getCargaTarefas() {
        throw new UnsupportedOperationException("Erros na 'getCargaTarefass'."); //To change body of generated methods, choose Tools | Templates.
    }*/
//}