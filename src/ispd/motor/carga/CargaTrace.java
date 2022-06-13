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
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargaTrace extends GerarCarga {

    private static final int HEADER_SIZE = 5;
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
        final List<Tarefa> tasks = new ArrayList<>(0);
        final List<String> users = new ArrayList<>(0);
        final List<Double> pcomp = new ArrayList<>(0);
        final List<Double> profile = new ArrayList<>(0);
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
                        this.addSomeTask(rdf, tasks, users, pcomp, profile,
                                random, mediaCap, bf, master);
                    }
                }

                for (int i = 0; i < remainder; i++) {
                    this.addSomeTask(rdf, tasks, users, pcomp, profile,
                            random, mediaCap, bf, rdf.getMestres().get(0)
                    );
                }

            } else if ("iSPD".equals(this.type)) {

                for (final CS_Processamento mestre : rdf.getMestres()) {
                    for (int i = 0; i < quantityPerMaster; i++) {
                        this.addSomeOtherTask(rdf, tasks, users, pcomp,
                                profile, bf, mestre);
                    }
                }

                for (int i = 0; i < remainder; i++) {
                    this.addSomeOtherTask(rdf, tasks, users, pcomp, profile,
                            bf, rdf.getMestres().get(0));
                }

            }

            for (final var master : rdf.getMestres()) {
                ((CS_Mestre) master).getEscalonador().getMetricaUsuarios().addAllUsuarios(users, pcomp, profile);
            }

            rdf.getUsuarios().addAll(users);
            return tasks;

        } catch (final IOException ex) {
            Logger.getLogger(CargaTrace.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return null;
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

    private void addSomeTask(
            final RedeDeFilas rdf,
            final Collection<? super Tarefa> tasks,
            final Collection<? super String> users,
            final Collection<? super Double> pcomp,
            final Collection<? super Double> profile,
            final Distribution random,
            final double mediaCap,
            final BufferedReader bf,
            final CS_Processamento mestre) throws IOException {

        final String[] campos = parseFields(rdf, users, pcomp, profile, bf);
        final Tarefa tarefa = makeTask(campos, mestre,
                random.twoStageUniform(200, 5000, 25000,
                        0.5), Double.parseDouble(campos[7]) * mediaCap);
        tasks.add(tarefa);
        if (campos[5].contains("0") || campos[5].contains("5")) {
            tarefa.setLocalProcessamento(mestre);
            tarefa.cancelar(0);
        }
    }

    private String[] parseFields(RedeDeFilas rdf,
                                Collection<? super String> users, Collection<
            ? super Double> pcomp, Collection<? super Double> profile,
                                BufferedReader bf) throws IOException {
        final String[] campos = bf.readLine().split("\"");
        if (!rdf.getUsuarios().contains(campos[11]) && !users.contains(campos[11])) {
            users.add(campos[11]);
            profile.add(100.0);
            pcomp.add(0.0);
        }
        return campos;
    }

    private void addSomeOtherTask(final RedeDeFilas rdf,
                                  final List<Tarefa> tasks,
                                  final List<String> users,
                                  final List<Double> pcomp,
                                  final List<Double> profile,
                                  final BufferedReader bf,
                                  final CS_Processamento mestre) throws IOException {
        final String[] campos = bf.readLine().split("\"");
        if (!rdf.getUsuarios().contains(campos[11]) && !users.contains(campos[11])) {
            users.add(campos[11]);
            profile.add(100.0);
            pcomp.add(0.0);
        }
        final Tarefa tarefa = makeTask(campos, mestre,
                Double.parseDouble(campos[9]), Double.parseDouble(campos[7]));
        tasks.add(tarefa);
    }

    private Tarefa makeTask(String[] campos, CS_Processamento mestre,
                            double random, double campos1) {
        return new Tarefa(
                Integer.parseInt(campos[1]),
                campos[11],
                "application1",
                mestre,
                random,
                0.0009765625,
                campos1,
                Double.parseDouble(campos[3])
        );
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
