/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Project Info:  http://gspd.dcce.ibilce.unesp.br/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * CS_Maquina.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.FutureEvent;
import ispd.motor.Mensagens;
import ispd.motor.Simulation;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison
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
    private List<Tarefa> historicoProcessamento;

    //TO DO: INCLUIR INFORMAÇÕES DE MEMÓRIA E DISCO

    /**
     * Constructor which specifies the machine configuration,
     * specifying the id, owner, computational power, core
     * count and load factor.
     * <p><br />
     * Using this constructor the machine number and the
     * energy consumption are both set as default to 0.
     *
     * @param id the id
     * @param owner the owner
     * @param computationalPower the computational power
     * @param coreCount the core count
     * @param loadFactor the load factor
     *
     * @see #CS_Maquina(String, String, double, int, double, int, double)
     *                  for specify the machine number and energy consumption.
     */
    public CS_Maquina(final String id,
                      final String owner,
                      final double computationalPower,
                      final int coreCount,
                      final double loadFactor) {
        this(id, owner, computationalPower, coreCount, loadFactor, 0, 0.0);
    }

    /**
     * Constructor which specifies the machine configuration,
     * specifying the id, owner, computational power, core
     * count, load factor and energy consumption.
     * <p><br />
     * Using this constructor the machine number is set as
     * default to 0.
     *
     * @param id the id
     * @param owner the owner
     * @param computationalPower the computational power
     * @param coreCount the core count
     * @param loadFactor the load factor
     * @param energy the energy consumption.
     *
     * @see #CS_Maquina(String, String, double, int, double, int, double)
     *                  for specify the machine number.
     */
    public CS_Maquina(final String id,
                      final String owner,
                      final double computationalPower,
                      final int coreCount,
                      final double loadFactor,
                      final double energy) {
        this(id, owner, computationalPower, coreCount, loadFactor, 0, energy);
    }

    /**
     * Constructor which specifies the machine configuration,
     * specifying the id, owner, computational power, core
     * count, load factor and machine number.
     * <p><br />
     * Using this constructor the energy consumption is set
     * as default to 0.
     *
     * @param id the id
     * @param owner the owner
     * @param computationalPower the computational power
     * @param coreCount the core count
     * @param loadFactor the load factor
     * @param machineNumber the machine number
     *
     * @see #CS_Maquina(String, String, double, int, double, int, double)
     *                  for specify the energy consumption
     */
    public CS_Maquina(final String id,
                      final String owner,
                      final double computationalPower,
                      final int coreCount,
                      final double loadFactor,
                      final int machineNumber) {
        this(id, owner, computationalPower, coreCount, loadFactor, machineNumber, 0.0);
    }

    /**
     * Constructor which specifies the machine configuration,
     * specifying the id, owner, computational power, core
     * count, load factor, machine number and energy consumption.
     *
     * @param id the id
     * @param owner the owner
     * @param computationalPower the computational power
     * @param coreCount the core count
     * @param loadFactor the load factor
     * @param machineNumber the machine number
     * @param energy the energy consumption
     */
    public CS_Maquina(final String id,
                      final String owner,
                      final double computationalPower,
                      final int coreCount,
                      final double loadFactor,
                      final int machineNumber,
                      final double energy) {
        super(id, owner, computationalPower, coreCount, loadFactor, machineNumber, energy);
        this.conexoesEntrada = new ArrayList<>();
        this.conexoesSaida = new ArrayList<>();
        this.filaTarefas = new ArrayList<>();
        this.mestres = new ArrayList<>();
        this.processadoresDisponiveis = coreCount;
        this.tarefaEmExecucao = new ArrayList<>(coreCount);
        this.historicoProcessamento = new ArrayList<>();
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
    public void chegadaDeCliente(Simulation simulacao, Tarefa cliente) {
        if (cliente.getEstado() != Tarefa.CANCELADO) {
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
            historicoProcessamento.add(cliente);
        }
    }

    @Override
    public void atendimento(Simulation simulacao, Tarefa cliente) {
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
        if (mestres.contains(cliente.getOrigem())) {
            int index = mestres.indexOf(cliente.getOrigem());
            List<CentroServico> caminho = new ArrayList<CentroServico>((List<CentroServico>) caminhoMestre.get(index));
            cliente.setCaminho(caminho);
            //Gera evento para chegada da tarefa no proximo servidor
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        } else {
            //buscar menor caminho!!!
            CS_Processamento novoMestre = (CS_Processamento) cliente.getOrigem();
            List<CentroServico> caminho = new ArrayList<CentroServico>(
                    CS_Maquina.getMenorCaminhoIndireto(this, novoMestre));
            this.addMestre(novoMestre);
            this.caminhoMestre.add(caminho);
            cliente.setCaminho(new ArrayList<CentroServico>(caminho));
            //Gera evento para chegada da tarefa no proximo servidor
            FutureEvent evtFut = new FutureEvent(
                    simulacao.getTime(this),
                    FutureEvent.CHEGADA,
                    cliente.getCaminho().remove(0),
                    cliente);
            //Event adicionado a lista de evntos futuros
            simulacao.addFutureEvent(evtFut);
        }
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
        mensagem.getTarefa().incMflopsDesperdicados(mflopsProcessados);
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
            double numCP = ((int) (mflopsProcessados / mensagem.getTarefa().getCheckPoint())) * mensagem.getTarefa().getCheckPoint();
            mensagem.getTarefa().setMflopsProcessado(numCP);
            //Incrementa desperdicio
            mensagem.getTarefa().incMflopsDesperdicados(mflopsProcessados - numCP);
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
                double numCP = ((int) (mflopsProcessados / tar.getCheckPoint())) * tar.getCheckPoint();
                tar.setMflopsProcessado(numCP);
                tar.incMflopsDesperdicados(mflopsProcessados - numCP);
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

    @Override
    public void atenderAckAlocacao(Simulation simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void atenderDesligamento(Simulation simulacao, Mensagem mensagem) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public List<Tarefa> getHistorico() {
        return this.historicoProcessamento;
    }
}
