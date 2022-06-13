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
import ispd.motor.filas.servidores.implementacao.CS_Maquina;
import ispd.motor.filas.servidores.implementacao.CS_Mestre;
import ispd.motor.random.Distribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargaTrace extends GerarCarga {

    private static final int USER_FIELD_INDEX = 11;
    private static final int HEADER_SIZE = 5;
    private static final double RECEIVING_FILE_SIZE = 0.0009765625;
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

    private static double averageGridProcessingCapacity(
            final Iterable<? extends CS_Maquina> machines) {

        double media = 0;
        int i = 0;

        for (final var machine : machines) {
            media += machine.getPoderComputacional();
            i++;
        }

        return media / i;
    }

    private static void skipHeader(final BufferedReader bf) throws IOException {
        int i = 0;
        while (bf.ready() && i < CargaTrace.HEADER_SIZE) {
            bf.readLine();
            i++;
        }
    }

    private static void addTaskSwfType(
            final RedeDeFilas rdf,
            final Collection<? super Tarefa> tasks,
            final Collection<? super String> users,
            final Collection<? super Double> pComps,
            final Collection<? super Double> profiles,
            final Distribution random,
            final double mediaCap,
            final BufferedReader bf,
            final CS_Processamento master) throws IOException {

        final String[] fields = CargaTrace.parseFields(
                rdf, users, pComps, profiles, bf);

        final Tarefa tarefa = CargaTrace.addTaskToList(tasks, master,
                fs -> random.twoStageUniform(200, 5000, 25000, 0.5),
                fs -> Double.parseDouble(fs[7]) * mediaCap,
                fields);

        CargaTrace.afterAdd(fields, tarefa, master);
    }

    private static void afterAdd(
            final String[] fields,
            final Tarefa tarefa,
            final CentroServico master) {
        if (fields[5].contains("0") || fields[5].contains("5")) {
            tarefa.setLocalProcessamento(master);
            tarefa.cancelar(0);
        }
    }

    private static void addTaskIspdType(
            final RedeDeFilas rdf,
            final Collection<? super Tarefa> tasks,
            final Collection<? super String> users,
            final Collection<? super Double> pComps,
            final Collection<? super Double> profiles,
            final BufferedReader bf,
            final CS_Processamento master) throws IOException {
        final String[] campos = CargaTrace.parseFields(
                rdf, users, pComps, profiles, bf);

        CargaTrace.addTaskToList(tasks, master,
                fs -> Double.parseDouble(fs[9]),
                fs -> Double.parseDouble(fs[7]),
                campos);
    }

    private static String[] parseFields(
            final RedeDeFilas rdf,
            final Collection<? super String> users,
            final Collection<? super Double> pcomps,
            final Collection<? super Double> profiles,
            final BufferedReader bf) throws IOException {
        final String[] fields = bf.readLine().split("\"");

        if (!rdf.getUsuarios().contains(fields[CargaTrace.USER_FIELD_INDEX])
                && !users.contains(fields[CargaTrace.USER_FIELD_INDEX])) {
            users.add(fields[CargaTrace.USER_FIELD_INDEX]);
            profiles.add(100.0);
            pcomps.add(0.0);
        }

        return fields;
    }

    private static Tarefa addTaskToList(
            final Collection<? super Tarefa> tasks,
            final CS_Processamento master,
            final Function<String[], Double> sentFileSize,
            final Function<String[], Double> processingTime,
            final String[] fields) {
        final Tarefa tarefa = CargaTrace.makeTask(fields, master,
                sentFileSize.apply(fields),
                processingTime.apply(fields));

        tasks.add(tarefa);

        return tarefa;
    }

    private static Tarefa makeTask(
            final String[] fields,
            final CS_Processamento master,
            final double sentFileSize,
            final double processingTime
    ) {
        return new Tarefa(
                Integer.parseInt(fields[1]),
                fields[CargaTrace.USER_FIELD_INDEX],
                "application1",
                master,
                sentFileSize,
                CargaTrace.RECEIVING_FILE_SIZE,
                processingTime,
                Double.parseDouble(fields[3])
        );
    }

    @Override
    public List<Tarefa> toTarefaList(final RedeDeFilas rdf) {
        final List<Tarefa> tasks = new ArrayList<>(0);
        final List<String> users = new ArrayList<>(0);
        final List<Double> pComps = new ArrayList<>(0);
        final List<Double> profiles = new ArrayList<>(0);
        final int quantityPerMaster = this.taskCount / rdf.getMestres().size();
        final int remainder = this.taskCount % rdf.getMestres().size();
        final var random = new Distribution(System.currentTimeMillis());
        final var mediaCap =
                CargaTrace.averageGridProcessingCapacity(rdf.getMaquinas());

        try (final var bf = new BufferedReader(new FileReader(this.filePath))) {

            CargaTrace.skipHeader(bf);

            if ("SWF".equals(this.type) || "GWF".equals(this.type)) {

                for (final var master : rdf.getMestres()) {
                    for (int i = 0; i < quantityPerMaster; i++) {
                        CargaTrace.addTaskSwfType(rdf, tasks, users, pComps,
                                profiles, random, mediaCap, bf, master);
                    }
                }

                for (int i = 0; i < remainder; i++) {
                    CargaTrace.addTaskSwfType(rdf, tasks, users, pComps,
                            profiles,
                            random, mediaCap, bf, rdf.getMestres().get(0)
                    );
                }

            } else if ("iSPD".equals(this.type)) {

                for (final var master : rdf.getMestres()) {
                    for (int i = 0; i < quantityPerMaster; i++) {
                        CargaTrace.addTaskIspdType(rdf, tasks, users, pComps,
                                profiles, bf, master);
                    }
                }

                for (int i = 0; i < remainder; i++) {
                    CargaTrace.addTaskIspdType(rdf, tasks, users, pComps,
                            profiles, bf, rdf.getMestres().get(0));
                }

            }

            for (final var master : rdf.getMestres()) {
                ((CS_Mestre) master).getEscalonador().getMetricaUsuarios().addAllUsuarios(users, pComps, profiles);
            }

            rdf.getUsuarios().addAll(users);
            return tasks;

        } catch (final IOException ex) {
            Logger.getLogger(CargaTrace.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public String toString() {
        return this.file.getAbsolutePath();

    }

    @Override
    public int getTipo() {
        return GerarCarga.TRACE;
    }

    public File getFile() {
        return this.file;
    }

    public String getTraceType() {
        return this.type;
    }

    public Integer getNumberTasks() {
        return this.taskCount;
    }
}
