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
 * SimulacaoGrafica.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Denison Menezes (for GSPD);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 14-Out-2014 : Version 2.0.1;
 *
 */
package ispd.motor;

import ispd.motor.filas.Client;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 * Realiza o controle da simulação junto a uma interface gráfica,
 * permitindo acompanhar cada passo da simulação de forma interativa
 *
 * @author denison
 */
public class GraphicSimulation extends Simulation {

    private double time;
    private double increment;
    private boolean shouldEnd;
    private boolean shouldStop;
    private final RedeDeFilas queueNetwork;
    private final List<Tarefa> jobs;
    private final PriorityQueue<FutureEvent> futureEvents;
    private final JLabel timeLabel;

    public GraphicSimulation(ProgressoSimulacao window, JLabel timeLabel, RedeDeFilas queueNetwork, List<Tarefa> jobs, double sleep) throws IllegalArgumentException {
        super(window, queueNetwork, jobs);

        this.time = 0;
        this.timeLabel = timeLabel;
        this.increment = sleep;
        this.shouldEnd = false;
        this.shouldStop = false;
        this.futureEvents = new PriorityQueue<>();

        this.queueNetwork = queueNetwork;
        if (queueNetwork == null) {
            throw new IllegalArgumentException("The model has no icons.");
        } else if (queueNetwork.getMestres() == null || queueNetwork.getMestres().isEmpty()) {
            throw new IllegalArgumentException("The model has no Masters.");
        } else if (queueNetwork.getLinks() == null || queueNetwork.getLinks().isEmpty()) {
            window.println("The model has no Networks.", Color.orange);
        }

        this.jobs = jobs;
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("One or more  workloads have not been configured.");
        }
    }

    @Override
    public void simulate() {
        initSchedulers();

        addFutureEventsFromJobs(this.jobs);

        if (shouldUpdateSchedulers()) {
            updateSchedulersAndRunSimulation();
        } else {
            runSimulation();
        }

        getWindow().incProgresso(35);
        getWindow().println("Simulation completed.", Color.green);
    }

    public void addFutureEventsFromJobs(List<Tarefa> jobs) {
        for (Tarefa job : jobs) {
            FutureEvent ev = new FutureEvent(job.getTimeCriacao(), FutureEvent.CHEGADA, job.getOrigem(), job);
            this.futureEvents.add(ev);
        }
    }

    @Override
    public void addFutureEvent(FutureEvent futureEvent) {
        this.futureEvents.offer(futureEvent);
    }

    @Override
    public boolean removeFutureEvent(int eventType, CentroServico eventServer, Client eventClient) {
        // Remove exit event of the client from the server.
        for (FutureEvent ev : this.futureEvents) {
            if (ev.getType() == eventType
                    && ev.getServidor().equals(eventServer)
                    && ev.getClient().equals(eventClient)) {
                this.futureEvents.remove(ev);
                return true;
            }
        }
        return false;
    }

    @Override
    public double getTime(Object origin) {
        return this.time;
    }

    public boolean shouldStop() {
        return this.shouldStop;
    }

    public void setShouldStop(boolean shouldStop) {
        this.shouldStop = shouldStop;
    }

    public boolean shouldEnd() {
        return this.shouldEnd;
    }

    public void setShouldEnd(boolean end) {
        this.shouldEnd = end;
    }

    private boolean shouldUpdateSchedulers() {
        for (CS_Processamento genericMaster : this.queueNetwork.getMestres()) {
            CS_Mestre master = (CS_Mestre) genericMaster;
            if (master.getEscalonador().getTempoAtualizar() != null) {
                return true;
            }
        }
        return false;
    }

    public void setIncrement(double inc) {
        this.increment = inc;
    }

    private void runSimulation() {
        double next = 0;
        while (!this.futureEvents.isEmpty() && !this.shouldEnd) {
            if (this.time > next) {
                try {
                    while (this.shouldStop) {
                        Thread.sleep(50);
                    }
                    Thread.sleep(500);
                    next += this.increment;
                    this.timeLabel.setText("Time: " + String.format("%.2f", this.time) + " s");
                } catch (InterruptedException ex) {
                    Logger.getLogger(GraphicSimulation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            commonLoop();
        }
    }

    /** Executa o laço de repetição responsavel por atender todos eventos da
     * simulação, e adiciona o evento para atualizar os escalonadores.
     */
    private void updateSchedulersAndRunSimulation() {
        List<Object[]> masterUpdateArray = new ArrayList<>();
        for (CS_Processamento genericMaster : this.queueNetwork.getMestres()) {
            CS_Mestre master = (CS_Mestre) genericMaster;
            if (master.getEscalonador().getTempoAtualizar() != null) {
                Object[] item = new Object[3];

                item[0] = master;
                item[1] = master.getEscalonador().getTempoAtualizar();
                item[2] = master.getEscalonador().getTempoAtualizar();

                masterUpdateArray.add(item);
            }
        }
        double next = 0;
        while (!this.futureEvents.isEmpty()) {
            if (this.time > next) {
                try {
                    Thread.sleep(500);
                    next += this.increment;
                    this.timeLabel.setText("Time: " + String.format("%.2f", this.time) + " s");
                } catch (InterruptedException ex) {
                    Logger.getLogger(GraphicSimulation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            // recupera o próximo evento e o executa. executa estes eventos de acordo com sua ordem de chegada de forma
            // a evitar a execução de um evento antes de outro que seria criado anteriormente
            for (Object[] object : masterUpdateArray) {
                if ((Double) object[2] < this.futureEvents.peek().getCreationTime()) {
                    CS_Mestre master = (CS_Mestre) object[0];
                    for (CS_Processamento slave : master.getEscalonador().getEscravos()) {
                        master.atualizar(slave, (Double) object[2]);
                    }
                    object[2] = (Double) object[2] + (Double) object[1];
                }
            }
            commonLoop();
        }
    }

    // recupera o próximo evento e o executa. executa estes eventos de acordo com sua ordem de chegada de forma a evitar
    // a execução de um evento antes de outro que seria criado anteriormente
    private void commonLoop() {
        // TODO: Learn what this do
        FutureEvent actualEvent = this.futureEvents.poll();
        this.time = actualEvent.getCreationTime();
        switch (actualEvent.getType()) {
            case FutureEvent.CHEGADA:
                actualEvent.getServidor().chegadaDeCliente(this, (Tarefa) actualEvent.getClient());
                break;
            case FutureEvent.ATENDIMENTO:
                actualEvent.getServidor().atendimento(this, (Tarefa) actualEvent.getClient());
                break;
            case FutureEvent.SAIDA:
                actualEvent.getServidor().saidaDeCliente(this, (Tarefa) actualEvent.getClient());
                break;
            case FutureEvent.ESCALONAR:
                actualEvent.getServidor().requisicao(this, null, FutureEvent.ESCALONAR);
                break;
            default:
                actualEvent.getServidor().requisicao(this, (Mensagem) actualEvent.getClient(), actualEvent.getType());
                break;
        }
    }
}