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
 * CargaTrace.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
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

import NumerosAleatorios.GeracaoNumAleatorios;
import ispd.arquivo.interpretador.cargas.Interpretador;
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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diogo Tavares
 */
public class CargaTrace extends GerarCarga {

    private String tipo, caminho;
    private File file;
    private int num_tasks;

    public CargaTrace(File file, int num_tasks, String tipo) {
        this.file = file;
        this.caminho = file.getAbsolutePath();
        TraceXML interpret = new TraceXML(caminho);
        this.num_tasks = num_tasks;
        this.tipo = tipo;
    }

    private double MediaCapProcGrade(List<CS_Maquina> maquinas) {
        double media = 0;
        int i = 0;
        for (CS_Maquina cS_Maquina : maquinas) {
            media += cS_Maquina.getPoderComputacional();
            i++;
        }
        media = media / i;
        return media;
    }

    @Override
    public List<Tarefa> toTarefaList(RedeDeFilas rdf) {
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        List<String> users = new ArrayList<String>();
        List<Double> pcomp = new ArrayList<Double>();
        List<Double> perfil= new ArrayList<Double>();
        int quantidadePorMestre = this.num_tasks / rdf.getMestres().size();
        int resto = this.num_tasks % rdf.getMestres().size();
        Distribution gerador = new Distribution((int)System.currentTimeMillis());
        double mediaCap = MediaCapProcGrade(rdf.getMaquinas());
        try {
            BufferedReader in = new BufferedReader(new FileReader(caminho));
            String aux;
            int j = 0;
            //escapa o cabeçalho
            while (in.ready() && j < 5) {
                in.readLine();
                j++;
            }
            if (tipo.equals("SWF") || tipo.equals("GWF")) {
                for (CS_Processamento mestre : rdf.getMestres()) {
                    for (int i = 0; i < quantidadePorMestre; i++) {
                        aux = in.readLine();
                        String[] campos = aux.split("\"");
                        if (rdf.getUsuarios().contains(campos[11]) == false && users.contains(campos[11]) == false) {
                            users.add(campos[11]);
                            perfil.add(100.0);
                            pcomp.add(0.0);
                        }
                        Tarefa tarefa = new Tarefa(
                                (int) Integer.parseInt(campos[1]),
                                campos[11],
                                "application1",
                                mestre,
                                gerador.twoStageUniform(200, 5000, 25000, 0.5),
                                0.0009765625 /*arquivo recebimento*/,
                                (double) (Double.parseDouble(campos[7]) * mediaCap),
                                Double.parseDouble(campos[3])/*tempo de criação*/);
                        tarefas.add(tarefa);
                        if (campos[5].contains("0") || campos[5].contains("5")) {
                            tarefa.setLocalProcessamento(mestre);
                            tarefa.cancelar(0);
                        }
                    }
                }
                for (int i = 0; i < resto; i++) {
                    aux = in.readLine();
                    String[] campos = aux.split("\"");
                    if (rdf.getUsuarios().contains(campos[11]) == false && users.contains(campos[11]) == false) {
                        users.add(campos[11]);
                        perfil.add(100.0);
                        pcomp.add(0.0);
                    }
                    Tarefa tarefa = new Tarefa(
                            (int) Integer.parseInt(campos[1]),
                            campos[11],
                            "application1",
                            rdf.getMestres().get(0),
                            gerador.twoStageUniform(200, 5000, 25000, 0.5),
                            0.0009765625 /*arquivo recebimento*/,
                            (double) (Double.parseDouble(campos[7]) * mediaCap),
                            Double.parseDouble(campos[3])/*tempo de criação*/);
                    tarefas.add(tarefa);
                    if (campos[5].contains("0") || campos[5].contains("5")) {
                        tarefa.setLocalProcessamento(rdf.getMestres().get(0));
                        tarefa.cancelar(0);
                    }
                }
                for (CS_Processamento mestre : rdf.getMestres()) {
                    CS_Mestre mestreaux = (CS_Mestre) mestre;
                    mestreaux.getEscalonador().getMetricaUsuarios().addAllUsuarios(users, pcomp, perfil);
                }
                rdf.getUsuarios().addAll(users);
                return tarefas;

            } else if (tipo.equals("iSPD")) {
                for (CS_Processamento mestre : rdf.getMestres()) {
                    for (int i = 0; i < quantidadePorMestre; i++) {
                        aux = in.readLine();
                        String[] campos = aux.split("\"");
                        if (rdf.getUsuarios().contains(campos[11]) == false && users.contains(campos[11]) == false) {
                            users.add(campos[11]);
                            perfil.add(100.0);
                            pcomp.add(0.0);
                        }
                        Tarefa tarefa = new Tarefa(
                                (int) Integer.parseInt(campos[1]),
                                campos[11],
                                "application1",
                                mestre,
                                (double) Double.parseDouble(campos[9]),
                                0.0009765625 /*arquivo recebimento*/,
                                (double) Double.parseDouble(campos[7]),
                                Double.parseDouble(campos[3])/*tempo de criação*/);
                        tarefas.add(tarefa);
                    }

                }
                for (int i = 0; i < resto; i++) {
                    aux = in.readLine();
                    String[] campos = aux.split("\"");
                    if (rdf.getUsuarios().contains(campos[11]) == false && users.contains(campos[11]) == false) {
                        users.add(campos[11]);
                        perfil.add(100.0);
                        pcomp.add(0.0);
                    }
                    Tarefa tarefa = new Tarefa(
                            (int) Integer.parseInt(campos[1]),
                            campos[11],
                            "application1",
                            rdf.getMestres().get(0),
                            (double) Double.parseDouble(campos[9]),
                            0.0009765625 /*arquivo recebimento*/,
                            (double) Double.parseDouble(campos[7]),
                            Double.parseDouble(campos[3])/*tempo de criação*/);
                    tarefas.add(tarefa);
                }
                for (CS_Processamento mestre : rdf.getMestres()) {
                    CS_Mestre mestreaux = (CS_Mestre) mestre;
                    mestreaux.getEscalonador().getMetricaUsuarios().addAllUsuarios(users, pcomp,perfil);
                }
                rdf.getUsuarios().addAll(users);
                return tarefas;

            }

        } catch (IOException ex) {
            Logger.getLogger(CargaTrace.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getTraceType() {
        return tipo;
    }
    
    public Integer getNumberTasks() {
        return num_tasks;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();

    }

    @Override
    public int getTipo() {
        return GerarCarga.TRACE;
    }
}
