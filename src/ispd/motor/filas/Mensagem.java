/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ispd.motor.filas;

import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.CentroServico;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author denison_usuario
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
        this.caminho = new ArrayList<CentroServico>();
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
