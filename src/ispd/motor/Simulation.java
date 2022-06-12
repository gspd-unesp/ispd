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
 * Simulacao.java
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
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.metricas.Metricas;

import java.awt.Color;
import java.util.List;

/**
 * @author denison
 */
public abstract class Simulation {

    private final List<Tarefa> jobs;
    private final ProgressoSimulacao window;
    private RedeDeFilas queueNetwork;
    private RedeDeFilasCloud cloudQueueNetwork;

    public Simulation(ProgressoSimulacao window, RedeDeFilas queueNetwork, List<Tarefa> jobs) {
        this.jobs = jobs;
        this.queueNetwork = queueNetwork;
        this.window = window;
    }

    public Simulation(ProgressoSimulacao window, RedeDeFilasCloud cloudQueueNetwork, List<Tarefa> jobs) {
        this.jobs = jobs;
        this.cloudQueueNetwork = cloudQueueNetwork;
        this.window = window;
    }

    public ProgressoSimulacao getWindow() {
        return this.window;
    }

    public RedeDeFilasCloud getCloudQueueNetwork() {
        return this.cloudQueueNetwork;
    }

    public RedeDeFilas getQueueNetwork() {
        return this.queueNetwork;
    }

    public List<Tarefa> getJobs() {
        return this.jobs;
    }

    public abstract void simulate();

    public abstract double getTime(Object origin);

    public abstract void addFutureEvent(FutureEvent ev);

    public abstract boolean removeFutureEvent(int eventType, CentroServico eventServer, Client eventClient);

    public void addJob(Tarefa job) {
        this.jobs.add(job);
    }

    public void initSchedulers() {
        for (CS_Processamento master : this.queueNetwork.getMestres()) {
            ((CS_Mestre) master).getEscalonador().iniciar();
        }
    }

    public void initCloudAllocators() {
        for (CS_Processamento genericMaster : this.cloudQueueNetwork.getMestres()) {
            CS_VMM master = (CS_VMM) genericMaster;

            System.out.printf("VMM %s iniciando o alocador %s%n",
                    genericMaster.getId(), master.getAlocadorVM().toString());

            master.getAlocadorVM().iniciar();
        }
    }

    public void initCloudSchedulers() {
        for (CS_Processamento genericMaster : this.cloudQueueNetwork.getMestres()) {
            CS_VMM master = (CS_VMM) genericMaster;

            System.out.printf("VMM %s iniciando escalonador %s%n",
                    genericMaster.getId(), master.getEscalonador().toString());

            master.getEscalonador().iniciar();
            master.instanciarCaminhosVMs();
        }
    }

    public void createRouting() {
        for (CS_Processamento master : this.queueNetwork.getMestres()) {
            Mestre temp = (Mestre) master;

            // Give access to the master of the queue of future events.
            temp.setSimulacao(this);

            // Find the shortest path between the master and its slaves.
            master.determinarCaminhos();
        }
        if (this.queueNetwork.getMaquinas() == null || this.queueNetwork.getMaquinas().isEmpty()) {
            this.window.println("The model has no processing slaves.", Color.orange);
        } else {
            // Find the shortest path between each slave and the master.
            for (CS_Maquina machine : this.queueNetwork.getMaquinas()) {
                machine.determinarCaminhos();
            }
        }
    }

    public Metricas getMetrics() {
        Metricas metric = new Metricas(this.queueNetwork, getTime(null), this.jobs);

        this.window.print("Getting Results.");
        this.window.print(" -> ");

        this.window.incProgresso(5);

        this.window.println("OK", Color.green);
        this.window.print("Falha injetada");
        this.window.println("OK", Color.red);

        return metric;
    }

    public Metricas getCloudMetrics() {
        this.window.print("Getting Results.");
        this.window.print(" -> ");

        Metricas metric = new Metricas(this.cloudQueueNetwork, getTime(null), this.jobs);

        this.window.incProgresso(5);
        this.window.println("OK", Color.green);

        return metric;
    }
}
