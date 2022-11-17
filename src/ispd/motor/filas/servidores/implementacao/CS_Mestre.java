package ispd.motor.filas.servidores.implementacao;

import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.policy.PolicyCondition;
import ispd.policy.escalonador.Carregar;
import ispd.policy.escalonador.Escalonador;
import ispd.policy.escalonador.Mestre;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public class CS_Mestre extends CS_Processamento
        implements Mestre, Mensagens, Vertice {
    private final List<CS_Comunicacao> conexoesEntrada = new ArrayList<>();
    private final List<CS_Comunicacao> conexoesSaida = new ArrayList<>();
    private final Escalonador escalonador;
    private final List<Tarefa> filaTarefas = new ArrayList<>();
    private boolean maqDisponivel = true;
    private boolean escDisponivel = true;
    private EnumSet<PolicyCondition> tipoEscalonamento = EnumSet.of(PolicyCondition.WHILE_THERE_ARE_RESOURCES);
    private Simulation simulacao = null;

    public CS_Mestre(final String id, final String owner,
                     final double computationalPower, final double loadFactor,
                     final String schedulingPolicyName, final Double energy) {
        super(id, owner, computationalPower, 1, loadFactor, 0, energy);
        this.escalonador = Carregar.getNewEscalonador(schedulingPolicyName);
        Objects.requireNonNull(this.escalonador).setMestre(this);
    }

    @Override
    public void chegadaDeCliente(final Simulation simulacao,
                                 final Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
            //Tarefas concluida possuem tratamento diferencial
            if (cliente.getEstado() == Tarefa.CONCLUIDO) {
                //se não for origem da tarefa ela deve ser encaminhada
                if (!cliente.getOrigem().equals(this)) {
                    //encaminhar tarefa!
                    //Gera evento para chegada da tarefa no proximo servidor
                    final FutureEvent evtFut = new FutureEvent(
                            simulacao.getTime(this),
                            FutureEvent.CHEGADA,
                            cliente.getCaminho().remove(0),
                            cliente);
                    //Event adicionado a lista de evntos futuros
                    simulacao.addFutureEvent(evtFut);
                }
                this.escalonador.addTarefaConcluida(cliente);
                if (this.tipoEscalonamento.contains(PolicyCondition.WHEN_RECEIVES_RETURN)) {
                    if (this.escalonador.getFilaTarefas().isEmpty()) {
                        this.escDisponivel = true;
                    } else {
                        this.executePolicy();
                    }
                }
            } else if (this.escDisponivel) {
                this.escDisponivel = false;
                //escalonador decide qual ação tomar na chegada de uma tarefa
                this.escalonador.adicionarTarefa(cliente);
                //Se não tiver tarefa na fila a primeira tarefa será escalonada
                this.executePolicy();
            } else {
                //escalonador decide qual ação tomar na chegada de uma tarefa
                this.escalonador.adicionarTarefa(cliente);
            }
        }
    }

    @Override
    public void atendimento(final Simulation simulacao, final Tarefa cliente) {
        //o atendimento pode realiza o processamento da tarefa como em uma
        // maquina qualquer
        if (this.maqDisponivel) {
            this.maqDisponivel = false;
            cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
            cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para saida do cliente do servidor
            final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this) + this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado()),
                    FutureEvent.SAIDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            this.filaTarefas.add(cliente);
        }
    }

    @Override
    public void saidaDeCliente(final Simulation simulacao,
                               final Tarefa cliente) {
        if (cliente.getEstado() == Tarefa.PROCESSANDO) {
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
            //Incrementa o tempo de transmissão
            final double tempoProc =
                    this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa o tempo de transmissão no pacote
            cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para chegada da tarefa no proximo servidor
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                ////Indica que está livre
                this.maqDisponivel = true;
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        } else {
            //Gera evento para chegada da tarefa no proximo servidor
            final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
            if (this.tipoEscalonamento.contains(PolicyCondition.WHILE_THERE_ARE_RESOURCES)) {
                //se fila de tarefas do servidor não estiver vazia escalona
                // proxima tarefa
                if (this.escalonador.getFilaTarefas().isEmpty()) {
                    this.escDisponivel = true;
                } else {
                    this.executePolicy();
                }
            }
        }
    }

    @Override
    public void requisicao(final Simulation simulacao,
                           final Mensagem mensagem, final int tipo) {
        if (tipo == FutureEvent.ESCALONAR) {
            this.escalonador.escalonar();
        } else if (mensagem != null) {
            if (mensagem.getTipo() == Mensagens.ATUALIZAR) {
                this.atenderAtualizacao(simulacao, mensagem);
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
                //encaminhando mensagem para o destino
                this.sendMessage(mensagem.getTarefa(),
                        (CS_Processamento) mensagem.getTarefa().getLocalProcessamento(), mensagem.getTipo());
            }
        }
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
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeFutureEvent(FutureEvent.SAIDA, this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
        }
        final double inicioAtendimento =
                mensagem.getTarefa().cancelar(simulacao.getTime(this));
        final double tempoProc = simulacao.getTime(this) - inicioAtendimento;
        final double mflopsProcessados = this.getMflopsProcessados(tempoProc);
        //Incrementa o número de Mflops processados por este recurso
        this.getMetrica().incMflopsProcessados(mflopsProcessados);
        //Incrementa o tempo de processamento
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa procentagem da tarefa processada
        mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
        mensagem.getTarefa().incMflopsDesperdicados(mflopsProcessados);
    }

    @Override
    public void atenderParada(final Simulation simulacao,
                              final Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            final boolean remover =
                    simulacao.removeFutureEvent(FutureEvent.SAIDA,
                            this, mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final double inicioAtendimento =
                    mensagem.getTarefa().parar(simulacao.getTime(this));
            final double tempoProc =
                    simulacao.getTime(this) - inicioAtendimento;
            final double mflopsProcessados =
                    this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
        }
    }

    @Override
    public void atenderDevolucao(final Simulation simulacao,
                                 final Mensagem mensagem) {
        final boolean temp1 = this.filaTarefas.remove(mensagem.getTarefa());
        final boolean temp2 =
                this.escalonador.getFilaTarefas().remove(mensagem.getTarefa());
        if (temp1 || temp2) {
            final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void atenderDevolucaoPreemptiva(final Simulation simulacao,
                                           final Mensagem mensagem) {
        boolean temp1 = false;
        boolean temp2 = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            temp1 = this.filaTarefas.remove(mensagem.getTarefa());
            temp2 = this.escalonador.getFilaTarefas().remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            temp1 = simulacao.removeFutureEvent(FutureEvent.SAIDA, this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (this.filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                final Tarefa proxCliente = this.filaTarefas.remove(0);
                final FutureEvent evtFut = new FutureEvent(
                        simulacao.getTime(this),
                        FutureEvent.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addFutureEvent(evtFut);
            }
            final double inicioAtendimento =
                    mensagem.getTarefa().parar(simulacao.getTime(this));
            final double tempoProc =
                    simulacao.getTime(this) - inicioAtendimento;
            final double mflopsProcessados =
                    this.getMflopsProcessados(tempoProc);
            //Incrementa o número de Mflops processados por este recurso
            this.getMetrica().incMflopsProcessados(mflopsProcessados);
            //Incrementa o tempo de processamento
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa procentagem da tarefa processada
            final double numCP =
                    ((int) (mflopsProcessados / mensagem.getTarefa().getCheckPoint())) * mensagem.getTarefa().getCheckPoint();
            mensagem.getTarefa().setMflopsProcessado(numCP);
            //Incrementa desperdicio
            mensagem.getTarefa().incMflopsDesperdicados(mflopsProcessados - numCP);
        }
        if (temp1 || temp2) {
            final FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
    }

    @Override
    public void atenderAtualizacao(final Simulation simulacao,
                                   final Mensagem mensagem) {
        //atualiza metricas dos usuarios globais
        //simulacao.getRedeDeFilas().getMetricasUsuarios()
        // .addMetricasUsuarios(escalonador.getMetricaUsuarios());
        //enviar resultados
        final List<CentroServico> caminho =
                new ArrayList<>(Objects.requireNonNull(CS_Processamento.getMenorCaminhoIndireto(this, (CS_Processamento) mensagem.getOrigem())));
        final Mensagem novaMensagem = new Mensagem(this,
                mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        novaMensagem.setFilaEscravo(new ArrayList<>(this.filaTarefas));
        novaMensagem.getFilaEscravo().addAll(this.escalonador.getFilaTarefas());
        novaMensagem.setCaminho(caminho);
        final FutureEvent evtFut = new FutureEvent(
                simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                novaMensagem.getCaminho().remove(0),
                novaMensagem);
        //Event adicionado a lista de evntos futuros
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
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderAckAlocacao(final Simulation simulacao,
                                   final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDesligamento(final Simulation simulacao,
                                    final Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To
        // change body of generated methods, choose Tools | Templates.
    }

    //métodos do Mestre
    @Override
    public void enviarTarefa(final Tarefa tarefa) {
        //Gera evento para atender proximo cliente da lista
        final FutureEvent evtFut = new FutureEvent(
                this.simulacao.getTime(this),
                FutureEvent.SAIDA,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void processarTarefa(final Tarefa tarefa) {
        tarefa.iniciarEsperaProcessamento(this.simulacao.getTime(this));
        final FutureEvent evtFut = new FutureEvent(
                this.simulacao.getTime(this),
                FutureEvent.ATENDIMENTO,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void executePolicy() {
        final FutureEvent evtFut = new FutureEvent(
                this.simulacao.getTime(this),
                FutureEvent.ESCALONAR,
                this, null);
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void sendMessage(final Tarefa task, final CS_Processamento machine,
                            final int messageType) {
        final Mensagem msg = new Mensagem(this, messageType, task);
        msg.setCaminho(this.escalonador.escalonarRota(machine));
        final FutureEvent evtFut = new FutureEvent(
                this.simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public void updateSubordinate(final CS_Processamento subordinate) {
        final Mensagem msg = new Mensagem(this, 0.011444091796875,
                Mensagens.ATUALIZAR);
        msg.setCaminho(this.escalonador.escalonarRota(subordinate));
        final FutureEvent evtFut = new FutureEvent(
                this.simulacao.getTime(this),
                FutureEvent.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    @Override
    public EnumSet<PolicyCondition> getPolicyCondition() {
        return this.tipoEscalonamento;
    }

    @Override
    public void setPolicyCondition(final EnumSet<PolicyCondition> newPolicyCondition) {
        this.tipoEscalonamento = newPolicyCondition;
    }

    @Override
    public Tarefa criarCopia(final Tarefa get) {
        final Tarefa tarefa = new Tarefa(get);
        this.simulacao.addJob(tarefa);
        return tarefa;
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
        final FutureEvent evtFut = new FutureEvent(
                time,
                FutureEvent.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        this.simulacao.addFutureEvent(evtFut);
    }

    public Escalonador getEscalonador() {
        return this.escalonador;
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
        this.escalonador.addEscravo(maquina);
    }

    /**
     * Encontra caminhos para chegar até um escravo e adiciona no caminhoEscravo
     */
    @Override
    public void determinarCaminhos() throws LinkageError {
        final List<CS_Processamento> escravos = this.escalonador.getEscravos();
        //Instancia objetos
        List<List> caminhoEscravo = new ArrayList<>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            caminhoEscravo.add(i, CS_Processamento.getMenorCaminho(this,
                    escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
        this.escalonador.setCaminhoEscravo(caminhoEscravo);
    }
}
