/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor;

import java.util.PriorityQueue;

/** @author Camila
 */
public class HardwareFaults {

    public static void FIHardware(int OHH) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private double time = 0;
    private PriorityQueue<FutureEvent> eventos;
}
    //Recebe as informações do botão "Simular do JPrincipal"
    
    /*JPrincipal jprincipal = new JPrincipal();
    private DesenhoGrade aDesenho = null;
    private int tipoModelo; //define se o modelo é GRID, IAAS ou PAAS;
    this.

    public int getTipoModelo() {
        return tipoModelo;
    }
            
    JSimulacao janelaSimulacao = new JSimulacao(this, true, aDesenho.getGrade(), aDesenho.toString(), palavras, tipoModelo);
    
    JSimulacao janelaSimulacao1 = new JSimulacao();
    janelaSimulacao1.iniciarSimulacao();
    janelaSimulacao1.setLocationRelativeTo(this);
    janelaSimulacao1.setVisible(true);
        
    public HardwareFaults(ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas, List<Tarefa> tarefas) throws IllegalArgumentException {
        super(janela, redeDeFilas,tarefas);
        this.time = 0;
        this.eventos = new PriorityQueue<EventoFuturo>();
        
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            VMM temp = (VMM) mst;
            MestreCloud aux = (MestreCloud) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            aux.setSimulacao(this);
            temp.setSimulacaoAlloc(this);
            //Encontra menor caminho entre o mestre e seus escravos
            System.out.println("Mestre " + mst.getId() + " escontrando seus escravos");
            
            mst.determinarCaminhos(); //mestre encontra caminho para seus escravos
        }
        
        
    }
    public static void FIHardware(int OmissaoHardware) {
        //public static void main(String[] args){
        //declaração das variáveis locais
               
        //Criação de um instância para a classe JSelecionarFalhas
        JSelecionarFalhas sf = new JSelecionarFalhas();
                                   
        //Expectativa: Acesso ao valor do objeto da instância da classe
        //X Armazena o valor do objeto da classe
        
        if(OmissaoHardware==1){
           //Confirmação do tipo de falha selecionada: Falha por omissão de hardware
           JOptionPane.showMessageDialog(null, "Falha de Omissão de hardware selecionada.");
           JOptionPane.showMessageDialog(null, "Mapeando máquinas da simulação");
           //acessar simulação
                 Então mapear a quantidade de hardwares existentes 
             (qtd_maquinas): classe CS_VMM
            sortear um deles (random) e desabilitar um deles
            simulando assim uma falha de hardware por sorteio
            fonte:     
            CS_VMM maquinasSimuladas = new CS_VMM(id, proprietario, X, X, X, X, Escalonador, Alocador);
            
            //Acessar ArrayList já existente para mapear a quantidade de hardwares existentes
             
            CS_VMM maquinasVirtuais = new CS_VMM(id, proprietario, X, X, X, X, Escalonador, Alocador);
            
            
            List<ispd.motor.filas.servidores.CS_Processamento> maquinasVirtuais = alocadorVM.getMaquinas();
            
            List maquinasSimuladas = new ArrayList(); //
            for (int i = 1; i <= maquinasSimuladas.size(); i++){
                maquinasSimuladas.add(i); // Inicializa o arraylist com as

            Collections.shuffle(maquinasSimuladas); //Embaralha o ArrayList

            //Imprime a posição do ArryList = mS= máquina Simulada
            //(da máquina virutal a ser desligada)
                for (int mS = 0; mS < 1; i++){
                    System.out.println(maquinasSimuladas.get(i));
                }//for (int i = 0; i < 1; i++){
            }//for (int i = 1; i <= maquinasSimuladas.size(); i++){
            
        List<CS_Processamento> escravos = alocadorVM.getMaquinasFisicas(); //lista de maquinas fisicas
            //Instancia objetos
            ArrayList<List> caminhoEscravo = new ArrayList<List>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            caminhoEscravo.add(i, CS_VMM.getMenorCaminho(this, escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }

        alocadorVM.setCaminhoMaquinas(caminhoEscravo);
        escalonador.setMaqFisicas(escravos);
        escalonador.setCaminhoMaquinas(caminhoEscravo);
           JOptionPane.showMessageDialog(null, "Inserindo falha de omissão de hardware");
           
           
        }
        else{
            JOptionPane.showMessageDialog(null, "Outra falha selecionada");
            }
        
        
      //  }//if (X==1)
        
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    
    
}
*/