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
 * EventoFuturo.java
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
package ispd.motor;

import ispd.motor.filas.Client;
import ispd.motor.filas.servidores.CentroServico;

/**
 * Classe que representa os eventos que alteram o estado do modelo simulado
 *
 * @author denison
 */
public class FutureEvent implements Comparable<FutureEvent> {

    public static final int CHEGADA = 1;
    public static final int ATENDIMENTO = 2;
    public static final int SAIDA = 3;
    public static final int ESCALONAR = 4;
    public static final int MENSAGEM = 5;
    public static final int SAIDA_MENSAGEM = 6;
    public static final int ALOCAR_VMS = 7;


    private final Double creationTime;
    private final int eventType;
    private final CentroServico resource;
    private final Client client;

    /**
     * Criacao de novo evento
     *
     * @param time      tempo do relógio em que foi criada
     * @param eventType tipo do evento criado
     * @param resource  servidor que executará o evento
     * @param client    cliente do evento
     */
    public FutureEvent(double time, int eventType, CentroServico resource, Client client) {
        this.creationTime = time;
        this.resource = resource;
        this.eventType = eventType;
        this.client = client;
    }

    /**
     * Informa o tipo do evento
     *
     * @return Retorna o tipo do evento de acordo com as constantes da classe
     */
    public int getType() {
        return this.eventType;
    }

    /**
     * Retorna recurso que realiza a ação
     *
     * @return recurso que deve executar ação
     */
    public CentroServico getServidor() {
        return this.resource;
    }

    /**
     * Retorna tarefa alvo da ação
     *
     * @return cliente do evento
     */
    public Client getClient() {
        return this.client;
    }

    public Double getCreationTime() {
        return this.creationTime;
    }

    /**
     * Comparação necessaria para utilizar PriorityQueue
     *
     * @param o evento que será comparado
     * @return 0 se valores iguais, um menor que 0 se "o" inferior, e maior que 0 se "o" for maior.
     */
    public int compareTo(FutureEvent o) {
        return this.creationTime.compareTo(o.creationTime);
    }
}
