/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas.servidores.implementacao;

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
public class CS_Maquina extends CS_Processamento implements Mensagens, Vertice {

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
    //TO DO: INCLUIR INFORMAÇÕES DE MEMÓRIA E DISCO
    
    /**
     * 
     * @param id
     * @param proprietario
     * @param PoderComputacional
     * @param numeroProcessadores
     * @param Ocupacao 
     */

    public CS_Maquina(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, 0);
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.mestres = new ArrayList<CS_Processamento>();
        this.processadoresDisponiveis = numeroProcessadores;
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
    }

    public CS_Maquina(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao, int numeroMaquina) {
        super(id, proprietario, PoderComputacional, numeroProcessadores, Ocupacao, numeroMaquina);
        this.conexoesEntrada = new ArrayList<CS_Comunicacao>();
        this.conexoesSaida = new ArrayList<CS_Comunicacao>();
        this.filaTarefas = new ArrayList<Tarefa>();
        this.mestres = new ArrayList<CS_Processamento>();
        this.processadoresDisponiveis = numeroProcessadores;
        this.tarefaEmExecucao = new ArrayList<Tarefa>(numeroProcessadores);
    }

    @Override
    public void addConexoesEntrada(CS_Link conexao) {
        this.conexoesEntrada.add(conexao);
    }

    @Override
    public void addConexoesSaida(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
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

    @Override
    public List<CS_Comunicacao> getConexoesSaida() {
        return this.conexoesSaida;
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
           cliente.iniciarEsperaProcessamento(simulacao.getTime(this));
            if (processadoresDisponiveis != 0) {
               // indica que recurso está ocupado
                processadoresDisponiveis--;
                //cria evento para iniciar o atendimento imediatamente
                EventoFuturo novoEvt = new EventoFuturo(
                        simulacao.getTime(this),
                        EventoFuturo.ATENDIMENTO,
                        this,
                        cliente);
                simulacao.addEventoFuturo(novoEvt);
            } else {
                filaTarefas.add(cliente);
            }
        }
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        cliente.finalizarEsperaProcessamento(simulacao.getTime(this));
        cliente.iniciarAtendimentoProcessamento(simulacao.getTime(this));
        tarefaEmExecucao.add(cliente);
        Double next = simulacao.getTime(this) + tempoProcessar(cliente.getTamProcessamento() - cliente.getMflopsProcessado());
        if (!falhas.isEmpty() && next > falhas.get(0)) {
            Double tFalha = falhas.remove(0);
            if (tFalha < simulacao.getTime(this)) {
                tFalha = simulacao.getTime(this);
            }
            Mensagem msg = new Mensagem(this, Mensagens.FALHAR, cliente);
            EventoFuturo evt = new EventoFuturo(
                    tFalha,
                    EventoFuturo.MENSAGEM,
                    this,
                    msg);
            simulacao.addEventoFuturo(evt);
        } else {
            falha = false;
            //Gera evento para atender proximo cliente da lista
            EventoFuturo evtFut = new EventoFuturo(
                    next,
                    EventoFuturo.SAÍDA,
                    this, cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
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
        if (mestres.contains(cliente.getOrigem())) {
            int index = mestres.indexOf(cliente.getOrigem());
            List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoMestre.get(index));
            cliente.setCaminho(caminho);
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        } else {
            //buscar menor caminho!!!
            CS_Processamento novoMestre = (CS_Processamento) cliente.getOrigem();
            List<CentroServico> caminho = new ArrayList<CentroServico>(
                    CS_Maquina.getMenorCaminhoIndireto(this, novoMestre));
            this.addMestre(novoMestre);
            this.caminhoMestre.add(caminho);
            cliente.setCaminho(new ArrayList<CentroServico>(caminho));
            //Gera evento para chegada da tarefa no proximo servidor
            EventoFuturo evtFut = new EventoFuturo(
                    simulacao.getTime(this),
                    EventoFuturo.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addEventoFuturo(evtFut);
        }
        if (filaTarefas.isEmpty()) {
            //Indica que está livre
            this.processadoresDisponiveis++;
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

    @Override
    public void requisicao(Simulacao simulacao, Mensagem mensagem, int tipo) {
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
    public void determinarCaminhos() throws LinkageError {
        //Instancia objetos
        caminhoMestre = new ArrayList<List>(mestres.size());
        //Busca pelos caminhos
        for (int i = 0; i < mestres.size(); i++) {
            caminhoMestre.add(i, CS_Maquina.getMenorCaminho(this, mestres.get(i)));
        }
        //verifica se todos os mestres são alcansaveis
        for (int i = 0; i < mestres.size(); i++) {
            if (caminhoMestre.get(i).isEmpty()) {
                throw new LinkageError();
            }
        }
    }

    @Override
    public void atenderCancelamento(Simulacao simulacao, Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            simulacao.removeEventoFuturo(EventoFuturo.SAÍDA, this, mensagem.getTarefa());
            tarefaEmExecucao.remove(mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
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
        //Incrementa porcentagem da tarefa processada
        mensagem.getTarefa().setMflopsProcessado(mflopsProcessados);
    }

    @Override
    public void atenderParada(Simulacao simulacao, Mensagem mensagem) {
        if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            //remover evento de saida do cliente do servidor
            boolean remover = simulacao.removeEventoFuturo(
                    EventoFuturo.SAÍDA,
                    this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
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
            tarefaEmExecucao.remove(mensagem.getTarefa());
            filaTarefas.add(mensagem.getTarefa());
        }
    }

    @Override
    public void atenderDevolucao(Simulacao simulacao, Mensagem mensagem) {
        boolean remover = filaTarefas.remove(mensagem.getTarefa());
        if (remover) {
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
        boolean remover = false;
        if (mensagem.getTarefa().getEstado() == Tarefa.PARADO) {
            remover = filaTarefas.remove(mensagem.getTarefa());
        } else if (mensagem.getTarefa().getEstado() == Tarefa.PROCESSANDO) {
            remover = simulacao.removeEventoFuturo(
                    EventoFuturo.SAÍDA,
                    this,
                    mensagem.getTarefa());
            //gerar evento para atender proximo cliente
            if (filaTarefas.isEmpty()) {
                //Indica que está livre
                this.processadoresDisponiveis++;
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
            tarefaEmExecucao.remove(mensagem.getTarefa());
        }
        if (remover) {
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
        //enviar resultados
        int index = mestres.indexOf(mensagem.getOrigem());
        List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoMestre.get(index));
        Mensagem novaMensagem = new Mensagem(this, mensagem.getTamComunicacao(), Mensagens.RESULTADO_ATUALIZAR);
        //Obtem informações dinâmicas
        novaMensagem.setProcessadorEscravo(new ArrayList<Tarefa>(tarefaEmExecucao));
        novaMensagem.setFilaEscravo(new ArrayList<Tarefa>(filaTarefas));
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void atenderFalha(Simulacao simulacao, Mensagem mensagem) {
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
                    EventoFuturo novoEvt = new EventoFuturo(
                            simulacao.getTime(this) + tempoRec,
                            EventoFuturo.ATENDIMENTO,
                            this,
                            tar);
                    simulacao.addEventoFuturo(novoEvt);
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

    @Override
    public void atenderAckAlocacao(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDesligamento(Simulacao simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}