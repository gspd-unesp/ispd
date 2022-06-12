/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ispd.motor;

import ispd.alocacaoVM.VMM;
import ispd.escalonadorCloud.MestreCloud;
import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
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
import ispd.escalonadorCloud.EscalonadorCloud;
import ispd.motor.falhas.FIHardware;
import ispd.motor.metricas.MetricasGlobais;

import java.util.LinkedList;

/**
 *
 * @author denison_usuario
 */
public class SimulacaoSequencialCloud extends Simulation {

    private double time = 0;
    private EscalonadorCloud escalonador;//Camila
    private PriorityQueue<FutureEvent> eventos;
    private ArrayList<CS_VirtualMac> maquinasVirtuais;
    private LinkedList<CS_Processamento> maquinasFisicas;
    private ArrayList<CS_VirtualMac> VMsRejeitadas;
    private List<List> caminhoVMs; //Camila criou para referenciar SimulacaoSequencialCloud
    
    public SimulacaoSequencialCloud(ProgressoSimulacao janela, RedeDeFilasCloud redeDeFilas, List<Tarefa> tarefas) throws IllegalArgumentException {
        super(janela, redeDeFilas,tarefas);
        this.time = 0;
        this.eventos = new PriorityQueue<FutureEvent>();

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
        //Injetar falhar de Omissão de Hardware: desligar uma máquina física
        JSelecionarFalhas selecionarFalhas = new JSelecionarFalhas();
        
        
       if (selecionarFalhas.isActive()){
        //-----------Injeção da Falha de Omissão de Hardware --------   
            if (selecionarFalhas.cbkOmissaoHardware != null){
                janela.println("There are injected hardware omission failures.");
                janela.println("Creating Hardware fault.");
                //ir para ispd.motor.falhas.FIHardware.java
                FIHardware fihardware = new FIHardware();
                fihardware.FIHardware1(janela, redeDeFilas, tarefas);
        }         
        else{
            janela.println("There aren't injected hardware omission failures.");
        }
        //-----------Injeção da  Falha de Omissão de Software --------   
            if (selecionarFalhas.cbkOmissaoSoftware != null){
                janela.println("There are injected software omission failures.");
                janela.println("Creating software fault.");
                janela.println("Software failure created.");
                //ir para ispd.motor.falhas.FISoftware.java
                FISoftware fisoftdware = new FISoftware();
                fisoftdware.FISfotware1(janela, redeDeFilas, tarefas);
        }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){        
        else{
            janela.println("There aren't injected software omission failures.");
            }
       //-----------Injeção da  Falha de Negação de serviço --------   
            if (selecionarFalhas.cbxNegacaoService!= null){
                janela.println("There are injected denial of service failures.");
                janela.println("Creating Denial of service fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIDenialService.java
                FIDenialService negacaoServico = new FIDenialService();
                
        }//if (selecionarFalhas.cbxNegacaoService.isSelected()){        
        else{
            janela.println("There aren't injected denial of service failures.");
        }
        //-----------Injeção da  Falha de HD Cheio --------   
        if (selecionarFalhas.cbxHDCheio!= null){
            janela.println("There are injected Full HD failures.");
            janela.println("Creating Full HD fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIHardware.java
            FIFullHD HDCheio = new FIFullHD();
                
        }//if (selecionarFalhas.cbkHDCheio.isSelected()){        
        else{
            janela.println("There aren't injected Full HD failures.");
        }
        
         //-----------Injeção da  Falha de Valores -------- 
        if (selecionarFalhas.cbxValores != null){
            janela.println("There are injected Values failures.");
            janela.println("Creating value fault.");
            //ir para ispd.motor.falhas.FValue.java
            MetricasGlobais global = new MetricasGlobais();
            FIValue value = new FIValue();
            value.FIValue1(janela, redeDeFilas, global);
        }//if (selecionarFalhas.cbx.isSelected()){        
        else{
            janela.println("There aren't injected Value failures.");
        }
        
        //-----------Injeção da  Falha de Estado --------   
        if (selecionarFalhas.cbxEstado != null){
            janela.println("There are injected State failures.");
            janela.println("Creating state fault.");
            //ir para ispd.motor.falhas.FState.java
            FState state = new FState();
            state.FIState1(janela, redeDeFilas);
        }//if (selecionarFalhas.cbxEstado.isSelected()){        
        else{
            janela.println("There aren't injected State failures.");
        }
        //-----------Injeção da  Falha de Sobrecarga de Tempo --------   
        if (selecionarFalhas.cbxSobrecargaTempo!= null){
            janela.println("There are injected time overload failures.");
            janela.println("Creating time overload fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIOverload.java
            FIOverload overload = new FIOverload();
            
        }//if (selecionarFalhas.cbxSobrecargaTempo.isSelected()){        
        else{
            janela.println("There aren't injected time overload failures.");
        }
        //-----------Injeção da  Falha de Interdependencia --------   
        if (selecionarFalhas.cbxInterdependencia!= null){
            janela.println("There are injected interdependencies failures.");
            janela.println("Creating interdependencie fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIInterdependencie.java
            FIInterdependencie fiInterdependencia = new FIInterdependencie();
                
        }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){        
        else{
            janela.println("There aren't injected interdependencies failures.");
        }    
        //-----------Injeção da  Falha de Incompatibilidade --------   
        if (selecionarFalhas.cbxIncompatibilidade!= null){
            janela.println("There are injected Incompatibility failures.");
            janela.println("Creating Incompatibility fault.");
            janela.println("Development fault.");//ir para ispd.motor.falhas.FIHardware.java
            FIIncompatibility fiIncompatibility = new FIIncompatibility();
                
        }//if (selecionarFalhas.cbxIncompatibilidade.isSelected()){        
        else{
            janela.println("There aren't injected Incompatibility failures.");
        }
            //-----------Injeção de Falhas Pemanentes --------   
            if (selecionarFalhas.cbxFPermanentes!= null){
                janela.println("There are injected permanents failures.");
                janela.println("Creating permanents fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIPermanent.java
                FIPermanent fiPermanent = new FIPermanent();
                
        }//if (selecionarFalhas.cbxFPermanentes.isSelected()){        
        else{
            janela.println("There aren't injected permanents failures.");
        }
        //-----------Injeção da  Falha de Desenho incorreto --------   
        if (selecionarFalhas.cbxDesenhoIncorreto!= null){
            janela.println("There are injected bad design failures.");
            janela.println("Creating bad design fault.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIHardware.java
            FIBadDesign fibadesign = new FIBadDesign();
                
        }//if (selecionarFalhas.cbxDesenhoIncorreto.isSelected()){        
        else{
            janela.println("There aren't injected bad design failures.");
        }
        //-----------Injeção da  Falha Precosse --------   
            if (selecionarFalhas.cbxPrecoce!= null){
                janela.println("There are injected early failures.");
                janela.println("Creating early fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FIEarly.java
                FIEarly fiearly = new FIEarly();
                
        }//if (selecionarFalhas.cbxPrecoce.isSelected()){        
        else{
            janela.println("There aren't injected early failures.");
        }
//-----------Injeção da  Falha de Tardia --------   
            if (selecionarFalhas.cbxTardia!= null){
                janela.println("There are injected late failures.");
                janela.println("Creating late fault.");
                janela.println("Development fault.");
                //ir para ispd.motor.falhas.FILate.java
                FILate fiTardia = new FILate();
                
        }//if (selecionarFalhas.cbxTardia.isSelected()){        
        else{
            janela.println("There aren't injected late failures.");
        }
            
        //-----------Injeção da  Falha Transiente --------   
        if (selecionarFalhas.cbxTransiente!= null){
            janela.println("There are injected transient failures.");
            janela.println("Creating transient failure.");
            janela.println("Development fault.");
            //ir para ispd.motor.falhas.FIHardware.java
            FITransient fitransient = new FITransient();
                
        }//if (selecionarFalhas.cbkOmissaoSoftware.isSelected()){        
        else{
            janela.println("There aren't injected transient failures.");
        }
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
    public void simulate() {
        //inicia os escalonadores
         System.out.println("---------------------------------------");
        initCloudSchedulers();
         System.out.println("---------------------------------------");
        
        initCloudAllocators();
         System.out.println("---------------------------------------");
        addEventos(this.getJobs());
         System.out.println("---------------------------------------");
        
        
        if (atualizarEscalonadores()) {
            realizarSimulacaoAtualizaTime();
        } else {
            realizarSimulacao();
        }
        
        desligarMaquinas(this, this.getCloudQueueNetwork());
        getWindow().incProgresso(30);
        getWindow().println("Simulation completed.", Color.green);
    }
    
    public void addEventos(List<Tarefa> tarefas) {
        /*for (CS_VirtualMac vm : VMs){
            EventoFuturo evt = new EventoFuturo(0.0, EventoFuturo.CHEGADA, vm.getVmmResponsavel(), vm);
            eventos.add(evt);
        }*/
        System.out.println("Tarefas sendo adicionadas na lista de eventos futuros");
        for (Tarefa tarefa : tarefas) {
            FutureEvent evt = new FutureEvent(tarefa.getTimeCriacao(), FutureEvent.CHEGADA, tarefa.getOrigem(), tarefa);
            eventos.add(evt);
        }
    }

    @Override
    public void addFutureEvent(FutureEvent ev) {
        eventos.offer(ev);
    }

    @Override
    public boolean removeFutureEvent(int eventType, CentroServico eventServer, Client eventClient) {
        //remover evento de saida do cliente do servidor
        java.util.Iterator<FutureEvent> interator = this.eventos.iterator();
        while (interator.hasNext()) {
            FutureEvent ev = interator.next();
            if (ev.getType() == eventType
                    && ev.getServidor().equals(eventServer)
                    && ev.getClient().equals(eventClient)) {
                this.eventos.remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime(Object origin) {
        return time;
    }

    private boolean atualizarEscalonadores() {
        for (CS_Processamento mst : getCloudQueueNetwork().getMestres()) {
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
            FutureEvent eventoAtual = eventos.poll();
            time = eventoAtual.getCreationTime();
            switch (eventoAtual.getType()) {
                case FutureEvent.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, (Tarefa) eventoAtual.getClient());
                    break;
                case FutureEvent.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, (Tarefa) eventoAtual.getClient());
                    break;
                case FutureEvent.SAIDA:
                    eventoAtual.getServidor().saidaDeCliente(this, (Tarefa) eventoAtual.getClient());
                    break;
                case FutureEvent.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, FutureEvent.ESCALONAR);
                    break;
                case FutureEvent.ALOCAR_VMS:
                    eventoAtual.getServidor().requisicao(this, null, FutureEvent.ALOCAR_VMS);
                    break;
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getClient(), eventoAtual.getType());
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
        for (CS_Processamento mst : getQueueNetwork().getMestres()) {
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
                if ((Double) ob[2] < eventos.peek().getCreationTime()) {
                    CS_Mestre mestre = (CS_Mestre) ob[0];
                    for (CS_Processamento maq : mestre.getEscalonador().getEscravos()) {
                        mestre.atualizar(maq, (Double) ob[2]);
                    } 
                    ob[2] = (Double) ob[2] + (Double) ob[1];
                }
            }
            FutureEvent eventoAtual = eventos.poll();
            time = eventoAtual.getCreationTime();
            switch (eventoAtual.getType()) {
                case FutureEvent.CHEGADA:
                    eventoAtual.getServidor().chegadaDeCliente(this, (Tarefa) eventoAtual.getClient());
                    break;
                case FutureEvent.ATENDIMENTO:
                    eventoAtual.getServidor().atendimento(this, (Tarefa) eventoAtual.getClient());
                    break;
                case FutureEvent.SAIDA:
                    eventoAtual.getServidor().saidaDeCliente(this, (Tarefa) eventoAtual.getClient());
                    break;
                case FutureEvent.ESCALONAR:
                    eventoAtual.getServidor().requisicao(this, null, FutureEvent.ESCALONAR);
                    break;
                    case FutureEvent.ALOCAR_VMS:
                    eventoAtual.getServidor().requisicao(this, null, FutureEvent.ALOCAR_VMS);
                    break;
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getClient(), eventoAtual.getType());
                    break;
            }
        }
        
        
    }

    private void desligarMaquinas(Simulation simulacao, RedeDeFilasCloud rdfCloud) {
        for(CS_MaquinaCloud aux : rdfCloud.getMaquinasCloud()){
            aux.desligar(simulacao);
            
        }
        
    }
}