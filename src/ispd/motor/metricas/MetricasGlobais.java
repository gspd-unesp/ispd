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
 * MetricasGlobais.java
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
package ispd.motor.metricas;

import ispd.motor.filas.RedeDeFilas;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author denison
 */
public class MetricasGlobais implements Serializable {

    private double tempoSimulacao;
    private double satisfacaoMedia;
    private double ociosidadeComputacao;
    private double ociosidadeComunicacao;
    private double eficiencia;
    private int total;

    public MetricasGlobais(RedeDeFilas redeDeFilas, double tempoSimulacao, List<Tarefa> tarefas) {
        this.tempoSimulacao = tempoSimulacao;
        this.satisfacaoMedia = 100;
        this.ociosidadeComputacao = getOciosidadeComputacao(redeDeFilas);
        this.ociosidadeComunicacao = getOciosidadeComunicacao(redeDeFilas);
        this.eficiencia = getEficiencia(tarefas);
        this.total = 0;
    }

    public MetricasGlobais() {
        this.tempoSimulacao = 0;
        this.satisfacaoMedia = 0;
        this.ociosidadeComputacao = 0;
        this.ociosidadeComunicacao = 0;
        this.eficiencia = 0;
        this.total = 0;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public double getOciosidadeComputacao() {
        return ociosidadeComputacao;
    }

    public double getOciosidadeComunicacao() {
        return ociosidadeComunicacao;
    }

    public double getSatisfacaoMedia() {
        return satisfacaoMedia;
    }

    public double getTempoSimulacao() {
        return tempoSimulacao;
    }

    private double getOciosidadeComputacao(RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (CS_Processamento maquina : redeDeFilas.getMaquinas()) {
            double aux = maquina.getMetrica().getSegundosDeProcessamento();
            aux = (this.getTempoSimulacao() - aux);
            tempoLivreMedio += aux;//tempo livre
            aux = maquina.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getMaquinas().size();
        return (tempoLivreMedio * 100) / getTempoSimulacao();
    }

    private double getOciosidadeComunicacao(RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (CS_Comunicacao link : redeDeFilas.getLinks()) {
            double aux = link.getMetrica().getSegundosDeTransmissao();
            aux = (this.getTempoSimulacao() - aux);
            tempoLivreMedio += aux; //tempo livre
            aux = link.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getLinks().size();
        return (tempoLivreMedio * 100) / getTempoSimulacao();
    }

    private double getEficiencia(List<Tarefa> tarefas) {
        double somaEfic = 0;
        for (Tarefa tar : tarefas) {
            somaEfic += tar.getMetricas().getEficiencia();
        }
        return somaEfic / tarefas.size();
        /*
         double tempoUtil = 0.0;
         double tempoMedio = 0.0;
         for (CS_Processamento maquina : redeDeFilas.getMaquinas()) {
         double aux = maquina.getMetrica().getSegundosDeProcessamento();
         aux = (this.getTempoSimulacao() - aux);//tempo livre
         aux = maquina.getOcupacao() * aux;//tempo processando sem ser tarefa
         tempoUtil = aux + maquina.getMetrica().getSegundosDeProcessamento();
         tempoMedio += tempoUtil / this.getTempoSimulacao();
         }
         tempoMedio = tempoMedio / redeDeFilas.getMaquinas().size();
         return tempoMedio; 
         */
    }

    public void setTempoSimulacao(double tempoSimulacao) {
        this.tempoSimulacao = tempoSimulacao;
    }

    public void setSatisfacaoMedia(double satisfacaoMedia) {
        this.satisfacaoMedia = satisfacaoMedia;
    }

    public void setOciosidadeComputacao(double ociosidadeComputacao) {
        this.ociosidadeComputacao = ociosidadeComputacao;
    }

    public void setOciosidadeComunicacao(double ociosidadeComunicacao) {
        this.ociosidadeComunicacao = ociosidadeComunicacao;
    }

    public void setEficiencia(double eficiencia) {
        this.eficiencia = eficiencia;
    }

    public void add(MetricasGlobais global) {
        tempoSimulacao += global.getTempoSimulacao();
        satisfacaoMedia += global.getSatisfacaoMedia();
        ociosidadeComputacao += global.getOciosidadeComputacao();
        ociosidadeComunicacao += global.getOciosidadeComunicacao();
        eficiencia += global.getEficiencia();
        total++;
    }

    @Override
    public String toString() {
        int totalTemp = 1;
        if (total > 0) {
            totalTemp = total;
        }
        String texto = "\t\tSimulation Results\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", tempoSimulacao / totalTemp);
        texto += String.format("\tSatisfaction = %g %%\n", satisfacaoMedia / totalTemp);
        texto += String.format("\tIdleness of processing resources = %g %%\n", ociosidadeComputacao / totalTemp);
        texto += String.format("\tIdleness of communication resources = %g %%\n", ociosidadeComunicacao / totalTemp);
        texto += String.format("\tEfficiency = %g %%\n", eficiencia / totalTemp);
        if (eficiencia / totalTemp > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (eficiencia / totalTemp > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
        return texto;
    }
}
