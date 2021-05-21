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
 * CS_Comunicacao.java
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
package ispd.motor.filas.servidores;

import ispd.motor.metricas.MetricasComunicacao;

/**
 * Classe abstrata que representa os servidores de comunicação do modelo de fila,
 * Esta classe possui atributos referente a este ripo de servidor, e indica como
 * calcular o tempo gasto para transmitir uma tarefa.
 * @author denison
 */
public abstract class CS_Comunicacao extends CentroServico {
    /**
     * Identificador do centro de serviço, deve ser o mesmo do modelo icônico
     */
    private double larguraBanda;
    private double ocupacao;
    private double latencia;
    private MetricasComunicacao metrica;
    private double larguraBandaDisponivel;

    public CS_Comunicacao(String id, double LarguraBanda, double Ocupacao, double Latencia) {
        this.larguraBanda = LarguraBanda;
        this.ocupacao = Ocupacao;
        this.latencia = Latencia;
        this.metrica = new MetricasComunicacao(id);
        this.larguraBandaDisponivel = this.larguraBanda - (this.larguraBanda * this.ocupacao);
    }

    public MetricasComunicacao getMetrica() {
        return metrica;
    }

    @Override
    public String getId(){
        return metrica.getId();
    }

    public double getLarguraBanda() {
        return larguraBanda;
    }

    public double getLatencia() {
        return latencia;
    }

    public double getOcupacao() {
        return ocupacao;
    }
    
    /**
     * Retorna o tempo gasto
     * @param Mbits
     */
    public double tempoTransmitir(double Mbits){
        return ( Mbits / larguraBandaDisponivel ) + latencia;
    }

}
