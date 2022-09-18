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
 * MetricasTarefa.java
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

/**
 *
 * @author denison
 */
public class MetricasTarefa {
    /**
     * Recebe tempo total que a tarefa permaneceu em um fila de um recurso de computação
     */
    private double tempoEsperaProc = 0;
    /**
     * Recebe tempo total que a tarefa permaneceu em um fila de um recurso de comunicação
     */
    private double tempoEsperaComu = 0;
    /**
     * Recebe tempo total que a tarefa gastou sendo computada no modelo
     */
    private double tempoProcessamento = 0;
    /**
     * Recebe tempo total que a tarefa gastou sendo transferida na rede modelada
     */
    private double tempoComunicacao = 0;
    
    private double eficiencia = 0;

    public void incTempoComunicacao(double tempoComunicacao) {
        this.tempoComunicacao += tempoComunicacao;
    }

    public void incTempoEsperaComu(double tempoEsperaComu) {
        this.tempoEsperaComu += tempoEsperaComu;
    }

    public void incTempoEsperaProc(double tempoEsperaProc) {
        this.tempoEsperaProc += tempoEsperaProc;
    }

    public void incTempoProcessamento(double tempoProcessamento) {
        this.tempoProcessamento += tempoProcessamento;
    }
    
    public void calcEficiencia(double capacidadeRecebida, double tamanhoTarefa){
        eficiencia = capacidadeRecebida / (tamanhoTarefa * tempoProcessamento);
    }

    public double getTempoComunicacao() {
        return tempoComunicacao;
    }

    public double getTempoEsperaComu() {
        return tempoEsperaComu;
    }

    public double getTempoEsperaProc() {
        return tempoEsperaProc;
    }

    public double getTempoProcessamento() {
        return tempoProcessamento;
    }

    public double getEficiencia() {
        return eficiencia;
    }
}
