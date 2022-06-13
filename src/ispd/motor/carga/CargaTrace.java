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
 * CargaTrace.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e
 * Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Diogo Tavares;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.motor.carga;

import ispd.arquivo.xml.TraceXML;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.random.Distribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargaTrace extends GerarCarga {

    private final String type;
    private final File file;
    private final String filePath;
    private final int taskCount;

    public CargaTrace(
            final File file,
            final int taskCount,
            final String type) {

        this.file = file;
        this.filePath = file.getAbsolutePath();
        new TraceXML(this.filePath);
        this.taskCount = taskCount;
        this.type = type;
    }

    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        return new TraceLoadHelper(rdf, this.type, this.taskCount).toTaskList(this.filePath);
    }

    @Override
    public String toString() {
        return this.file.getAbsolutePath();

    }

    @Override
    public int getTipo() {
        return GerarCarga.TRACE;
    }

    public String getTraceType() {
        return this.type;
    }

    public File getFile() {
        return this.file;
    }

    public Integer getNumberTasks() {
        return this.taskCount;
    }

    private static class TraceLoadHelper {

        private static final int USER_FIELD_INDEX = 11;
        private static final int HEADER_SIZE = 5;
        private static final double RECEIVING_FILE_SIZE = 0.0009765625;
        private final List<Tarefa> tasks = new ArrayList<>(0);
        private final List<String> users = new ArrayList<>(0);
        private final List<Double> pComps = new ArrayList<>(0);
        private final List<Double> profiles = new ArrayList<>(0);
        private final int taskCount;
        private final String traceType;
        private final RedeDeFilas queueNetwork;
        private final Distribution random =
                new Distribution(System.currentTimeMillis());

        TraceLoadHelper(
                final RedeDeFilas rdf,
                final String traceType,
                final int taskCount) {
            this.queueNetwork = rdf;
            this.traceType = traceType;
            this.taskCount = taskCount;
        }

        static void skipHeader(final BufferedReader bf) throws IOException {
            int i = 0;
            while (bf.ready() && i < TraceLoadHelper.HEADER_SIZE) {
                bf.readLine();
                i++;
            }
        }

        static void afterAdd(
                final String[] fields,
                final Tarefa tarefa,
                final CentroServico master) {
            if (fields[5].contains("0") || fields[5].contains("5")) {
                tarefa.setLocalProcessamento(master);
                tarefa.cancelar(0);
            }
        }

        static Tarefa makeTask(
                final String[] fields,
                final CS_Processamento master,
                final double sentFileSize,
                final double processingTime
        ) {
            return new Tarefa(
                    Integer.parseInt(fields[1]),
                    fields[TraceLoadHelper.USER_FIELD_INDEX],
                    "application1",
                    master,
                    sentFileSize,
                    TraceLoadHelper.RECEIVING_FILE_SIZE,
                    processingTime,
                    Double.parseDouble(fields[3])
            );
        }

        String[] parseFields(final BufferedReader bf) throws IOException {
            final String[] fields = bf.readLine().split("\"");

            if (!this.queueNetwork.getUsuarios().contains(fields[TraceLoadHelper.USER_FIELD_INDEX])
                    && !this.users.contains(fields[TraceLoadHelper.USER_FIELD_INDEX])) {
                this.users.add(fields[TraceLoadHelper.USER_FIELD_INDEX]);
                this.profiles.add(100.0);
                this.pComps.add(0.0);
            }

            return fields;
        }

        Tarefa addTaskToList(
                final CS_Processamento master,
                final Function<String[], Double> sentFileSize,
                final Function<String[], Double> processingTime,
                final String[] fields) {
            final Tarefa tarefa = TraceLoadHelper.makeTask(fields, master,
                    sentFileSize.apply(fields),
                    processingTime.apply(fields));

            this.tasks.add(tarefa);

            return tarefa;
        }

        void addTaskIspdType(
                final BufferedReader bf,
                final CS_Processamento master) throws IOException {
            final String[] fields = this.parseFields(bf);

            this.addTaskToList(master,
                    fs -> Double.parseDouble(fs[9]),
                    fs -> Double.parseDouble(fs[7]),
                    fields);
        }

        void addTaskSwfType(
                final BufferedReader bf,
                final CS_Processamento master) throws IOException {

            final String[] fields = this.parseFields(bf);

            final Tarefa tarefa = this.addTaskToList(
                    master,
                    fs -> this.random.twoStageUniform(200, 5000, 25000, 0.5),
                    fs -> Double.parseDouble(fs[7]) * this.calculateAverageCapacity(),
                    fields);

            TraceLoadHelper.afterAdd(fields, tarefa, master);
        }

        private List<Tarefa> toTaskList(final String path) {
            final int tasksPerMaster =
                    this.taskCount / this.queueNetwork.getMestres().size();
            final int remainder =
                    this.taskCount % this.queueNetwork.getMestres().size();

            try (final var bf = new BufferedReader(new FileReader(path))) {

                TraceLoadHelper.skipHeader(bf);

                if ("SWF".equals(this.traceType) || "GWF".equals(this.traceType)) {

                    for (final var master : this.queueNetwork.getMestres()) {
                        for (int i = 0; i < tasksPerMaster; i++) {
                            this.addTaskSwfType(bf, master);
                        }
                    }

                    for (int i = 0; i < remainder; i++) {
                        final var first = this.queueNetwork.getMestres().get(0);
                        this.addTaskSwfType(bf, first);
                    }

                } else if ("iSPD".equals(this.traceType)) {

                    for (final var master : this.queueNetwork.getMestres()) {
                        for (int i = 0; i < tasksPerMaster; i++) {
                            this.addTaskIspdType(bf, master);
                        }
                    }

                    for (int i = 0; i < remainder; i++) {
                        final var first = this.queueNetwork.getMestres().get(0);
                        this.addTaskIspdType(bf, first);
                    }
                }

                for (final var master : this.queueNetwork.getMestres()) {
                    ((CS_Mestre) master).getEscalonador().getMetricaUsuarios().addAllUsuarios(this.users, this.pComps, this.profiles);
                }

                this.queueNetwork.getUsuarios().addAll(this.users);
                return this.tasks;

            } catch (final IOException ex) {
                Logger.getLogger(CargaTrace.class.getName())
                        .log(Level.SEVERE, null, ex);
            }

            return null;
        }

        private double calculateAverageCapacity() {

            double media = 0;
            int i = 0;

            for (final var machine : this.queueNetwork.getMaquinas()) {
                media += machine.getPoderComputacional();
                i++;
            }

            return media / i;
        }
    }
}
