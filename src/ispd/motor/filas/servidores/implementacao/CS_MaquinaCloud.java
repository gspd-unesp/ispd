/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.TarefaVM;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.metricas.MetricasAlocacao;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diogo Tavares
 */
public class CS_MaquinaCloud extends CS_Processamento implements Mensagens, Vertice {
    
    public static final int DORMINDO = 0;
    public static final int LIGADO = 1;
    public static final int DESLIGADO = 2;
    
    private List<CS_Comunicacao> conexoesEntrada;
    private List<CS_Comunicacao> conexoesSaida;
    private List<Tarefa> filaTarefas;
    private List<CS_Processamento> mestres;
    private List<List> caminhoMestre;
    private int processadoresDisponiveis;
    //Dados dinamicos
    private List<Tarefa> tarefaEmExecucao;
    //Adição de falhas
    private List<Double> falhas = new ArrayList<Double>();
    private List<Double> recuperacao = new ArrayList<Double>();
    private boolean erroRecuperavel;
    private boolean falha = false;
    private double memoriaDisponivel;
    private double discoDisponivel;
    private double custoProc;
    private double custoMemoria;
    private double custoDisco;
    private double custoTotalDisco;
    private double custoTotalMemoria;
    private double custoTotalProc;
    private int status;
    //lista de máquinas virtuais
    private List<CS_VirtualMac> VMs;
    private MetricasAlocacao metricaAloc;

    /**
     *
     * @param id
     * @param proprietario
     * @param PoderComputacional
     * @param numeroProcessadores
     * @param Ocupacao
     * @param memoria
     * @param disco
     */
    public CS_MaquinaCloud(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao, double memoria, double disco, double custoProc, double custoMem, double custoDisco) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, 0);
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.mestres = new ArrayList<CS_Processamento>();
        this.VMs = new ArrayList<CS_VirtualMac>();
        this.metricaAloc = new MetricasAlocacao(id);
        this.processadoresDisponiveis = numeroProcessadores;
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
        this.memoriaDisponivel = memoria;
        this.discoDisponivel = disco;
        this.custoProc = custoProc;
        this.custoMemoria = custoMem;
        this.custoDisco = custoDisco;
        this.custoTotalProc = 0;
        this.custoTotalMemoria = 0;
        this.custoTotalDisco = 0;
        this.status = DESLIGADO;
        

    }

    public CS_MaquinaCloud(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double memoria, double disco, double custoProc, double custoMem, double custoDisco, double Ocupacao, int numeroMaquina) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, numeroMaquina);
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.mestres = new ArrayList<CS_Processamento>();
        this.VMs = new ArrayList<CS_VirtualMac>();
        this.processadoresDisponiveis = numeroProcessadores;
        this.memoriaDisponivel = memoria;
        this.discoDisponivel = disco;
        this.custoProc = custoProc;
        this.custoMemoria = custoMem;
        this.custoDisco = custoDisco;
        this.custoTotalProc = 0;
        this.custoTotalMemoria = 0;
        this.custoTotalDisco = 0;
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
        this.status = DESLIGADO;

    }

   /* public CS_MaquinaCloud() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/

    @Override
    public void chegadaDeCliente(Simulation simulacao, Tarefa cliente) {
        System.out.println("----------------------------------------------");
        System.out.println("Chegada de evento na  máquina " + this.getId());
        if (cliente instanceof TarefaVM) {
            TarefaVM trf = (TarefaVM) cliente;
            CS_VirtualMac vm = trf.getVM_enviada();
            if (vm.getMaquinaHospedeira().equals(this)) {
                if (this.VMs.contains(vm)) {
                    System.out.println("Cliente duplicado!");
                } else {
                    System.out.println(vm.getId() + " enviada para evento de atendimento nesta máquina");
                    System.out.println("----------------------------------------------");
                    FutureEvent evtFut = new FutureEvent(
                            simulacao.getTime(this),
                            FutureEvent.ATENDIMENTO,
                            this,
                            cliente);
                    simulacao.addFutureEvent(evtFut);
                }
            } else {
                System.out.println(vm.getId() + " encaminhada para seu destino, esta máquina é intermediária");
                System.out.println("----------------------------------------------");
                FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.CHEGADA,
                        cliente.getCaminho().remove(0),
                        cliente);
                simulacao.addFutureEvent(evtFut);
            }
        } else {
            //procedimento caso cliente seja uma tarefa!
            CS_VirtualMac vm = (CS_VirtualMac) cliente.getLocalProcessamento();
            if (vm.getMaquinaHospedeira().equals(this)) {//se a tarefa é endereçada pra uma VM qu está alocada nessa máquina
                System.out.println(this.getId() + ": Tarefa " + cliente.getIdentificador() + " sendo enviada para execução na vm " + vm.getId());
                System.out.println("----------------------------------------------");
                FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.CHEGADA,
                        vm,
                        cliente);
                simulacao.addFutureEvent(evtFut);
            } else {
                System.out.println(this.getId() + ": Tarefa " + cliente.getIdentificador() + " sendo encaminhada para próximo CS");
                System.out.println("----------------------------------------------");
                FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.SAIDA,
                        this,
                        cliente);
                simulacao.addFutureEvent(evtFut);
            }

        }

    }

    @Override
    public void atendimento(Simulation simulacao, Tarefa cliente) {

        TarefaVM trf = (TarefaVM) cliente;
        CS_VirtualMac vm = trf.getVM_enviada();
        System.out.println("--------------------------------------------------");
        System.out.println("atendimento da vm:" + vm.getId() + "na maquina:" + this.getId());

            //vm.setStatus(CS_VirtualMac.ALOCADA);
        this.addVM(vm); //incluir a VM na lista de VMs
        getMetricaAloc().incVMsAlocadas();
        //Setar o caminho da vm para o VMM e o caminho do ACK da mensagem >>>
        CS_VMM vmm = vm.getVmmResponsavel();
            //trecho de teste
        //
        //fim teste
        int index = mestres.indexOf(vmm);
        Mensagem msg = new Mensagem(this, Mensagens.ALOCAR_ACK, cliente);

        if (index == -1) {
            ArrayList<CentroServico> caminhoVMM = new ArrayList<CentroServico>(getMenorCaminhoIndiretoCloud(this, vmm));
            ArrayList<CentroServico> caminhoMsg = new ArrayList<CentroServico>(getMenorCaminhoIndiretoCloud(this, vmm));

            System.out.println("Imprimindo caminho para o mestre:");
            for (CentroServico cs : caminhoVMM) {
                System.out.println(cs.getId());
            }

            vm.setCaminhoVMM(caminhoVMM);
            msg.setCaminho(caminhoMsg);
        } else {
            ArrayList<CentroServico> caminhoVMM = new ArrayList<CentroServico>(caminhoMestre.get(index));
            ArrayList<CentroServico> caminhoMsg = new ArrayList<CentroServico>(caminhoMestre.get(index));

            System.out.println("Imprimindo caminho para o mestre:");
            for (CentroServico cs : caminhoVMM) {
                System.out.println(cs.getId());
            }
            vm.setCaminhoVMM(caminhoVMM);
            msg.setCaminho(caminhoMsg);
        }

        //enviar mensagem de ACK para o VMM
        FutureEvent NovoEvt = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                this,
                msg);
        simulacao.addFutureEvent(NovoEvt);

        //Gerenciamento de custos
        custoTotalProc = custoTotalProc + (vm.getProcessadoresDisponiveis() * custoProc);
        custoTotalMemoria = custoTotalMemoria + (vm.getMemoriaDisponivel() * custoMemoria);
        custoTotalDisco = custoTotalDisco + (vm.getDiscoDisponivel() * custoDisco);
        //setar o poder de processamento da VM.
        vm.setPoderProcessamentoPorNucleo(this.getPoderComputacional());
        System.out.println("----------------------------------------------------");
        /*
         //setar o caminho da vm para o mestre
         CS_VMM vmm = vm.getVmmResponsavel();

         //trecho de teste
         System.out.println("atendimento da vm:" + vm.getId() + "na maquina:" + vm.getMaquinaHospedeira().getId());
         //fim teste
         int index = mestres.indexOf(vmm);
         if (index == -1){
         List<CentroServico> caminhoVMM = getMenorCaminhoCloud(this, vmm);
         vm.setCaminhoVMM(caminhoVMM);
         }else{
         vm.setCaminhoVMM(caminhoMestre.get(index));
         }

         System.out.println("indice do mestre:" + index);
         //vm.setCaminhoVMM(caminhoMestre.get(index));
         */

    }

    @Override
    public void saidaDeCliente(Simulation simulacao, Tarefa cliente) {
        System.out.println("--------------------------------------");
        System.out.println(this.getId() + ": Saída de cliente");
        System.out.println("--------------------------------------");
        FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.CHEGADA,
                cliente.getCaminho().remove(0),
                cliente);
    }

    @Override
    public void requisicao(Simulation simulacao, Mensagem mensagem, int tipo) {
        if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTipo() == Mensagens.ALOCAR_ACK) { //a máquina é só um intermediário
                //esse tipo de mensagem só é atendido por um VMM
                atenderAckAlocacao(simulacao, mensagem);

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
    public void determinarCaminhos() throws LinkageError {
        //Instancia objetos
        caminhoMestre = new ArrayList<List>(mestres.size());
        System.out.println("maquina " + getId() + " determinando caminhos para " + mestres.size() + " mestres");
        //Busca pelos caminhos

        for (int i = 0; i < mestres.size(); i++) {
            caminhoMestre.add(i, CS_MaquinaCloud.getMenorCaminhoCloud(this, mestres.get(i)));
        }
        //verifica se todos os mestres são alcansaveis
        for (int i = 0; i < mestres.size(); i++) {
            if (caminhoMestre.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
    }

    @Override
    public void atenderAckAlocacao(Simulation simulacao, Mensagem mensagem) {
        //quem deve resolver esse método é o VMM de origem
        //portanto as maquinas só encaminham pro próximo centro de serviço.
        System.out.println("--------------------------------------");
        System.out.println("Encaminhando ACK de alocação para " + mensagem.getOrigem().getId());
        FutureEvent evt = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                mensagem.getCaminho().remove(0),
                mensagem);
        simulacao.addFutureEvent(evt);
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
        int index = mestres.indexOf(mensagem.getOrigem());
        List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoMestre.get(index));
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
        throw new UnsupportedOperationException("Not supported yet.");
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

    @Override
    public Integer getCargaTarefas() {
        if (falha) {
            return -100;
        } else {
            return (filaTarefas.size() + tarefaEmExecucao.size());
        }
    }

    public void addFalha(Double tFalha, double tRec, boolean recuperavel) {
        falhas.add(tFalha);
        recuperacao.add(tRec);
        erroRecuperavel = recuperavel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MetricasAlocacao getMetricaAloc() {
        return metricaAloc;
    }
    
    //manda o custo total para as metricas
    public List<List> getCaminhoMestre() {
        return caminhoMestre;
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

    public int getProcessadoresDisponiveis() {
        return processadoresDisponiveis;
    }

    public void setProcessadoresDisponiveis(int processadoresDisponiveis) {
        this.processadoresDisponiveis = processadoresDisponiveis;
    }

    public double getCustoProc() {
        return custoProc;
    }

    public void setCustoProc(double custoProc) {
        this.custoProc = custoProc;
    }

    public double getCustoMemoria() {
        return custoMemoria;
    }

    public void setCustoMemoria(double custoMemoria) {
        this.custoMemoria = custoMemoria;
    }

    public double getCustoDisco() {
        return custoDisco;
    }

    public void setCustoDisco(double custoDisco) {
        this.custoDisco = custoDisco;
    }

    public double getCustoTotalDisco() {
        return custoTotalDisco;
    }

    public double getCustoTotalMemoria() {
        return custoTotalMemoria;
    }

    public double getCustoTotalProc() {
        return custoTotalProc;
    }

    @Override
    public void addConexoesEntrada(CS_Link conexao) {
        this.conexoesEntrada.add(conexao);
    }

    @Override
    public void addConexoesSaida(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    public List<CS_Processamento> getMestres() {
        return mestres;
    }

    public void setMestres(List<CS_Processamento> mestres) {
        this.mestres = mestres;
    }

    public void addConexoesEntrada(CS_Switch conexao) {
        this.conexoesEntrada.add(conexao);
    }

    public void addConexoesSaida(CS_Switch conexao) {
        this.conexoesSaida.add(conexao);
    }

    public void addMestre(CS_Processamento mestre) {
        this.mestres.add(mestre);
    }

    public void addVM(CS_VirtualMac vm) {
        this.VMs.add(vm);
    }

    public void removeVM(CS_VirtualMac vm) {
        this.VMs.remove(vm);
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida() {
        return this.conexoesSaida;
    }

    @Override
    public void atenderDesligamento(Simulation simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void desligar(Simulation simulacao) {
        for(CS_VirtualMac vm : VMs){
            vm.setStatus(CS_VirtualMac.DESTRUIDA);
            vm.setTempoDeExec(simulacao.getTime(this));
            vm.getMetricaCusto().setCustoDisco(custoDisco*vm.getDiscoDisponivel()*(vm.getTempoDeExec()/60));
            vm.getMetricaCusto().setCustoMem(custoMemoria*(vm.getMemoriaDisponivel()/1024)*(vm.getTempoDeExec()/60));
            vm.getMetricaCusto().setCustoProc(custoProc*vm.getProcessadoresDisponiveis()*(vm.getTempoDeExec()/60));
        }
    }

}
