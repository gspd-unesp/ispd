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
 * SimulacaoSequencial.java
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
package ispd.motor;

import ispd.escalonador.Mestre;
import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author denison
 */
public class SimulacaoSequencial extends Simulation {

    private double time = 0;
    private PriorityQueue<FutureEvent> eventos;
    
    public SimulacaoSequencial(ProgressoSimulacao janela, RedeDeFilas redeDeFilas, List<Tarefa> tarefas) throws IllegalArgumentException {
        super(janela, redeDeFilas,tarefas);
        this.time = 0;
        this.eventos = new PriorityQueue<FutureEvent>();

        if (redeDeFilas == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (redeDeFilas.getMestres() == null || redeDeFilas.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (redeDeFilas.getLinks() == null || redeDeFilas.getLinks().isEmpty()) {
            janela.println("The model has no Networks.", Color.orange);
        }
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
        janela.print("Creating routing.");
        janela.print(" -> ");
        janela.print("Creating failuresSS.");
        /**
         * Trecho de código que implementa o roteamento entre os mestres e os seus respectivos escravos
         */
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            Mestre temp = (Mestre) mst;
            //Cede acesso ao mestre a fila de eventos futuros
            temp.setSimulacao(this);
            //Encontra menor caminho entre o mestre e seus escravos
            mst.determinarCaminhos(); //mestre encontra caminho para seus escravos
        }
        
        janela.incProgresso(5);
        janela.println("OK", Color.green);
        
        if (redeDeFilas.getMaquinas() == null || redeDeFilas.getMaquinas().isEmpty()) {
            janela.println("The model has no processing slaves.", Color.orange);
        } else {
            for (CS_Maquina maq : redeDeFilas.getMaquinas()) {
                //Encontra menor caminho entre o escravo e seu mestre
                maq.determinarCaminhos();//escravo encontra caminhos para seu mestre
            }
        }
        //fim roteamento
        janela.incProgresso(5);
    }

    @Override
    public void simulate() {
        //inicia os escalonadores
        initSchedulers();
        //adiciona chegada das tarefas na lista de eventos futuros
        addEventos(getJobs());
        if (atualizarEscalonadores()) {
            realizarSimulacaoAtualizaTime();
        } else {
            realizarSimulacao();
        }
        getWindow().incProgresso(30);
        getWindow().println("Simulation completed.", Color.green);
        //Centralizando métricas de usuários
        //for (CS_Processamento mestre : redeDeFilas.getMestres()) {
            //CS_Mestre mst = (CS_Mestre) mestre;
            //janela.println(mst.getId());
            //janela.println(mst.getEscalonador().getMetricaUsuarios().toString());
            //redeDeFilas.getMetricasUsuarios().addMetricasUsuarios(mst.getEscalonador().getMetricaUsuarios());
        //}
        //janela.println(redeDeFilas.getMetricasUsuarios().toString());
    }

    public void addEventos(List<Tarefa> tarefas) {
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
        for (CS_Processamento mst : getQueueNetwork().getMestres()) {
            CS_Mestre mestre = (CS_Mestre) mst;
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
                default:
                    eventoAtual.getServidor().requisicao(this, (Mensagem) eventoAtual.getClient(), eventoAtual.getType());
                    break;
            }
        }
    }
}
