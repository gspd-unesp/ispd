/* ==========================================================
 * iSPD : iconic Simulator of Parallel and Distributed System
 * ==========================================================
 *
 * (C) Copyright 2010-2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 *  USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------
 * CargaForNode.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
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
package ispd.motor.carga;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 * Descreve como gerar tarefas para um nó escalonador
 */
public class CargaForNode extends GerarCarga {

    private static final double ARQUIVO_RECEBIMENTO = 0.0009765625;
    private final String application;
    private final int taskCount;
    private final Double minComputation;
    private final Double maxComputation;
    private final Double minCommunication;
    private final Double maxCommunication;
    private String owner;
    private String scheduler;
    private int taskIdentifierStart = 0;

    public CargaForNode(final String application, final String owner,
                        final String scheduler, final int taskCount,
                        final double maxComputation,
                        final double minComputation,
                        final double maxCommunication,
                        final double minCommunication) {
        this.application = application;
        this.owner = owner;
        this.scheduler = scheduler;
        this.taskCount = taskCount;
        this.minComputation = minComputation;
        this.maxComputation = maxComputation;
        this.minCommunication = minCommunication;
        this.maxCommunication = maxCommunication;
    }

    static GerarCarga newGerarCarga(final String s) {
        final String[] values = s.split(" ");
        final String scheduler = values[0];
        final int taskCount = Integer.parseInt(values[1]);
        // TODO: flip max, min order
        final double maxComputation = Double.parseDouble(values[2]);
        final double minComputation = Double.parseDouble(values[3]);
        final double maxCommunication = Double.parseDouble(values[4]);
        final double minCommunication = Double.parseDouble(values[5]);
        return new CargaForNode(
                "application0",
                "user1",
                scheduler,
                taskCount,
                maxComputation,
                minComputation,
                maxCommunication,
                minCommunication
        );
    }

    public Vector toVector() {
        final Vector temp = new Vector<Integer>(8);
        temp.add(0, this.application);
        temp.add(1, this.owner);
        temp.add(2, this.scheduler);
        temp.add(3, this.taskCount);
        temp.add(4, this.maxComputation);
        temp.add(5, this.minComputation);
        temp.add(6, this.maxCommunication);
        temp.add(7, this.minCommunication);
        return temp;
    }

    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        final List<Tarefa> tasks = new ArrayList<>(0);
        this.findMaster(rdf)
                .ifPresent(master -> this.addTasksToList(tasks, master));
        return tasks;
    }

    private Optional<CS_Processamento> findMaster(final RedeDeFilas rdf) {
        int i = 0;
        while (i < rdf.getMestres().size()) {
            if (rdf.getMestres().get(i).getId().equals(this.scheduler)) {
                return Optional.of(rdf.getMestres().get(i));
            }
            i++;
        }
        return Optional.empty();
    }

    private void addTasksToList(
            final Collection<? super Tarefa> tasks,
            final CS_Processamento master) {

        final Distribution random =
                new Distribution(System.currentTimeMillis());

        for (int i = 0; i < this.getNumeroTarefas(); i++) {
            if ("NoDelay".equals(this.owner)) {
                tasks.add(this.noDelayOwner(master, random, 120));
            } else {
                tasks.add(this.noDelayOwner(master, random, 0));
            }

            this.taskIdentifierStart++;
        }
    }

    public Integer getNumeroTarefas() {
        return this.taskCount;
    }

    private Tarefa noDelayOwner(
            final CS_Processamento master,
            final Distribution random,
            final int delay) {

        return new Tarefa(
                this.taskIdentifierStart,
                this.owner,
                this.application,
                master,
                CargaForNode.fromTwoStageUniform(
                        random,
                        this.minCommunication,
                        this.maxCommunication
                ),
                CargaForNode.ARQUIVO_RECEBIMENTO,
                CargaForNode.fromTwoStageUniform(
                        random,
                        this.minComputation,
                        this.maxComputation
                ),
                random.nextExponential(5) + delay
        );
    }

    private static double fromTwoStageUniform(
            final Distribution random,
            final Double min,
            final Double max) {

        return random.twoStageUniform(min, min + (max - min) / 2, max, 1);
    }

    @Override
    public String toString() {
        return String.format("%s %d %f %f %f %f",
                this.scheduler, this.taskCount,
                this.maxComputation, this.minComputation,
                this.maxCommunication, this.minCommunication
        );
    }

    @Override
    public int getTipo() {
        return GerarCarga.FORNODE;
    }

    void setInicioIdentificadorTarefa(final int taskIdentifierStart) {
        this.taskIdentifierStart = taskIdentifierStart;
    }

    public String getEscalonador() {
        return this.scheduler;
    }

    public void setEscalonador(final String scheduler) {
        this.scheduler = scheduler;
    }

    public String getAplicacao() {
        return this.application;
    }

    public Double getMaxComputacao() {
        return this.maxComputation;
    }

    public Double getMaxComunicacao() {
        return this.maxCommunication;
    }

    public Double getMinComputacao() {
        return this.minComputation;
    }

    public Double getMinComunicacao() {
        return this.minCommunication;
    }

    public String getProprietario() {
        return this.owner;
    }

    public void setProprietario(final String owner) {
        this.owner = owner;
    }
}
