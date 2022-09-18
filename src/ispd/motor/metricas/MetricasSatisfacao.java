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
 * MetricasSatisfacao.java
 * ---------------
 * (C) Copyright 2014, by Grupo de pesquisas em Sistemas Paralelos e Distribuídos da Unesp (GSPD).
 *
 * Original Author:  Cássio Henrique Volpatto Forte;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 
 * 09-Set-2014 : Version 2.0;
 *
 */
package ispd.motor.metricas;

import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Processamento;

/**
 *
 * @author cassio
 */
public class MetricasSatisfacao {

    private String usuario;
    private Double satisfacao;

    public MetricasSatisfacao(String usuario) {
        this.usuario = usuario;
        this.satisfacao = 0.0;
    }

    public Double getSatisfacao(Metricas metricas) {
        
        return satisfacao/metricas.getNumeroDeSimulacoes();
        
    }
    
    public Double getSatisfacao() {
        
        return satisfacao;
        
    }
    
    public void addSatisfacao(MetricasUsuarios metricasUsuarios) {
        Double suij = 0.0;
        for (Tarefa j : metricasUsuarios.getTarefasConcluidas(usuario)) {
            CS_Processamento maq = (CS_Processamento) j.getHistoricoProcessamento().get(0);
            suij += ((j.getTimeCriacao() + maq.tempoProcessar(j.getTamProcessamento())) / (j.getTempoFinal().get(j.getTempoFinal().size() - 1) - j.getTimeCriacao())) * (100);
        }
        this.satisfacao += suij/metricasUsuarios.getTarefasConcluidas(usuario).size();
    }

    public void addSatisfacao(MetricasUsuarios metricasUsuarios, CS_Processamento maqMedia) {
        Double suij = 0.0;
        for (Tarefa j : metricasUsuarios.getTarefasConcluidas(usuario)) {
            suij += ((j.getTimeCriacao() + maqMedia.tempoProcessar(j.getTamProcessamento())) / (j.getTempoFinal().get(j.getTempoFinal().size() - 1) - j.getTimeCriacao())) * (100);
        }
        this.satisfacao += suij/metricasUsuarios.getTarefasConcluidas(usuario).size();
    }
}
