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
import ispd.policy.PolicyCondition;
import ispd.policy.PolicyConditions;
import ispd.policy.allocation.vm.VmAllocationPolicy;
import ispd.policy.allocation.vm.VmMaster;
import ispd.policy.loaders.CloudSchedulingPolicyLoader;
import ispd.policy.loaders.VmAllocationPolicyLoader;
import ispd.policy.scheduling.cloud.CloudMaster;
import ispd.policy.scheduling.cloud.CloudSchedulingPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CS_VMM extends CS_Processamento
        implements VmMaster, CloudMaster, Mensagens, Vertice {
    private final List<CS_Comunicacao> conexoesEntrada = new ArrayList<>();
    private final List<CS_Comunicacao> conexoesSaida = new ArrayList<>();
    private final CloudSchedulingPolicy escalonador;
    private final VmAllocationPolicy alocadorVM;
    private final List<Tarefa> filaTarefas = new ArrayList<>();
    private final List<CS_VirtualMac> maquinasVirtuais = new ArrayList<>();
    private boolean vmsAlocadas = false;
    private boolean escDisponivel = false;
    private boolean alocDisponivel = true;
    private Set<PolicyCondition> tipoEscalonamento =
            PolicyConditions.WHILE_MUST_DISTRIBUTE;
    private Set<PolicyCondition> tipoAlocacao =
            PolicyConditions.WHILE_MUST_DISTRIBUTE;
    private List<List> caminhoEscravo = null;
    private List<List> caminhoVMs = null;
    private Simulation simulacao = null;

    public CS_VMM(
            final String id, final String owner,
            final double computationalPower, final double ignoredMemory,
            final double ignoredDisk, final double loadFactor,
            final String schedulingPolicyName,
            final String allocationPolicyName) {
        super(id, owner, computationalPower, 1, loadFactor, 0);
        this.alocadorVM = new VmAllocationPolicyLoader()
                .loadPolicy(allocationPolicyName);
        this.alocadorVM.setMestre(this);
        this.escalonador = new CloudSchedulingPolicyLoader()
                .loadPolicy(schedulingPolicyName);
        this.escalonador.setMestre(this);
    }

    @Override
    public void chegadaDeCliente(
            final Simulation simulacao, final Tarefa cliente) {
        System.out.println("------------------------------------------");
        System.out.println("Evento de chegada no vmm " + this.getId());
        if (cliente instanceof TarefaVM trf) {
            final CS_VirtualMac vm = trf.getVM_enviada();
            if (cliente.getCaminho().isEmpty()) {
                // trecho dbg
                if (this.maquinasVirtuais.contains(vm)) {
                    System.out.println("VM duplicada");
                    System.out.println(
                            "------------------------------------------");
                } else {
                    System.out.println("vm " + vm.getId() + " adicionada no " +
                                       "alocador do VMM " + this.getId());
                    System.out.println(
                            "------------------------------------------");
                    this.maquinasVirtuais.add(vm); // adiciona na lista de
                    // maquinas virtuais
                    if (this.alocDisponivel) {
                        this.alocDisponivel = false;
                        this.alocadorVM.addVM(vm);
                        this.escalonador.addEscravo(vm);
                        this.executeAllocation();
                    } else {
                        this.alocadorVM.addVM(vm);
                        this.escalonador.addEscravo(vm);
                    }
                }
            } else {// se não for ele a origem ele precisa encaminhá-la

                System.out.println(this.getId() + ": sou VMM intermediario, " +
                                   "encamininhando " + vm.getId());
                final FutureEvent evtFut =
                        new FutureEvent(simulacao.getTime(this),
                                FutureEvent.CHEGADA,
                                cliente.getCaminho().remove(0), cliente);
                simulacao.addFutureEvent(evtFut);
            }
        } else { // cliente é tarefa comum
            System.out.println("cliente é a tarefa " + cliente.getIdentificador() + " com status " + cliente.getEstado());

            if (cliente.getEstado() != Tarefa.CANCELADO) {
                // Tarefas concluida possuem tratamento diferencial
                if (cliente.getEstado() == Tarefa.CONCLUIDO) {
                    System.out.println("cliente é o retorno de tarefa " + cliente.getIdentificador());
                    // se não for origem da tarefa ela deve ser encaminhada
                    if (!cliente.getOrigem().equals(this)) {
                        // encaminhar tarefa!
                        // Gera evento para chegada da tarefa no proximo
                        // servidor
                        final FutureEvent evtFut =
                                new FutureEvent(simulacao.getTime(this),
                                        FutureEvent.CHEGADA,
                                        cliente.getCaminho().remove(0),
                                        cliente);
                        // Adicionar na lista de eventos futuros
                        simulacao.addFutureEvent(evtFut);
                    }
                    // caso seja este o centro de serviço de origem
                    System.out.println("Tarefa " + cliente.getIdentificador() + " adicionada na lista de concluídas");
                    this.escalonador.addTarefaConcluida(cliente);

                    if (this.tipoEscalonamento.contains(PolicyCondition.WHEN_RECEIVES_RESULT)) {
                        if (this.escalonador.getFilaTarefas().isEmpty()) {
                            this.escDisponivel = true;
                        } else {
                            this.executeScheduling();
                        }
                    }
                } // Caso a tarefa está chegando pra ser escalonada
                else {
                    if (cliente.getCaminho() != null) {
                        final FutureEvent evtFut =
                                new FutureEvent(simulacao.getTime(this),
                                        FutureEvent.CHEGADA,
                                        cliente.getCaminho().remove(0),
                                        cliente);
                        simulacao.addFutureEvent(evtFut);
                    } else {
                        if (this.escDisponivel) {
                            System.out.println("Tarefa " + cliente.getIdentificador() + " chegando para ser escalonada");
                            this.escDisponivel = false;
                            // escalonador adiciona nova tarefa
                            this.escalonador.adicionarTarefa(cliente);
                            // como o escalonador está disponível vai
                            // executar o escalonamento diretamente
                            this.executeScheduling();
                        } else {
                            // escalonador apenas adiciona a tarefa
                            this.escalonador.adicionarTarefa(cliente);
                        }
                    }
                }
            }
        }
    }

    // o VMM não irá processar tarefas... apenas irá escaloná-las..
    @Override
    public void atendimento(final Simulation simulacao, final Tarefa cliente) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saidaDeCliente(
            final Simulation simulacao, final Tarefa cliente) {
        System.out.println("Evento de Saída: VMM " + this.getId());
        if (cliente instanceof TarefaVM trf) {
            System.out.println("cliente é a vm " + trf.getVM_enviada().getId());
            final FutureEvent evtFut = new FutureEvent(simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            // Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (this.tipoAlocacao.contains(PolicyCondition.WHILE_MUST_DISTRIBUTE)) {
                if (!this.alocadorVM.getMaquinasVirtuais().isEmpty()) {
                    this.executeAllocation();
                } else {
                    this.alocDisponivel = true;
                }
            }
        } else {
            System.out.println("cliente é uma tarefa " + cliente.getIdentificador());
            final FutureEvent evtFut = new FutureEvent(simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            // Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (this.tipoEscalonamento.contains(PolicyCondition.WHILE_MUST_DISTRIBUTE)) {
                // se fila de tarefas do servidor não estiver vazia escalona
                // proxima tarefa
                if (!this.escalonador.getFilaTarefas().isEmpty()) {
                    this.executeScheduling();
                } else {
                    this.escDisponivel = true;
                }
            }
        }
    }

    @Override
    public void requisicao(
            final Simulation simulacao,
            final Mensagem mensagem,
            final int tipo) {
        if (tipo == FutureEvent.ESCALONAR) {
            System.out.println("Iniciando escalonamento...");
            this.escalonador.escalonar();
        } else if (tipo == FutureEvent.ALOCAR_VMS) {
            System.out.println("Iniciando Alocação...");
            this.alocadorVM.escalonar();// realizar a rotina de alocar a
            // máquina
            // virtual
        } else if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                this.atenderAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTipo() == Mensagens.ALOCAR_ACK) {
                this.atenderAckAlocacao(simulacao, mensagem);

            } else if (mensagem.getTarefa() != null && mensagem.getTarefa().getLocalProcessamento().equals(this)) {
                switch (mensagem.getTipo()) {
                    case Mensagens.PARAR ->
                            this.atenderParada(simulacao, mensagem);
                    case Mensagens.CANCELAR ->
                            this.atenderCancelamento(simulacao, mensagem);
                    case Mensagens.DEVOLVER ->
                            this.atenderDevolucao(simulacao, mensagem);
                    case Mensagens.DEVOLVER_COM_PREEMPCAO ->
                            this.atenderDevolucaoPreemptiva(simulacao,
                                    mensagem);
                }
            } else if (mensagem.getTipo() == Mensagens.RESULTADO_ATUALIZAR) {
                this.atenderRetornoAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null) {
                // encaminhando mensagem para o destino
                this.sendMessage(mensagem.getTarefa(),
                        (CS_Processamento) mensagem.getTarefa().getLocalProcessamento(),
                        mensagem.getTipo());
            }
        }
        // deve incluir requisição para alocar..
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida() {
        return this.conexoesSaida;
    }

    @Override
    public Integer getCargaTarefas() {
        return (this.escalonador.getFilaTarefas().size() + this.filaTarefas.size());
    }

    @Override
    public void atenderCancelamento(final Simulation simulacao,
                                    final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderParada(final Simulation simulacao,
                              final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderDevolucao(final Simulation simulacao,
                                 final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderDevolucaoPreemptiva(final Simulation simulacao,
                                           final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderAtualizacao(final Simulation simulacao,
                                   final Mensagem mensagem) {
        // atualiza metricas dos usuarios globais
        // simulacao.getRedeDeFilas().getMetricasUsuarios()
        // .addMetricasUsuarios(escalonador.getMetricaUsuarios());
        // enviar resultados
        final List<CentroServico> caminho = new ArrayList<>(
                Objects.requireNonNull(CS_Processamento.getMenorCaminhoIndireto(this,
                        (CS_Processamento) mensagem.getOrigem())));
        final Mensagem novaMensagem = new Mensagem(this,
                mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        // Obtem informações dinâmicas
        novaMensagem.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaMensagem.getFilaEscravo().addAll(this.escalonador.getFilaTarefas());
        novaMensagem.setCaminho(caminho);
        final FutureEvent evtFut = new FutureEvent(simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                novaMensagem.getCaminho().remove(0), novaMensagem);
        // Event adicionado a lista de evntos futuros
        simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void atenderRetornoAtualizacao(final Simulation simulacao,
                                          final Mensagem mensagem) {
        this.escalonador.resultadoAtualizar(mensagem);
    }

    @Override
    public void atenderFalha(final Simulation simulacao,
                             final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); // To
        // change body of generated methods, choose Tools
        // | Templates.
    }

    @Override
    public void atenderAckAlocacao(final Simulation simulacao,
                                   final Mensagem mensagem) {
        // se este VMM for o de origem ele deve atender senão deve encaminhar
        // a mensagem
        // para frente
        System.out.println("--------------------------------------");
        final TarefaVM trf = (TarefaVM) mensagem.getTarefa();
        final CS_VirtualMac auxVM = trf.getVM_enviada();
        final CS_MaquinaCloud auxMaq = auxVM.getMaquinaHospedeira();
        System.out.println("Atendendo ACK de alocação da vm " + auxVM.getId() + " na máquina " + auxMaq.getId());
        if (auxVM.getVmmResponsavel().equals(this)) {// se o VMM responsável
            // da VM for este..
            // tratar o ack
            // primeiro encontrar o caminho pra máquina onde a vm está alocada

            final int index =
                    this.alocadorVM.getEscravos().indexOf(auxMaq); //
            // busca índice da maquina na lista de máquinas
            // físicas do vmm
            final ArrayList<CentroServico> caminho;
            if (index == -1) {
                caminho =
                        new ArrayList<>(Objects.requireNonNull(CS_Processamento.getMenorCaminhoIndiretoCloud(this, auxMaq)));
            } else {
                caminho =
                        new ArrayList<CentroServico>(this.caminhoEscravo.get(index));
            }
            System.out.println(this.getId() + ": Caminho encontrado para a vm" +
                               " com tamanho: " + caminho.size());
            this.determinarCaminhoVM(auxVM, caminho);
            System.out.println(auxVM.getId() + " Alocada");
            auxVM.setStatus(CS_VirtualMac.ALOCADA);
            auxVM.setInstanteAloc(simulacao.getTime(this));
            if (!this.vmsAlocadas) {
                this.vmsAlocadas = true;
                this.escDisponivel = true;
            }
        } else {// passar adiante, encontrando antes o caminho intermediário
            // para poder
            // escalonar tarefas desse VMM tbm para a vm hierarquica
            System.out.println(this.getId() + ": VMM intermediário, definindo" +
                               " caminho intermediário para " + auxVM.getId());
            if (this.escalonador.getEscravos().contains(auxVM)) {
                final int index =
                        this.alocadorVM.getEscravos().indexOf(auxMaq);
                final ArrayList<CentroServico> caminho;
                if (index == -1) {
                    caminho =
                            new ArrayList<>(Objects.requireNonNull(CS_Processamento.getMenorCaminhoIndiretoCloud(this, auxMaq)));
                } else {
                    caminho =
                            new ArrayList<CentroServico>(this.caminhoEscravo.get(index));
                }
                System.out.println("Caminho encontrado para a vm com tamanho:" +
                                   " " + caminho.size());
                this.determinarCaminhoVM(auxVM, caminho);
            }
            final FutureEvent evt = new FutureEvent(simulacao.getTime(this),
                    FutureEvent.MENSAGEM,
                    mensagem.getCaminho().remove(0), mensagem);
            simulacao.addFutureEvent(evt);
        }
    }

    private void determinarCaminhoVM(final CS_Processamento vm,
                                     final List<CentroServico> caminhoVM) {

        final int indVM = this.escalonador.getEscravos().indexOf(vm);
        System.out.println("indice da vm: " + indVM);
        if (indVM >= this.caminhoVMs.size()) {
            this.caminhoVMs.add(indVM, caminhoVM);
        } else {
            this.caminhoVMs.set(indVM, caminhoVM);
        }
        System.out.println("Lista atualizada de caminho para as vms:");
        for (int i = 0; i < this.caminhoVMs.size(); i++) {
            System.out.println(this.escalonador.getEscravos().get(i).getId());
            System.out.println(this.caminhoVMs.get(i).toString());
        }
        this.escalonador.setCaminhoEscravo(this.caminhoVMs);
        System.out.println("------------------------------");

    }

    @Override
    public void atenderDesligamento(final Simulation simulacao,
                                    final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); // To
        // change body of generated methods, choose Tools
        // | Templates.
    }

    @Override
    public void executeScheduling() {
        System.out.println(this.getId() + " solicitando escalonamento");
        final FutureEvent evtFut = new FutureEvent(this.simulacao.getTime(this),
                FutureEvent.ESCALONAR, this, null);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public Set<PolicyCondition> getSchedulingConditions() {
        return this.tipoEscalonamento;
    }

    @Override
    public void setSchedulingConditions(final Set<PolicyCondition> newConditions) {
        this.tipoEscalonamento = newConditions;
    }

    // métodos do Mestre
    @Override
    public void sendTask(final Tarefa task) {
        // Gera evento para atender proximo cliente da lista
        System.out.println(
                "Tarefa:" + task.getIdentificador() + "escalonada para vm:" + task.getLocalProcessamento().getId());
        final FutureEvent evtFut = new FutureEvent(this.simulacao.getTime(this),
                FutureEvent.SAIDA, this, task);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void processTask(final Tarefa task) {
        throw new UnsupportedOperationException("Not supported yet."); // To
        // change body of generated methods, choose Tools
        // | Templates.
    }

    @Override
    public Tarefa cloneTask(final Tarefa task) {
        final Tarefa tarefa = new Tarefa(task);
        this.simulacao.addJob(tarefa);
        return tarefa;
    }

    @Override
    public void sendMessage(final Tarefa task,
                            final CS_Processamento slave,
                            final int messageType) {
        final Mensagem msg = new Mensagem(this, messageType, task);
        msg.setCaminho(this.escalonador.escalonarRota(slave));
        final FutureEvent evtFut = new FutureEvent(this.simulacao.getTime(this),
                FutureEvent.MENSAGEM, msg.getCaminho().remove(0),
                msg);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void updateSubordinate(final CS_Processamento slave) {
        final Mensagem msg = new Mensagem(this, 0.011444091796875,
                Mensagens.ATUALIZAR);
        msg.setCaminho(this.escalonador.escalonarRota(slave));
        final FutureEvent evtFut = new FutureEvent(this.simulacao.getTime(this),
                FutureEvent.MENSAGEM, msg.getCaminho().remove(0),
                msg);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void executeAllocation() {
        final FutureEvent evtFut = new FutureEvent(this.simulacao.getTime(this),
                FutureEvent.ALOCAR_VMS, this, null);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public Set<PolicyCondition> getAllocationConditions() {
        return this.tipoAlocacao;
    }

    @Override
    public void setAllocationConditions(final Set<PolicyCondition> tipo) {
        this.tipoAlocacao = tipo;
    }

    @Override
    public void freeScheduler() {
        this.escDisponivel = true;
    }

    @Override
    public void sendVm(final CS_VirtualMac vm) {
        System.out.println("Enviar VM: alocando VM " + vm.getId());
        System.out.println("------------------------------------------");
        final TarefaVM tarefa = new TarefaVM(vm.getVmmResponsavel(), vm,
                300.0, 0.0);
        tarefa.setCaminho(vm.getCaminho());
        final FutureEvent evtFut = new FutureEvent(this.simulacao.getTime(this),
                FutureEvent.SAIDA, this, tarefa);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public Simulation getSimulation() {
        return this.simulacao;
    }

    @Override
    public void setSimulation(final Simulation newSimulation) {
        this.simulacao = newSimulation;
    }

    public void atualizar(final CentroServico escravo, final Double time) {
        final Mensagem msg = new Mensagem(this, 0.011444091796875,
                Mensagens.ATUALIZAR);
        msg.setCaminho(this.escalonador.escalonarRota(escravo));
        final FutureEvent evtFut = new FutureEvent(time, FutureEvent.MENSAGEM,
                msg.getCaminho().remove(0), msg);
        // Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    public CloudSchedulingPolicy getEscalonador() {
        return this.escalonador;
    }

    public VmAllocationPolicy getAlocadorVM() {
        return this.alocadorVM;
    }

    @Override
    public void addConexoesEntrada(final CS_Link link) {
        this.conexoesEntrada.add(link);
    }

    @Override
    public void addConexoesSaida(final CS_Link link) {
        this.conexoesSaida.add(link);
    }

    public void addConexoesSaida(final CS_Comunicacao Switch) {
        this.conexoesSaida.add(Switch);
    }

    public void addConexoesEntrada(final CS_Comunicacao Switch) {
        this.conexoesEntrada.add(Switch);
    }

    public void addEscravo(final CS_Processamento maquina) {
        this.alocadorVM.addEscravo(maquina);

    }

    public void addVM(final CS_VirtualMac vm) {
        this.maquinasVirtuais.add(vm);
        this.alocadorVM.addVM(vm);
        this.escalonador.addEscravo(vm);
    }

    @Override
    public void determinarCaminhos() throws LinkageError {
        final var escravos = this.alocadorVM.getEscravos();
        this.caminhoEscravo = new ArrayList<>(escravos.size());
        // Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            this.caminhoEscravo.add(i, CS_Processamento.getMenorCaminho(this,
                    escravos.get(i)));
        }
        // verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (this.caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }

        this.alocadorVM.setCaminhoEscravo(this.caminhoEscravo);
    }

    public void instanciarCaminhosVMs() {
        this.caminhoVMs =
                new ArrayList<>(this.escalonador.getEscravos().size());
        for (int i = 0; i < this.escalonador.getEscravos().size(); i++) {
            this.caminhoVMs.add(i, new ArrayList());
        }
    }
}
