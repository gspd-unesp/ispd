/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

import ispd.alocacaoVM.VMM;
import ispd.escalonador.Mestre;
import ispd.escalonadorCloud.MestreCloud;
import ispd.motor.filas.Cliente;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import ispd.motor.falhas.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import ispd.gui.JSelecionarFalhas;
import javax.swing.JOptionPane;
import ispd.alocacaoVM.Alocacao;
import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.filas.servidores.CS_Comunicacao;
import static ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud.DESLIGADO;
import ispd.motor.falhas.FIHardware;
import ispd.motor.metricas.MetricasAlocacao;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author denison_usuario
 */
public class SimulacaoSequencialCloud extends Simulacao {

    private double time = 0;
    private EscalonadorCloud escalonador;//Camila
    private PriorityQueue<EventoFuturo> eventos;
    private ArrayList<CS_VirtualMac> maquinasVirtuais;
    private LinkedList<CS_Processamento> maquinasFisicas;
    private ArrayList<CS_VirtualMac> VMsRejeitadas;
    private List<List> caminhoVMs; //Camila criou para referenciar SimulacaoSequencialCloud
    
    public SimulacaoSequencialCloud(ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas, List<Tarefa> tarefas) throws IllegalArgumentException {
        super(janela, redeDeFilas,tarefas);
        this.time = 0;
        this.eventos = new PriorityQueue<EventoFuturo>();

        if (redeDeFilas == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (redeDeFilas.getMestres() == null || redeDeFilas.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (redeDeFilas.getLinks() == null || redeDeFilas.getLinks().isEmpty()) {
            janela.println("The model has no Networks.", Color.orange);
        }else if (redeDeFilas.getVMs() == null || redeDeFilas.getVMs().isEmpty())
            janela.println("The model has no virtual machines configured.", Color.orange);
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
        
        janela.print("Creating routing.");
        janela.print(" -> ");
        
        /**
         * Trecho de código que implementa o roteamento entre os mestres e os seus respectivos escravos
         */
        System.out.println("---------------------------------------");
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            VMM temp = (VMM) mst;
            MestreCloud aux = (MestreCloud) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            aux.setSimulacao(this);
            temp.setSimulacaoAlloc(this);
            //Encontra menor caminho entre o mestre e seus escravos
            System.out.println("Mestre " + mst.getId() + " encontrando seus escravos");
            mst.determinarCaminhos(); //mestre encontra caminho para seus escravos
        }
        
        janela.incProgresso(5);
        janela.println("OK", Color.green);
        
        //--------------- Injeção de falhas
        //By Camila
        /*Injetando as falhas:
        verifica qual checkbox foi clicado quando escolheu a falha e executa*/
        //Injetar falhar de Omissão de Hardware: delisgar uma máquina física
        JSelecionarFalhas selecionarFalhas = new JSelecionarFalhas();
        
       if (selecionarFalhas!=null){
        //-----------Injeção da Falha de Omissão de Hardware --------   
            if (selecionarFalhas.cbkOmissaoHardware != null){
                janela.println("There are injected hardware omission failures.");
                janela.println("Creating Hardware fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                FIHardware fihardware = new FIHardware();
                fihardware.FIHardware1(janela, redeDeFilas, tarefas);
        }         
        else
            janela.println("There aren't injected hardware omission failures.");
        
        //-----------Injeção da  Falha de Omissão de Software --------   
            if (selecionarFalhas.cbkOmissaoSoftware!= null){
                janela.println("There are injected software omission failures.");
                janela.println("Creating software fault.");
                janela.println("Software failure created.");
                //ir para ispd.motor.falhas.FISoftware.java
                FISoftware fisoftdware = new FISoftware();
                fisoftdware.FISfotware1(janela, redeDeFilas, tarefas);
        }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){        
        else
            janela.println("There aren't injected software omission failures.");
       
       //-----------Injeção da  Falha de Negação de serviço --------   
            if (selecionarFalhas.cbxNegacaoService!= null){
                janela.println("There are injected denial of service failures.");
                janela.println("Creating Denial of service fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIDenialService.java
                FIDenialService negacaoServico = new FIDenialService();
                
        }//if (selecionarFalhas.cbxNegacaoService.isSelected()){        
        else
            janela.println("There aren't injected denial of service failures.");
       
        //-----------Injeção da  Falha de HD Cheio --------   
        if (selecionarFalhas.cbxHDCheio!= null){
            janela.println("There are injected Full HD failures.");
            janela.println("Creating Full HD fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIHardware.java
            FIFullHD HDCheio = new FIFullHD();
                
        }//if (selecionarFalhas.cbkHDCheio.isSelected()){        
        else
            janela.println("There aren't injected Full HD failures.");
       
        //-----------Injeção da  Falha de Estado --------   
        if (selecionarFalhas.cbxEstado!= null){
            janela.println("There are injected State failures.");
            janela.println("Creating state fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FState.java
            FState state = new FState();
        }//if (selecionarFalhas.cbxEstado.isSelected()){        
        else
            janela.println("There aren't injected State failures.");
       
        //-----------Injeção da  Falha de Sobrecarga de Tempo --------   
        if (selecionarFalhas.cbxSobrecargaTempo!= null){
            janela.println("There are injected time overload failures.");
            janela.println("Creating time overload fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIOverload.java
            FIOverload overload = new FIOverload();
            
        }//if (selecionarFalhas.cbxSobrecargaTempo.isSelected()){        
        else
            janela.println("There aren't injected time overload failures.");
       
        //-----------Injeção da  Falha de Interdependencia --------   
        if (selecionarFalhas.cbxInterdependencia!= null){
            janela.println("There are injected interdependencies failures.");
            janela.println("Creating interdependencie fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIInterdependencie.java
            FIInterdependencie fiInterdependencia = new FIInterdependencie();
                
        }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){        
        else
            janela.println("There aren't injected interdependencies failures.");
            
        //-----------Injeção da  Falha de Incompatibilidade --------   
        if (selecionarFalhas.cbxIncompatibilidade!= null){
            janela.println("There are injected Incompatibility failures.");
            janela.println("Creating Incompatibility fault.");
            janela.println("Development fault.");//ir para ispd.motor.falhas.FIHardware.java
            FIIncompatibility fiIncompatibility = new FIIncompatibility();
                
        }//if (selecionarFalhas.cbxIncompatibilidade.isSelected()){        
        else
            janela.println("There aren't injected Incompatibility failures.");
            
            //-----------Injeção de Falhas Pemanentes --------   
            if (selecionarFalhas.cbxFPermanentes!= null){
                janela.println("There are injected permanents failures.");
                janela.println("Creating permanents fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIPermanent.java
                FIPermanent fiPermanent = new FIPermanent();
                
        }//if (selecionarFalhas.cbxFPermanentes.isSelected()){        
        else
            janela.println("There aren't injected permanents failures.");
            
        //-----------Injeção da  Falha de Desenho incorreto --------   
        if (selecionarFalhas.cbxDesenhoIncorreto!= null){
            janela.println("There are injected bad design failures.");
            janela.println("Creating bad design fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIHardware.java
            FIBadDesign fibadesign = new FIBadDesign();
                
        }//if (selecionarFalhas.cbxDesenhoIncorreto.isSelected()){        
        else
            janela.println("There aren't injected bad design failures.");
       
        //-----------Injeção da  Falha Precosse --------   
            if (selecionarFalhas.cbxPrecoce!= null){
                janela.println("There are injected early failures.");
                janela.println("Creating early fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIEarly.java
                FIEarly fiearly = new FIEarly();
                
        }//if (selecionarFalhas.cbxPrecoce.isSelected()){        
        else
            janela.println("There aren't injected early failures.");

//-----------Injeção da  Falha de Tardia --------   
            if (selecionarFalhas.cbxTardia!= null){
                janela.println("There are injected late failures.");
                janela.println("Creating late fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FILate.java
                FILate fiTardia = new FILate();
                
        }//if (selecionarFalhas.cbxTardia.isSelected()){        
        else
            janela.println("There aren't injected late failures.");
       
        //-----------Injeção da  Falha Transiente --------   
        if (selecionarFalhas.cbxTransiente!= null){
            janela.println("There are injected transient failures.");
            janela.println("Creating transient failure.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIHardware.java
            FITransient fitransient = new FITransient();
                
        }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){        
        else
            janela.println("There aren't injected transient failures.");
      
       }//if (selecionarFalhas!=null)
       else 
            janela.println("There aren't selected faults.");
         if (redeDeFilas.getMaquinasCloud() == null || redeDeFilas.getMaquinasCloud().isEmpty()) {
            janela.println("The model has no phisical machines.", Color.orange);
        } else {
             System.out.println("---------------------------------------");
            for (CS_MaquinaCloud maq : redeDeFilas.getMaquinasCloud()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.setStatus(CS_MaquinaCloud.LIGADO);
                maq.determinarCaminhos();//escravo encontra caminhos para seu mestre
                //System.out.println("Maquina " + maq.getId() + " encontrando seus mestres");
            }
        }
        //fim roteamento
        janela.incProgresso(5);
    }
    
    @Override
    public void simular() {
        //inicia os escalonadores
         System.out.println("---------------------------------------");
        iniciarEscalonadoresCloud();
         System.out.println("---------------------------------------");
        
        iniciarAlocadoresCloud();
         System.out.println("---------------------------------------");
        addEventos(this.getTarefas());
         System.out.println("---------------------------------------");
        
        
        if (atualizarEscalonadores()) {
            realizarSimulacaoAtualizaTime();
        } else {
            realizarSimulacao();
        }
        
        desligarMaquinas(this, this.getRedeDeFilasCloud());
        getJanela().incProgresso(30);
        getJanela().println("Simulation completed.", Color.green);
    }
    
    public void addEventos(List<Tarefa> tarefas) {
        /*for (CS_VirtualMac vm : VMs){
            EventoFuturo evt = new EventoFuturo(0.0, EventoFuturo.CHEGADA, vm.getVmmResponsavel(), vm);
            eventos.add(evt);
        }*/
        System.out.println("Tarefas sendo adicionadas na lista de eventos futuros");
        for (Tarefa tarefa : tarefas) {
            EventoFuturo evt = new EventoFuturo(tarefa.getTimeCriacao(), EventoFuturo.CHEGADA, tarefa.getOrigem(), tarefa);
            eventos.add(evt);
        }
    }

    @Override
    public void addEventoFuturo(EventoFuturo ev) {
        eventos.offer(ev);
    }

    @Override
    public boolean removeEventoFuturo(int tipoEv, CentroServico servidorEv, Cliente clienteEv) {
        //remover evento de saida do cliente do servidor
        java.util.Iterator<EventoFuturo> interator = this.eventos.iterator();
        while (interator.hasNext()) {
            EventoFuturo ev = interator.next();
            if (ev.getTipo() == tipoEv
                    && ev.getServidor().equals(servidorEv)
                    && ev.getCliente().equals(clienteEv)) {
                this.eventos.remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime(Object origem) {
        return time;
    }

    private boolean atualizarEscalonadores() {
        for (CS_Processamento mst : getRedeDeFilasCloud().getMestres()) {
            CS_VMM mestre = (CS_VMM) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                return true;
            }
        }
        return false;
    }

    
    private void realizarSimulacao() {
        while (!eventos.isEmpty()) {
        //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            EventoFuturo eventoAtual = eventos.poll();
            time = eventoAtual.getTempoOcorrencia();
            switch (eventoAtual.getTipo()) {
                case EventoFuturo.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.SAÍDA:
                    eventoAtual.getServidor().saidaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ESCALONAR);
                    break;
                case EventoFuturo.ALOCAR_VMS:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ALOCAR_VMS);
                    break;
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getCliente(), eventoAtual.getTipo());
                    break;
            }
        }
    }

    /**
     * Executa o laço de repetição responsavel por atender todos eventos da
     * simulação, e adiciona o evento para atualizar os escalonadores.
     */
    private void realizarSimulacaoAtualizaTime() {
        List<Object[]> Arrayatualizar = new ArrayList<Object[]>();
        for (CS_Processamento mst : getRedeDeFilas().getMestres()) {
            CS_Mestre mestre = (CS_Mestre) mst;
            if (mestre.getEscalonador().getTempoAtualizar() != null) {
                Object[] item = new Object[3];
                item[0] = mestre;
                item[1] = mestre.getEscalonador().getTempoAtualizar();
                item[2] = mestre.getEscalonador().getTempoAtualizar();
                Arrayatualizar.add(item);
            }
        }
        while (!eventos.isEmpty()) {
            //recupera o próximo evento e o executa.
            //executa estes eventos de acordo com sua ordem de chegada
            //de forma a evitar a execução de um evento antes de outro
            //que seria criado anteriormente
            for (Object[] ob : Arrayatualizar) {
                if ((Double) ob[2] < eventos.peek().getTempoOcorrencia()) {
                    CS_Mestre mestre = (CS_Mestre) ob[0];
                    for (CS_Processamento maq : mestre.getEscalonador().getEscravos()) {
                        mestre.atualizar(maq, (Double) ob[2]);
                    } 
                    ob[2] = (Double) ob[2] + (Double) ob[1];
                }
            }
            EventoFuturo eventoAtual = eventos.poll();
            time = eventoAtual.getTempoOcorrencia();
            switch (eventoAtual.getTipo()) {
                case EventoFuturo.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.SAÍDA:
                    eventoAtual.getServidor().saidaDeCliente(this, (Tarefa) eventoAtual.getCliente());
                    break;
                case EventoFuturo.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ESCALONAR);
                    break;
                    case EventoFuturo.ALOCAR_VMS:
                    eventoAtual.getServidor().requisicao(this, null, EventoFuturo.ALOCAR_VMS);
                    break;
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getCliente(), eventoAtual.getTipo());
                    break;
            }
        }
        
        
    }

    private void desligarMaquinas(Simulacao simulacao, RedeDeFilasCloud rdfCloud) {
        for(CS_MaquinaCloud aux : rdfCloud.getMaquinasCloud()){
            aux.desligar(simulacao);
            
        }
        
    }
}