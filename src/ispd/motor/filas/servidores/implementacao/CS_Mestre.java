/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.escalonador.Carregar;
import ispd.escalonador.Escalonador;
import ispd.escalonador.Mestre;
import ispd.motor.EventoFuturo;
import ispd.motor.Mensagens;
import ispd.motor.Simulacao;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class CS_Mestre extends CS_Processamento implements Mestre, Mensagens, Vertice {

    private List<CS_Comunicacao> conexoesEntrada;
    private List<CS_Comunicacao> conexoesSaida;
    private Escalonador escalonador;
    private List<Tarefa> filaTarefas;
    private boolean maqDisponivel;
    private boolean escDisponivel;
    private int tipoEscalonamento;
    
    /**
     * Armazena os caminhos possiveis para alcançar cada escravo
     */
    private List<List> caminhoEscravo;
    private Simulacao simulacao;

    public CS_Mestre(String id, String proprietario, double PoderComputacional, double Ocupacao, String Escalonador) {
        super(id, proprietario, PoderComputacional, 1, Ocupacao, 0);
        this.escalonador = Carregar.getNewEscalonador(Escalonador);
        escalonador.setMestre(this);
        this.filaTarefas = new ArrayList<Tarefa>();
        this.maqDisponivel = true;
        this.escDisponivel = true;
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.tipoEscalonamento = ENQUANTO_HOUVER_TAREFAS;
    }

    //Métodos do centro de serviços
    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
            //Tarefas concluida possuem tratamento diferencial
            if (cliente.getEstado() == Tarefa.CONCLUIDO) {
                //se não for origem da tarefa ela deve ser encaminhada
                if (!cliente.getOrigem().equals(this)) {
                    //encaminhar tarefa!
                    //Gera evento para chegada da tarefa no proximo servidor
                    EventoFuturo evtFut = new EventoFuturo(
                            simulacao.getTime(this),
                            EventoFuturo.CHEGADA,
                            cliente.getCaminho().remove(0),
                            cliente);
                    //Event adicionado a lista de evntos futuros
                    simulacao.addEventoFuturo(evtFut);
                }
                this.escalonador.addTarefaConcluida(cliente);
                if (tipoEscalonamento == QUANDO_RECEBE_RESULTADO || tipoEscalonamento == AMBOS) {
                    if (this.escalonador.getFilaTarefas().isEmpty()) {
                        this.escDisponivel = true;
                    } else {
                        executarEscalonamento();
                    }
                }
            } else if (escDisponivel) {
                this.escDisponivel = false;
                //escalonador decide qual ação tomar na chegada de uma tarefa
                escalonador.adicionarTarefa(cliente);
                //Se não tiver tarefa na fila a primeira tarefa será escalonada
                executarEscalonamento();
            } else {
                //escalonador decide qual ação tomar na chegada de uma tarefa
                escalonador.adicionarTarefa(cliente);
            }
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        //o atendimento pode realiza o processamento da tarefa como em uma maquina qualquer
        if (this.maqDisponivel) {
            this.maqDisponivel = false;
            cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
            cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para saida do cliente do servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this) + tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado()),
                    EventoFuturo.SAÍDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        } else {
            filaTarefas.add(cliente);
        }
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() == Tarefa.PROCESSANDO) {
            //Incrementa o número de Mbits transmitido por este link
            this.getMetrica().incMflopsProcessados(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
            //Incrementa o tempo de transmissão
            double tempoProc = this.tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
            this.getMetrica().incSegundosDeProcessamento(tempoProc);
            //Incrementa o tempo de transmissão no pacote
            cliente.finalizarAtendimentoProcessamento(simulacao.getTime(this));
            //Gera evento para chegada da tarefa no proximo servidor
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                ////Indica que está livre
                this.maqDisponivel = true;
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
            }
        } else {
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0), cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
            if (tipoEscalonamento == ENQUANTO_HOUVER_TAREFAS || tipoEscalonamento == AMBOS) {
                //se fila de tarefas do servidor não estiver vazia escalona proxima tarefa
                if (!escalonador.getFilaTarefas().isEmpty()) {
                    executarEscalonamento();
                } else {
                    this.escDisponivel = true;
                }
            }
        }
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem mensagem, int tipo) {
        if (tipo == EventoFuturo.ESCALONAR) {
            escalonador.escalonar();
        } else if (mensagem != null) {
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
                }
            } else if(mensagem.getTipo() == Mensagens.RESULTADO_ATUALIZAR){
                atenderRetornoAtualizacao(simulacao, mensagem);
            } else if (mensagem.getTarefa() != null) {
                //encaminhando mensagem para o destino
                this.enviarMensagem(mensagem.getTarefa(), (CS_Processamento) mensagem.getTarefa().getLocalProcessamento(), mensagem.getTipo());
            }
        }
    }

    //métodos do Mestre
    @Override
    public void enviarTarefa(Tarefa tarefa) {
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.SAÍDA,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void processarTarefa(Tarefa tarefa) {
        tarefa.iniciarEsperaProcessamento(simulacao.getTime(this));
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.ATENDIMENTO,
                this, tarefa);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void executarEscalonamento() {
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.ESCALONAR,
                this, null);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void enviarMensagem(Tarefa tarefa, CS_Processamento escravo, int tipo) {
        Mensagem msg = new Mensagem(this, tipo, tarefa);
        msg.setCaminho(escalonador.escalonarRota(escravo));
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void atualizar(CS_Processamento escravo) {
        Mensagem msg = new Mensagem(this, 0.011444091796875, Mensagens.ATUALIZAR);
        msg.setCaminho(escalonador.escalonarRota(escravo));
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    public void atualizar(CS_Processamento escravo, Double time) {
        Mensagem msg = new Mensagem(this, 0.011444091796875, Mensagens.ATUALIZAR);
        msg.setCaminho(escalonador.escalonarRota(escravo));
        EventoFuturo evtFut = new EventoFuturo(
                time,
                EventoFuturo.MENSAGEM,
                msg.getCaminho().remove(0),
                msg);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void setSimulacao(Simulacao simulacao) {
        this.simulacao = simulacao;
    }

    public Escalonador getEscalonador() {
        return escalonador;
    }

    @Override
    public void addConexoesSaida(CS_Link link) {
        conexoesSaida.add(link);
    }

    @Override
    public void addConexoesEntrada(CS_Link link) {
        conexoesEntrada.add(link);
    }

    public void addConexoesSaida(CS_Switch Switch) {
        conexoesSaida.add(Switch);
    }

    public void addConexoesEntrada(CS_Switch Switch) {
        conexoesEntrada.add(Switch);
    }

    public void addEscravo(CS_Processamento maquina) {
        escalonador.addEscravo(maquina);
    }

    @Override
    public List<CS_Comunicacao> getConexoesSaida() {
        return this.conexoesSaida;
    }

    /**
     * Encontra caminhos para chegar até um escravo e adiciona no caminhoEscravo
     */
    @Override
    public void determinarCaminhos() throws LinkageError {
        List<CS_Processamento> escravos = escalonador.getEscravos();
        //Instancia objetos
        caminhoEscravo = new ArrayList<List>(escravos.size());
        //Busca pelo melhor caminho
        for (int i = 0; i < escravos.size(); i++) {
            caminhoEscravo.add(i, CS_Mestre.getMenorCaminho(this, escravos.get(i)));
        }
        //verifica se todos os escravos são alcansaveis
        for (int i = 0; i < escravos.size(); i++) {
            if (caminhoEscravo.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
        escalonador.setCaminhoEscravo(caminhoEscravo);
    }

    @Override
    public int getTipoEscalonamento() {
        return tipoEscalonamento;
    }

    @Override
    public void setTipoEscalonamento(int tipo) {
        tipoEscalonamento = tipo;
    }

    @Override
    public Tarefa criarCopia(Tarefa get) {
        Tarefa tarefa = new Tarefa(get);
        simulacao.addTarefa(tarefa);
        return tarefa;
    }

    @Override
    public Simulacao getSimulacao() {
        return simulacao;
    }
    
    @Override
    public void atenderCancelamento(Simulacao simulacao, Mensagem mensagem) {
        boolean temp1 = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            temp1 = simulacao.removeEventoFuturo(EventoFuturo.SAÍDA, this, mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
            }
        }
        double inicioAtendimento = mensagem.getTarefa().cancelar(simulacao.getTime(this));
        double tempoProc = simulacao.getTime(this) - inicioAtendimento;
        double mflopsProcessados = this.getMflopsProcessados(tempoProc);
        //Incrementa o número de Mflops processados por este recurso
        this.getMetrica().incMflopsProcessados(mflopsProcessados);
        //Incrementa o tempo de processamento
        this.getMetrica().incSegundosDeProcessamento(tempoProc);
        //Incrementa procentagem da tarefa processada
        mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void atenderParada(Simulacao simulacao, Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            boolean remover = simulacao.removeEventoFuturo(EventoFuturo.SAÍDA, this , mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
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
        }
    }

    @Override
    public void atenderDevolucao(Simulacao simulacao, Mensagem mensagem) {
        boolean temp1 = filaTarefas.remove(mensagem.getTarefa());
        boolean temp2 = escalonador.getFilaTarefas().remove(mensagem.getTarefa());
        if (temp1 || temp2) {
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    @Override
    public void atenderDevolucaoPreemptiva(Simulacao simulacao, Mensagem mensagem) {
        boolean temp1 = false;
        boolean temp2 = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            temp1 = filaTarefas.remove(mensagem.getTarefa());
            temp2 = escalonador.getFilaTarefas().remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            temp1 = simulacao.removeEventoFuturo(EventoFuturo.SAÍDA, this, mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.maqDisponivel = true;
            } else {
                //Gera evento para atender proximo cliente da lista
                Tarefa proxCliente = filaTarefas.remove(0);
                EventoFuturo evtFut = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this, proxCliente);
                //Event adicionado a lista de evntos futuros
                simulacao.addEventoFuturo(evtFut);
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
        }
        if (temp1 || temp2) {
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    mensagem.getTarefa().getOrigem(),
                    mensagem.getTarefa());
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    @Override
    public void atenderAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        //atualiza metricas dos usuarios globais
        //simulacao.getRedeDeFilas().getMetricasUsuarios().addMetricasUsuarios(escalonador.getMetricaUsuarios());
        //enviar resultados
        List<CentroServico> caminho = new ArrayList<CentroServico>(CS_Maquina.getMenorCaminhoIndireto(this, (CS_Processamento) mensagem.getOrigem()));
        Mensagem novaMensagem = new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        //novaMensagem.setProcessadorEscravo(new ArrayList<Tarefa>(tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<Tarefa>(filaTarefas));
        novaMensagem.getFilaEscravo().addAll(escalonador.getFilaTarefas());
        novaMensagem.setCaminho(caminho);
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.MENSAGEM,
                novaMensagem.getCaminho().remove(0),
                novaMensagem);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void atenderRetornoAtualizacao(Simulacao simulacao, Mensagem mensagem) {
        escalonador.resultadoAtualizar(mensagem);
    }
    
    @Override
    public void atenderFalha(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Integer getCargaTarefas() {
        return (escalonador.getFilaTarefas().size() + filaTarefas.size());
    }

    @Override
    public void atenderAckAlocacao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDesligamento(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}