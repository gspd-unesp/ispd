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
 * CS_Processamento.java
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

import ispd.alocacaoVM.VMM;
import ispd.motor.filas.servidores.implementacao.CS_Link;
import ispd.motor.filas.servidores.implementacao.CS_Switch;
import ispd.motor.filas.servidores.implementacao.CS_Internet;
import ispd.escalonador.Mestre;
import ispd.motor.metricas.MetricasProcessamento;
import java.util.ArrayList;
import java.util.List;
import ispd.gui.auxiliar.ParesOrdenadosUso;
import java.util.Collections;

/**
 * Classe abstrata que representa os servidores de processamento do modelo de fila,
 * Esta classe possui atributos referente a este ripo de servidor, e indica como
 * calcular o tempo gasto para processar uma tarefa.
 * @author denison
 */
public abstract class CS_Processamento extends CentroServico {

    /**
     * Identificador do centro de serviço, deve ser o mesmo do modelo icônico
     */
    private double poderComputacional;
    private int numeroProcessadores;
    private double Ocupacao;
    private double PoderComputacionalDisponivelPorProcessador;
    private MetricasProcessamento metrica;
    private List<ParesOrdenadosUso> lista_pares = new ArrayList<ParesOrdenadosUso>();
    private Double consumoEnergia;
    
    public CS_Processamento(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao, int numeroMaquina) {
        this.poderComputacional = PoderComputacional;
        this.numeroProcessadores = numeroProcessadores;
        this.Ocupacao = Ocupacao;
        this.metrica = new MetricasProcessamento(id, numeroMaquina, proprietario);
        this.PoderComputacionalDisponivelPorProcessador =
                (this.poderComputacional - (this.poderComputacional * this.Ocupacao))
                / this.numeroProcessadores;
        
    }
    
    public CS_Processamento(String id, String proprietario, double PoderComputacional, int numeroProcessadores, double Ocupacao, int numeroMaquina, Double energia) {
        this.poderComputacional = PoderComputacional;
        this.numeroProcessadores = numeroProcessadores;
        this.Ocupacao = Ocupacao;
        this.metrica = new MetricasProcessamento(id, numeroMaquina, proprietario);
        this.consumoEnergia = energia;
        this.PoderComputacionalDisponivelPorProcessador =
                (this.poderComputacional - (this.poderComputacional * this.Ocupacao))
                / this.numeroProcessadores;
    }
    
    public int getnumeroMaquina(){
        return metrica.getnumeroMaquina();
    }
    
    public Double getConsumoEnergia(){
        return this.consumoEnergia;
    }
    
    public double getOcupacao() {
        return Ocupacao;
    }

    public double getPoderComputacional() {
        return poderComputacional;
    }

    @Override
    public String getId() {
        return metrica.getId();
    }
    
   
    public String getProprietario() {
        return metrica.getProprietario();
    }

    public int getNumeroProcessadores() {
        return numeroProcessadores;
    }

    public double tempoProcessar(double Mflops) {
        return (Mflops / PoderComputacionalDisponivelPorProcessador);
    }
    
    public double getMflopsProcessados(double tempoProc) {
        return (tempoProc * PoderComputacionalDisponivelPorProcessador);
    }

    public MetricasProcessamento getMetrica() {
        return metrica;
    }

    public void setPoderComputacionalDisponivelPorProcessador(double PoderComputacionalDisponivelPorProcessador) {
        this.PoderComputacionalDisponivelPorProcessador = PoderComputacionalDisponivelPorProcessador;
    }

    public void setPoderComputacional(double poderComputacional) {
        this.poderComputacional = poderComputacional;
    }
    
    
    

    /**
     * Utilizado para buscar as rotas entre os recursos e armazenar em uma tabela,
     * deve retornar em erro se não encontrar nenhum caminho
     */
    public abstract void determinarCaminhos() throws LinkageError;
    /**
     * Método que determina todas as conexões entre dois recursos
     * podendo haver conexões indiretas, passando por diverssos elementos de comunicação
     * @param inicio CS_Internet inicial
     * @param fim Recurso que está sendo buscado um caminho
     * @return lista de caminho existentes
     */
    protected List<List> getCaminhos(CS_Comunicacao inicio, CentroServico fim, List itensVerificados) {
        List<List> lista = new ArrayList<List>();
        //Adiciona camino para elementos diretamente conectados
        if (inicio instanceof CS_Link && inicio.getConexoesSaida().equals(fim)) {
            List novoCaminho = new ArrayList<CentroServico>();
            novoCaminho.add(inicio);
            novoCaminho.add(fim);
            lista.add(novoCaminho);
        } //Adiciona caminho para elementos diretamentes conectados por um switch
        else if (inicio instanceof CS_Switch) {
            CS_Switch Switch = (CS_Switch) inicio;
            for (CentroServico saida : Switch.getConexoesSaida()) {
                if (saida.equals(fim)) {
                    List novoCaminho = new ArrayList<CentroServico>();
                    novoCaminho.add(inicio);
                    novoCaminho.add(fim);
                    lista.add(novoCaminho);
                }
            }
        } //faz chamada recursiva para novo caminho
        else if (inicio.getConexoesSaida() instanceof CS_Internet && !itensVerificados.contains(inicio.getConexoesSaida())) {
            CS_Internet net = (CS_Internet) inicio.getConexoesSaida();
            itensVerificados.add(net);
            List<List> recursivo = null;
            for (CS_Link link : net.getConexoesSaida()) {
                recursivo = getCaminhos(link, fim, itensVerificados);
                if (recursivo != null) {
                    for (List caminho : recursivo) {
                        List novoCaminho = new ArrayList<CentroServico>();
                        novoCaminho.add(inicio);
                        novoCaminho.add(net);
                        novoCaminho.addAll(caminho);
                        lista.add(novoCaminho);
                    }
                }
            }
            itensVerificados.remove(net);
        }
        if (lista.isEmpty()) {
            return null;
        } else {
            return lista;
        }
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento
     * @param origem recurso origem
     * @param destino recurso destino
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<CentroServico> getMenorCaminho(CS_Processamento origem, CS_Processamento destino) {
        //cria vetor com distancia acumulada
        List<CentroServico> nosExpandidos = new ArrayList<CentroServico>();
        List<Object[]> caminho = new ArrayList<Object[]>();
        CentroServico atual = origem;
        //armazena valor acumulado até atingir o nó atual
        Double acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof CS_Link) {
                Object caminhoItem[] = new Object[4];
                caminhoItem[0] = atual;
                if (atual.getConexoesSaida() instanceof CS_Processamento && atual.getConexoesSaida() != destino) {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                } else if (atual.getConexoesSaida() instanceof CS_Comunicacao) {
                    CS_Comunicacao cs = (CS_Comunicacao) atual.getConexoesSaida();
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                } else {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                ArrayList<CentroServico> lista = (ArrayList<CentroServico>) atual.getConexoesSaida();
                for (CentroServico cs : lista) {
                    Object caminhoItem[] = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof CS_Processamento && cs != destino) {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    } else if (cs instanceof CS_Comunicacao) {
                        CS_Comunicacao comu = (CS_Comunicacao) cs;
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            Object[] menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (Object[] obj : caminho) {
                Double menor = (Double) menorCaminho[1];
                Double objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (CentroServico) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            List<CentroServico> menorCaminho = new ArrayList<CentroServico>();
            List<CentroServico> inverso = new ArrayList<CentroServico>();
            Object[] obj;
            while (atual != origem) {
                int i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (CentroServico) obj[0];
            }
            for (int j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return null;
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento indiretamente conectados
     * passando por mestres no caminho
     * @param origem recurso origem
     * @param destino recurso destino
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<CentroServico> getMenorCaminhoIndireto(CS_Processamento origem, CS_Processamento destino) {
        //cria vetor com distancia acumulada
        ArrayList<CentroServico> nosExpandidos = new ArrayList<CentroServico>();
        ArrayList<Object[]> caminho = new ArrayList<Object[]>();
        CentroServico atual = origem;
        //armazena valor acumulado até atingir o nó atual
        Double acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof CS_Link) {
                Object caminhoItem[] = new Object[4];
                caminhoItem[0] = atual;
                if (atual.getConexoesSaida() instanceof CS_Comunicacao) {
                    CS_Comunicacao cs = (CS_Comunicacao) atual.getConexoesSaida();
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                } else if (atual.getConexoesSaida() instanceof Mestre || atual.getConexoesSaida() == destino) {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                } else {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                ArrayList<CentroServico> lista = (ArrayList<CentroServico>) atual.getConexoesSaida();
                for (CentroServico cs : lista) {
                    Object caminhoItem[] = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof CS_Comunicacao) {
                        CS_Comunicacao comu = (CS_Comunicacao) cs;
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else if (cs instanceof Mestre || cs == destino) {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            Object[] menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (Object[] obj : caminho) {
                Double menor = (Double) menorCaminho[1];
                Double objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (CentroServico) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            List<CentroServico> menorCaminho = new ArrayList<CentroServico>();
            List<CentroServico> inverso = new ArrayList<CentroServico>();
            Object[] obj;
            while (atual != origem) {
                int i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (CentroServico) obj[0];
            }
            for (int j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return null;
    }
    
    public static List<CentroServico> getMenorCaminhoCloud(CS_Processamento origem, CS_Processamento destino) {
        //cria vetor com distancia acumulada
        List<CentroServico> nosExpandidos = new ArrayList<CentroServico>();
        List<Object[]> caminho = new ArrayList<Object[]>();
        CentroServico atual = origem;
        //armazena valor acumulado até atingir o nó atual
        Double acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof CS_Link) {
                Object caminhoItem[] = new Object[4];
                caminhoItem[0] = atual;
                if (atual.getConexoesSaida() instanceof CS_Processamento && atual.getConexoesSaida() != destino) {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                } else if (atual.getConexoesSaida() instanceof CS_Comunicacao) {
                    CS_Comunicacao cs = (CS_Comunicacao) atual.getConexoesSaida();
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                } else {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                ArrayList<CentroServico> lista = (ArrayList<CentroServico>) atual.getConexoesSaida();
                for (CentroServico cs : lista) {
                    Object caminhoItem[] = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof CS_Processamento && cs != destino) {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    } else if (cs instanceof CS_Comunicacao) {
                        CS_Comunicacao comu = (CS_Comunicacao) cs;
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            Object[] menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (Object[] obj : caminho) {
                Double menor = (Double) menorCaminho[1];
                Double objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (CentroServico) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            List<CentroServico> menorCaminho = new ArrayList<CentroServico>();
            List<CentroServico> inverso = new ArrayList<CentroServico>();
            Object[] obj;
            while (atual != origem) {
                int i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (CentroServico) obj[0];
            }
            for (int j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return null;
    }

    /**
     * Retorna o menor caminho entre dois recursos de processamento indiretamente conectados
     * passando por mestres no caminho
     * @param origem recurso origem
     * @param destino recurso destino
     * @return caminho completo a partir do primeiro link até o recurso destino
     */
    public static List<CentroServico> getMenorCaminhoIndiretoCloud(CS_Processamento origem, CS_Processamento destino) {
        //cria vetor com distancia acumulada
        ArrayList<CentroServico> nosExpandidos = new ArrayList<CentroServico>();
        ArrayList<Object[]> caminho = new ArrayList<Object[]>();
        CentroServico atual = origem;
        //armazena valor acumulado até atingir o nó atual
        Double acumulado = 0.0;
        do {
            //busca valores das conexões de saida do recurso atual e coloca no vetor caminho
            if (atual instanceof CS_Link) {
                Object caminhoItem[] = new Object[4];
                caminhoItem[0] = atual;
                if (atual.getConexoesSaida() instanceof CS_Comunicacao) {
                    CS_Comunicacao cs = (CS_Comunicacao) atual.getConexoesSaida();
                    caminhoItem[1] = cs.tempoTransmitir(10000) + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                } else if (atual.getConexoesSaida() instanceof VMM || atual.getConexoesSaida() == destino) {
                    caminhoItem[1] = 0.0 + acumulado;
                    caminhoItem[2] = atual.getConexoesSaida();
                } else {
                    caminhoItem[1] = Double.MAX_VALUE;
                    caminhoItem[2] = null;
                }
                caminhoItem[3] = acumulado;
                caminho.add(caminhoItem);
            } else {
                ArrayList<CentroServico> lista = (ArrayList<CentroServico>) atual.getConexoesSaida();
                for (CentroServico cs : lista) {
                    Object caminhoItem[] = new Object[4];
                    caminhoItem[0] = atual;
                    if (cs instanceof CS_Comunicacao) {
                        CS_Comunicacao comu = (CS_Comunicacao) cs;
                        caminhoItem[1] = comu.tempoTransmitir(10000) + acumulado;
                        caminhoItem[2] = cs;
                    } else if (cs instanceof VMM || cs == destino) {
                        caminhoItem[1] = 0.0 + acumulado;
                        caminhoItem[2] = cs;
                    } else {
                        caminhoItem[1] = Double.MAX_VALUE;
                        caminhoItem[2] = null;
                    }
                    caminhoItem[3] = acumulado;
                    caminho.add(caminhoItem);
                }
            }
            //Marca que o nó atual foi expandido
            nosExpandidos.add(atual);
            //Inicia variavel de menor caminho com maior valor possivel
            Object[] menorCaminho = new Object[4];
            menorCaminho[0] = null;
            menorCaminho[1] = Double.MAX_VALUE;
            menorCaminho[2] = null;
            menorCaminho[3] = Double.MAX_VALUE;
            //busca menor caminho não expandido
            for (Object[] obj : caminho) {
                Double menor = (Double) menorCaminho[1];
                Double objAtual = (Double) obj[1];
                if (menor > objAtual && !nosExpandidos.contains(obj[2])) {
                    menorCaminho = obj;
                }
            }
            //atribui valor a atual com resultado da busca do menor caminho
            atual = (CentroServico) menorCaminho[2];
            acumulado = (Double) menorCaminho[1];
        } while (atual != null && atual != destino);
        if (atual == destino) {
            List<CentroServico> menorCaminho = new ArrayList<CentroServico>();
            List<CentroServico> inverso = new ArrayList<CentroServico>();
            Object[] obj;
            while (atual != origem) {
                int i = 0;
                do {
                    obj = caminho.get(i);
                    i++;
                } while (obj[2] != atual);
                inverso.add(atual);
                atual = (CentroServico) obj[0];
            }
            for (int j = inverso.size() - 1; j >= 0; j--) {
                menorCaminho.add(inverso.get(j));
            }
            return menorCaminho;
        }
        return null;
    }
    
    
     public void setTempoProcessamento(double inicio, double fim){
        ParesOrdenadosUso par = new ParesOrdenadosUso(inicio,fim);
        lista_pares.add(par); 
    }
    
    public List getListaProcessamento(){
        Collections.sort(lista_pares);
        return (this.lista_pares);
    }

}
