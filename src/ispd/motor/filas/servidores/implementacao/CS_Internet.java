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
 * CS_Internet.java
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
package ispd.motor.filas.servidores.implementacao;

import ispd.motor.Simulacao;
import ispd.motor.EventoFuturo;
import ispd.motor.filas.Mensagem;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison
 */
public class CS_Internet extends CS_Comunicacao implements Vertice {

    private List<CS_Link> conexoesEntrada;
    private List<CS_Link> conexoesSaida;
    private Integer pacotes = 0;

    public CS_Internet(String id, double LarguraBanda, double Ocupacao, double Latencia) {
        super(id, LarguraBanda, Ocupacao, Latencia);
        this.conexoesEntrada = new ArrayList<CS_Link>();
        this.conexoesSaida = new ArrayList<CS_Link>();
    }

    public List<CS_Link> getConexoesEntrada() {
        return conexoesEntrada;
    }

    @Override
    public List<CS_Link> getConexoesSaida() {
        return conexoesSaida;
    }

    @Override
    public void addConexoesEntrada(CS_Link conexao) {
        this.conexoesEntrada.add(conexao);
    }

    @Override
    public void addConexoesSaida(CS_Link conexao) {
        this.conexoesSaida.add(conexao);
    }

    @Override
    public void chegadaDeCliente(Simulacao simulacao, Tarefa cliente) {
        //cria evento para iniciar o atendimento imediatamente
        EventoFuturo novoEvt = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.ATENDIMENTO,
                this,
                cliente);
        simulacao.addEventoFuturo(novoEvt);
    }

    @Override
    public void atendimento(Simulacao simulacao, Tarefa cliente) {
        pacotes++;
        cliente.iniciarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para atender proximo cliente da lista
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this) + tempoTransmitir(cliente.getTamComunicacao()),
                EventoFuturo.SAÍDA,
                this, cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void saidaDeCliente(Simulacao simulacao, Tarefa cliente) {
        pacotes--;
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Incrementa o tempo de transmissão no pacote
        cliente.finalizarAtendimentoComunicacao(simulacao.getTime(this));
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this),
                EventoFuturo.CHEGADA,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }

    @Override
    public void requisicao(Simulacao simulacao, Mensagem cliente, int tipo) {
        //Incrementa o número de Mbits transmitido por este link
        this.getMetrica().incMbitsTransmitidos(cliente.getTamComunicacao());
        //Incrementa o tempo de transmissão
        double tempoTrans = this.tempoTransmitir(cliente.getTamComunicacao());
        this.getMetrica().incSegundosDeTransmissao(tempoTrans);
        //Gera evento para chegada da tarefa no proximo servidor
        EventoFuturo evtFut = new EventoFuturo(
                simulacao.getTime(this) + tempoTrans,
                EventoFuturo.MENSAGEM,
                cliente.getCaminho().remove(0), cliente);
        //Event adicionado a lista de evntos futuros
        simulacao.addEventoFuturo(evtFut);
    }
    
    @Override
    public Integer getCargaTarefas() {
        return pacotes;
    }
}
