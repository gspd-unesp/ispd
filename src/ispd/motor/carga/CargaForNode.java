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
 * CargaForNode.java
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
package ispd.motor.carga;

import NumerosAleatorios.GeracaoNumAleatorios;
import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.random.Distribution;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Descreve como gerar tarefas para um nó escalonador
 * @author denison
 */
public class CargaForNode extends GerarCarga {

    private String aplicacao;
    private String proprietario;
    private String escalonador;
    private int numeroTarefas;
    private Double minComputacao;
    private Double maxComputacao;
    private Double minComunicacao;
    private Double maxComunicacao;
    private int inicioIdentificadorTarefa;

    public CargaForNode(String aplicacao, String proprietario, String escalonador, int numeroTarefas, double maxComputacao, double minComputacao, double maxComunicacao, double minComunicacao) {
        this.inicioIdentificadorTarefa = 0;
        this.aplicacao = aplicacao;
        this.proprietario = proprietario;
        this.escalonador = escalonador;
        this.numeroTarefas = numeroTarefas;
        this.minComputacao = minComputacao;
        this.maxComputacao = maxComputacao;
        this.minComunicacao = minComunicacao;
        this.maxComunicacao = maxComunicacao;
    }

    public Vector toVector() {
        Vector temp = new Vector<Integer>(8);
        temp.add(0, aplicacao);
        temp.add(1, proprietario);
        temp.add(2, escalonador);
        temp.add(3, numeroTarefas);
        temp.add(4, maxComputacao);
        temp.add(5, minComputacao);
        temp.add(6, maxComunicacao);
        temp.add(7, minComunicacao);
        return temp;
    }

    @Override
    public List<Tarefa> toTarefaList(RedeDeFilas rdf) {
        List<Tarefa> tarefas = new ArrayList<Tarefa>();
        CS_Processamento mestre = null;
        int i = 0;
        boolean encontrou = false;
        while (!encontrou && i < rdf.getMestres().size()) {
            if (rdf.getMestres().get(i).getId().equals(this.escalonador)) {
                encontrou = true;
                mestre = rdf.getMestres().get(i);
            }
            i++;
        }
        if (encontrou) {
            Distribution gerador = new Distribution((int)System.currentTimeMillis());
            for (i = 0; i < this.getNumeroTarefas(); i++) {
                //Random sorteio = new Random();
                //double srt = sorteio.nextInt(this.maxComputacao.intValue()) + this.minComputacao;
                if(proprietario.equals("NoDelay")){
                    Tarefa tarefa = new Tarefa(
                        inicioIdentificadorTarefa,
                        proprietario,
                        aplicacao,
                        mestre,
                        gerador.twoStageUniform(minComunicacao, minComunicacao + (maxComunicacao - minComunicacao) / 2, maxComunicacao, 1),
                        0.0009765625 /*arquivo recebimento*/,
                        gerador.twoStageUniform(minComputacao, minComputacao + (maxComputacao - minComputacao) / 2, maxComputacao, 1),
                        gerador.nextExponential(5)+120);
                    tarefas.add(tarefa);
                }
                else{
                    Tarefa tarefa = new Tarefa(
                        inicioIdentificadorTarefa,
                        proprietario,
                        aplicacao,
                        mestre,
                        gerador.twoStageUniform(minComunicacao, minComunicacao + (maxComunicacao - minComunicacao) / 2, maxComunicacao, 1),
                        0.0009765625 /*arquivo recebimento*/,
                        gerador.twoStageUniform(minComputacao, minComputacao + (maxComputacao - minComputacao) / 2, maxComputacao, 1),
                        gerador.nextExponential(5));
                tarefas.add(tarefa);
                }
                
                inicioIdentificadorTarefa++;
            }
        }
        return tarefas;
    }

    @Override
    public String toString() {
        return String.format("%s %d %f %f %f %f",
                this.escalonador, this.numeroTarefas,
                this.maxComputacao, this.minComputacao,
                this.maxComunicacao, this.minComunicacao);
    }

    public static GerarCarga newGerarCarga(String entrada) {
        CargaForNode newObj = null;
        String[] valores = entrada.split(" ");
        String aplicacao = "application0";
        String proprietario = "user1";
        String escalonador = valores[0];
        int numeroTarefas = Integer.parseInt(valores[1]);
        double maxComputacao = Double.parseDouble(valores[2]);
        double minComputacao = Double.parseDouble(valores[3]);
        double maxComunicacao = Double.parseDouble(valores[4]);
        double minComunicacao = Double.parseDouble(valores[5]);
        newObj = new CargaForNode(aplicacao, proprietario, escalonador,
                numeroTarefas, maxComputacao, minComputacao, maxComunicacao, minComunicacao);
        return newObj;
    }

    @Override
    public int getTipo() {
        return GerarCarga.FORNODE;
    }

    //Gets e Sets
    
    public void setInicioIdentificadorTarefa(int inicioIdentificadorTarefa) {
        this.inicioIdentificadorTarefa = inicioIdentificadorTarefa;
    }
    
    public String getEscalonador() {
        return escalonador;
    }

    public void setEscalonador(String escalonador) {
        this.escalonador = escalonador;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    public Double getMaxComputacao() {
        return maxComputacao;
    }

    public void setMaxComputacao(double maxComputacao) {
        this.maxComputacao = maxComputacao;
    }

    public Double getMaxComunicacao() {
        return maxComunicacao;
    }

    public void setMaxComunicacao(double maxComunicacao) {
        this.maxComunicacao = maxComunicacao;
    }

    public Double getMinComputacao() {
        return minComputacao;
    }

    public void setMinComputacao(double minComputacao) {
        this.minComputacao = minComputacao;
    }

    public Double getMinComunicacao() {
        return minComunicacao;
    }

    public void setMinComunicacao(double minComunicacao) {
        this.minComunicacao = minComunicacao;
    }

    public Integer getNumeroTarefas() {
        return numeroTarefas;
    }

    public void setNumeroTarefas(int numeroTarefas) {
        this.numeroTarefas = numeroTarefas;
    }

    public String getProprietario() {
        return proprietario;
    }

    public void setProprietario(String proprietario) {
        this.proprietario = proprietario;
    }
}
