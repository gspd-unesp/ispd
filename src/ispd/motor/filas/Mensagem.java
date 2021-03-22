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
 * Mensagem.java
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
package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.List;

/**
 *
 * @author denison
 */
public class Mensagem implements Cliente {
    
    private int tipo;
    private Tarefa tarefa;
    private CentroServico origem;
    private List<CentroServico> caminho;
    private double tamComunicacao;
    private List<Tarefa> filaEscravo;
    private List<Tarefa> processadorEscravo;

    public Mensagem(CS_Processamento origem, int tipo) {
        this.origem = origem;
        this.tipo = tipo;
        this.tamComunicacao = 0.011444091796875;
    }
    
    public Mensagem(CS_Processamento origem, int tipo, Tarefa tarefa) {
        this.origem = origem;
        this.tipo = tipo;
        this.tamComunicacao = 0.011444091796875;
        this.tarefa = tarefa;
    }
    
    public Mensagem(CS_Processamento origem, double tamComunicacao, int tipo) {
        this.origem = origem;
        this.tipo = tipo;
        this.tamComunicacao = tamComunicacao; 
    }
    
    @Override
    public double getTamComunicacao() {
        return tamComunicacao;
    }

    @Override
    public double getTamProcessamento() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CentroServico getOrigem() {
        return origem;
    }

    @Override
    public List<CentroServico> getCaminho() {
        return caminho;
    }

    @Override
    public void setCaminho(List<CentroServico> caminho) {
        this.caminho = caminho;
    }

    public List<Tarefa> getFilaEscravo() {
        return filaEscravo;
    }

    public void setFilaEscravo(List<Tarefa> filaEscravo) {
        this.filaEscravo = filaEscravo;
    }

    public List<Tarefa> getProcessadorEscravo() {
        return processadorEscravo;
    }

    public void setProcessadorEscravo(List<Tarefa> processadorEscravo) {
        this.processadorEscravo = processadorEscravo;
    }
    
    public int getTipo(){
        return tipo;
    }
    
    public Tarefa getTarefa(){
        return tarefa;
    }

    @Override
    public double getTimeCriacao() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
