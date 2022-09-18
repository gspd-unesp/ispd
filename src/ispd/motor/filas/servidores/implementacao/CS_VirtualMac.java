/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasCusto;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public class CS_VirtualMac extends CS_Processamento implements Client, Mensagens {
    
    public static final int LIVRE = 1;
    public static final int ALOCADA = 2;
    public static final int REJEITADA = 3;
    public static final int DESTRUIDA = 4;
    
    //Lista de atributos
    private CS_VMM vmmResponsavel;
    private int processadoresDisponiveis;
    private double poderProcessamento;
    private double memoriaDisponivel;
    private double discoDisponivel;
    private double instanteAloc;
    private double tempoDeExec;
    private String OS;
    private CS_MaquinaCloud maquinaHospedeira;
    private List<CentroServico> caminho;
    private List<CentroServico> caminhoVMM;
    private List<List> caminhoIntermediarios;
    private int status;
    private List<Tarefa> filaTarefas;
    private List<Tarefa> tarefaEmExecucao;
    private List<CS_VMM> VMMsIntermediarios;
    private MetricasCusto metricaCusto;
    private List<Double> falhas = new ArrayList<Double>();
    private List<Double> recuperacao = new ArrayList<Double>();
    private boolean erroRecuperavel;
    private boolean falha = false;
    
    
    
    
    /**
     * @author Diogo Tavares
     * 
     * @param id
     * @param proprietario
     * @param PoderComputacional
     * @param numeroProcessadores
     * @param Ocupacao
     * @param numeroMaquina
     * @param memoria
     * @param disco 
     */ 
    
    public CS_VirtualMac(String id, String proprietario, int numeroProcessadores, double memoria, double disco, String OS) {
        super(id, proprietario, 0, numeroProcessadores, 0, 0);
        this.processadoresDisponiveis = numeroProcessadores;
        this.memoriaDisponivel = memoria;
        this.discoDisponivel = disco;
        this.OS = OS;
        this.metricaCusto = new MetricasCusto(id);
        this.maquinaHospedeira = null;
        this.caminhoVMM = null;
        this.VMMsIntermediarios = new ArrayList<CS_VMM>();
        this.caminhoIntermediarios = new ArrayList<List>();
        this.tempoDeExec = 0;
        this.status = LIVRE;
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
        this.filaTarefas = new ArrayList<Tarefa>();
    }

    
     @Override
    public void chegadaDeCliente(Simulation simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) { //se a tarefa estiver parada ou executando
            cliente.iniciarEsperaProcessamento(simulacao.getTime(this));
            if (processadoresDisponiveis != 0) {
                //indica que recurso está ocupado
                processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                FutureEvent novoEvt = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this,
                        cliente);
                simulacao.addFutureEvent(novoEvt);
            } else {
                filaTarefas.add(cliente);
            }
        }
    }

    @Override
    public void atendimento(Simulation simulacao, Tarefa cliente) {
        cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
        if(cliente == null)
            System.out.println("cliente nao existe");
        else
            System.out.println("cliente é a tarefa " + cliente.getIdentificador());
        tarefaEmExecucao.add(cliente);
        Double next = simulacao.getTime(this) + tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        if (!falhas.isEmpty() && next > falhas.get(0)) {
            Double tFalha = falhas.remove(0);
            if (tFalha < simulacao.getTime(this)) {
                tFalha = simulacao.getTime(this);
            }
            Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            FutureEvent evt = new FutureEvent(
                    tFalha,
                    FutureEvent.MENSAGEM,
                    this,
                    msg);
            simulacao.addFutureEvent(evt);
        } else {
            falha = false;
            //Gera evento para atender proximo cliente da lista
            FutureEvent evtFut = new FutureEvent(
                    next,
                    FutureEvent.SAIDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void saidaDeCliente(Simulation simulacao, Tarefa cliente) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        //Incrementa o tempo de processamento
        double tempoProc = this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
        tarefaEmExecucao.remove(cliente);
        //eficiencia calculada apenas nas classes CS_Maquina
        cliente.calcEficiencia(this.getPoderComputacional());
        //Devolve tarefa para o mestre
        
        CentroServico Origem = cliente.getOrigem();
        ArrayList<CentroServico> caminho;
        if(Origem.equals(this.vmmResponsavel)){
            caminho =  new ArrayList<CentroServico>(caminhoVMM);
            
        }else{
            System.out.println("A tarefa não saiu do vmm desta vm!!!!!");
            int index = VMMsIntermediarios.indexOf((CS_VMM) Origem);
            if(index == -1){
                CS_MaquinaCloud auxMaq = this.getMaquinaHospedeira();
                ArrayList<CentroServico> caminhoInter = new ArrayList<CentroServico>(getMenorCaminhoIndiretoCloud(auxMaq, (CS_Processamento) Origem));
                caminho = new ArrayList<CentroServico>(caminhoInter);
                VMMsIntermediarios.add((CS_VMM) Origem);
                int idx = VMMsIntermediarios.indexOf((CS_VMM) Origem);
                caminhoIntermediarios.add(idx, caminhoInter);
                
            }else{
                caminho = new ArrayList<CentroServico>(caminhoIntermediarios.get(index));
            }
        }
            cliente.setCaminho(caminho);
            System.out.println("Saida -"+ this.getId() +"- caminho size:" + caminho.size());
           
            //Gera evento para chegada da tarefa no proximo servidor
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        
        if (filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
        } else {
            //Gera evento para atender proximo cliente da lista
            Tarefa proxCliente = filaTarefas.remove(0);
            FutureEvent NovoEvt = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.ATENDIMENTO,
                    this, proxCliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(NovoEvt);
        }
    }

    @Override
    public void requisicao(Simulation simulacao, Mensagem mensagem, int tipo) {
        if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null && mensagem.getTarefa().getLocalProcessamento().equals(this)) {
                switch (mensagem.getTipo()) {
                    case Mensagens.PARAR:
                        atenderParada(simulacao, mensagem);
                        break;
                    case Mensagens.CANCELAR:
                        atenderCancelamento(simulacao, mensagem);
                        break;
                    case Mensagens.DEVOLVER:
                        atenderDevolucao(simulacao, mensagem);
                        break;
                    case Mensagens.DEVOLVER_COM_PREEMPCAO:
                        atenderDevolucaoPreemptiva(simulacao, mensagem);
                        break;
                    case Mensagens.FALHAR:
                        atenderFalha(simulacao, mensagem);
                        break;
                }
            }
        }
    }

    @Override
    public void atenderCancelamento(Simulation simulacao, Mensagem mensagem) {
         if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(FutureEvent.SAIDA, this, mensagem.getTarefa());
            tarefaEmExecucao.remove(mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        }
        double inicioAtendimento = mensagem.getTarefa().cancelar(simulacao.getTime(this));
        double tempoProc = simulacao.getTime(this) - inicioAtendimento;
        double mflopsProcessados = this.getMflopsProcessados(tempoProc);
        //Incrementa o número de Mflops processados por este recurso
        this.getMetrica().incMflopsProcessados(mflopsProcessados);
        //Incrementa o tempo de processamento
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa porcentagem da tarefa processada
        mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void atenderParada(Simulation simulacao, Mensagem mensagem) {
           if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            boolean remover = simulacao.removeFutureEvent(
                    FutureEvent.SAIDA,
                    this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            double inicioAtendimento = mensagem.getTarefa().parar(simulacao.getTime(this));
            double tempoProc = simulacao.getTime(this) - inicioAtendimento;
            double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
            tarefaEmExecucao.remove(mensagem.getTarefa());
            filaTarefas.add(mensagem.getTarefa());
        }
    }

    @Override
    public void atenderDevolucao(Simulation simulacao, Mensagem mensagem) {
        boolean remover = filaTarefas.remove(mensagem.getTarefa());
        if (remover) {
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void atenderDevolucaoPreemptiva(Simulation simulacao, Mensagem mensagem) {
        boolean remover = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            remover = filaTarefas.remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            remover = simulacao.removeFutureEvent(
                    FutureEvent.SAIDA,
                    this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            double inicioAtendimento = mensagem.getTarefa().parar(simulacao.getTime(this));
            double tempoProc = simulacao.getTime(this) - inicioAtendimento;
            double mflopsProcessados = this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            int numCP = (int) (mflopsProcessados / mensagem.getTarefa().getCheckPoint());
            mensagem.getTarefa().setMflopsProcessado(numCP * mensagem.getTarefa().getCheckPoint());
            tarefaEmExecucao.remove(mensagem.getTarefa());
        }
        if (remover) {
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void atenderAtualizacao(Simulation simulacao, Mensagem mensagem) {
        //enviar resultados
        
        List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoVMM);
        Mensagem novaMensagem = new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        novaMensagem.setProcessadorEscravo(new ArrayList<Tarefa>(tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<Tarefa>(filaTarefas));
        novaMensagem.setCaminho(caminho);
        FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                novaMensagem.getCaminho().remove(0),
                novaMensagem);
        //Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void atenderRetornoAtualizacao(Simulation simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderFalha(Simulation simulacao, Mensagem mensagem) {
       double tempoRec = recuperacao.remove(0);
        for (Tarefa tar : tarefaEmExecucao) {
            if (tar.getEstado() == Tarefa.PROCESSANDO) {
                falha = true;
                double inicioAtendimento = tar.parar(simulacao.getTime(this));
                double tempoProc = simulacao.getTime(this) - inicioAtendimento;
                double mflopsProcessados = this.getMflopsProcessados(tempoProc);
                //Incrementa o número de Mflops processados por este recurso
                this.getMetrica().incMflopsProcessados(mflopsProcessados);
                //Incrementa o tempo de processamento
                this.getMetrica().incSegundosDeProcessamento(tempoProc);
                //Incrementa procentagem da tarefa processada
                int numCP = (int) (mflopsProcessados / tar.getCheckPoint());
                tar.setMflopsProcessado(numCP * tar.getCheckPoint());
                if (erroRecuperavel) {
                    //Reiniciar atendimento da tarefa
                    tar.iniciarEsperaProcessamento(simulacao.getTime(this));
                    //cria evento para iniciar o atendimento imediatamente
                    FutureEvent novoEvt = new FutureEvent(
                            simulacao.getTime(this) + tempoRec,
                            FutureEvent.ATENDIMENTO,
                            this,
                            tar);
                    simulacao.addFutureEvent(novoEvt);
                } else {
                    tar.setEstado(Tarefa.FALHA);
                }
            }
        }
        if (!erroRecuperavel) {
            processadoresDisponiveis += tarefaEmExecucao.size();
            filaTarefas.clear();
        }
        tarefaEmExecucao.clear();
    }

    public CS_VMM getVmmResponsavel() {
        return vmmResponsavel;
    }
    
    public List<CS_VMM> getVMMsIntermediarios(){
        return this.VMMsIntermediarios;
    }

    public List<List> getCaminhoIntermediarios() {
        return caminhoIntermediarios;
    }
    
    public void addIntermediario(CS_VMM aux){
       System.out.println(aux.getId());
              
       this.VMMsIntermediarios.add(aux);
    }
    
    public void addCaminhoIntermediario(int i, List<CentroServico> caminho){
        this.caminhoIntermediarios.add(i, caminho);
    }

       public int getProcessadoresDisponiveis() {
        return processadoresDisponiveis;
    }

    public void setProcessadoresDisponiveis(int processadoresDisponiveis) {
        this.processadoresDisponiveis = processadoresDisponiveis;
    }

    public double getPoderProcessamento() {
        return poderProcessamento;
    }

    public void setPoderProcessamentoPorNucleo(double poderProcessamento) {
        super.setPoderComputacionalDisponivelPorProcessador(poderProcessamento);
        super.setPoderComputacional(poderProcessamento);
    }

    public double getMemoriaDisponivel() {
        return memoriaDisponivel;
    }

    public void setMemoriaDisponivel(double memoriaDisponivel) {
        this.memoriaDisponivel = memoriaDisponivel;
    }

    public double getDiscoDisponivel() {
        return discoDisponivel;
    }

    public void setDiscoDisponivel(double discoDisponivel) {
        this.discoDisponivel = discoDisponivel;
    }

    public CS_MaquinaCloud getMaquinaHospedeira() {
        return maquinaHospedeira;
    }

    public void setMaquinaHospedeira(CS_MaquinaCloud maquinaHospedeira) {
        this.maquinaHospedeira = maquinaHospedeira;
    }

    public List<CentroServico> getCaminhoVMM() {
        return caminhoVMM;
    }

    public void setCaminhoVMM(List<CentroServico> caminhoMestre) {
        this.caminhoVMM = caminhoMestre;        
    }

     public void addVMM(CS_VMM vmmResponsavel) {
        this.vmmResponsavel = vmmResponsavel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MetricasCusto getMetricaCusto() {
        return metricaCusto;
    }
    
        
    @Override
    public void determinarCaminhos() throws LinkageError {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Object getConexoesSaida() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer getCargaTarefas() {
         if (falha) {
            return -100;
        } else {
            return (filaTarefas.size() + tarefaEmExecucao.size());
        }
    }
    

    @Override
    public double getTimeCriacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CentroServico getOrigem() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<CentroServico> getCaminho() {
        return this.caminho;
    }

    @Override
    public void setCaminho(List<CentroServico> caminho) {
        this.caminho = caminho;
    }

    @Override
    public double getTamComunicacao() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getTamProcessamento() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderAckAlocacao(Simulation simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDesligamento(Simulation simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public double getInstanteAloc() {
        return instanteAloc;
    }

    public void setInstanteAloc(double instanteAloc) {
        this.instanteAloc = instanteAloc;
    }

    public double getTempoDeExec() {
        return tempoDeExec;
    }

    public void setTempoDeExec(double tempoDestruir) {
        this.tempoDeExec = tempoDestruir - getInstanteAloc();
    }
    
    
    
}
