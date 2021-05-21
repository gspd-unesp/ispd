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
 * Metricas.java
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
import ispd.motor.filas.RedeDeFilasCloud;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import ispd.motor.filas.servidores.implementacao.CS_MaquinaCloud;
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.io.Serializable;
import ispd.motor.filas.Tarefa;
import ispd.motor.filas.servidores.CS_Comunicacao;
import ispd.motor.filas.servidores.CS_Processamento;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author denison
 */
public class Metricas implements Serializable {

    private int numeroDeSimulacoes;
    private RedeDeFilas redeDeFilas;
    private List<Tarefa> tarefas;
    /**
     * Armazena métricas obtidas da simulação
     */
    private MetricasGlobais metricasGlobais;
    private List<String> usuarios;
    private Map<String, MetricasComunicacao> metricasComunicacao;
    private Map<String, MetricasProcessamento> metricasProcessamento;
    private Map<String, MetricasAlocacao> metricasAlocacao;
    private Map<String, MetricasCusto> metricasCusto;
    private Map<String, Double> metricasSatisfacao;
    private Map<String, Integer> tarefasConcluidas;
    private double tempoMedioFilaComunicacao;
    private double tempoMedioComunicacao;
    private double tempoMedioFilaProcessamento;
    private double tempoMedioProcessamento;
    private double MflopsDesperdicio;
    private int numTarefasCanceladas;
    private int numTarefas;

    //Maps de Satisfacao, Energia e Preempção
    private Map<String, BigDecimal> satisfacaoGeralSim;//Satisfação geral do usuário considerando beta calculado com tempo de simulação
    private Map<String, BigDecimal> satisfacaoGeralUso;//Satisfação geral do usuário considerando beta calculado com tempo de uso
    private Map<String, BigDecimal> consumoEnergiaTotalUsuario;//Consumo de energia em Joules, total do usuário
    private Map<String, BigDecimal> satisfacaoDesempenho;//Satisfação sobre desempenho, idêntica à utilizada no HOSEP
    private Map<String, Integer> tarefasPreemp;//Número de tarefas que sofreram preempção
    private Map<String, BigDecimal> consumoLocal;//Consumo da porção de cada usuário
    private BigDecimal consumoTotalSistema;//Consumo total do sistema
    private Map<String, BigDecimal> consumoMaxLocal;//Consumo máximo da porção de cada usuário, como se a porção ficasse ativa por completo durante toda a simulação
    private Map<String, BigDecimal> limitesConsumoTempoSim;//Limite de consumo em Joules, considerando o tempo total de simulação
    private Map<String, BigDecimal> limitesConsumoTempoUso;//Limite de consumo em Joules, considerando em o usuário teve tarefas no sistema
    private Map<String, BigDecimal> consumoLocalProprio;//Energia consumida na porção com tarefas do usuário proprietário
    private Map<String, BigDecimal> consumoLocalEstrangeiro;//Energia consumida na porção com tarefas de usuários não proprietários
    private Map<String, BigDecimal> energiaDespercicada;//Energia desperdiçada para cada usuário
    private Map<String, BigDecimal> alphaUsuarios;//Energia desperdiçada para cada usuário
    private Map<String, BigDecimal> betaTempoSim;//Energia desperdiçada para cada usuário
    private Map<String, BigDecimal> betaTempoUso;//Energia desperdiçada para cada usuário
    private Map<String, BigDecimal> tempoInicialExec;
    private Map<String, BigDecimal> tempoFinalExec;
    private Map<String, BigDecimal> turnaroundTime;
    private Double tempoSIM;

    //Historicos
    private List<Map<String, BigDecimal>> historicoConsumoTotalUsuario;
    private List<Map<String, BigDecimal>> historicoSatisfacaoGeralTempoSim;
    private List<Map<String, BigDecimal>> historicoSatisfacaoGeralTempoUso;
    private List<Map<String, BigDecimal>> historicoConsumoLocal;
    private List<Map<String, BigDecimal>> historicoSatisfacaoDesempenho;
    private List<Map<String, Integer>> historicoTarefasPreemp;
    private List<Map<String, BigDecimal>> historicoConsumoMaxLocal;
    private List<BigDecimal> historicoConsumoTotalSistema;
    private List<Map<String, BigDecimal>> historicoLimitesConsumoTempoSim;
    private List<Map<String, BigDecimal>> historicoLimitesConsumoTempoUso;
    private List<Map<String, BigDecimal>> historicoEnergiaDeperdicada;
    private List<Map<String, BigDecimal>> historicoAlpha;
    private List<Map<String, BigDecimal>> historicoBetaTempoSim;
    private List<Map<String, BigDecimal>> historicoBetaTempoUso;
    private List<Map<String, BigDecimal>> historicoConsumoLocalProprio;
    private List<Map<String, BigDecimal>> historicoConsumoLocalEstrangeiro;
    private List<Map<String, BigDecimal>> historicoTempoInicial;
    private List<Map<String, BigDecimal>> historicoTempoFinal;
    private List<Map<String, BigDecimal>> historicoTurnaroundTime;
    private List<Double> historicoTempoSim;
    
    public Metricas(List<String> usuarios) {

        this.numeroDeSimulacoes = 0;
        this.metricasGlobais = new MetricasGlobais();
        this.usuarios = usuarios;
        tempoMedioFilaComunicacao = 0;
        tempoMedioComunicacao = 0;
        tempoMedioFilaProcessamento = 0;
        tempoMedioProcessamento = 0;
        MflopsDesperdicio = 0;
        numTarefasCanceladas = 0;
        numTarefas = 0;
        tempoSIM = 0.0;

        //Maps
        satisfacaoGeralSim = new HashMap<>();
        satisfacaoGeralUso = new HashMap<>();
        consumoEnergiaTotalUsuario = new HashMap<>();
        consumoLocal = new HashMap<>();
        limitesConsumoTempoSim = new HashMap<>();
        limitesConsumoTempoUso = new HashMap<>();
        satisfacaoDesempenho = new HashMap<>();
        tarefasPreemp = new HashMap<>();
        consumoMaxLocal = new HashMap<>();
        consumoLocalEstrangeiro = new HashMap<>();
        consumoLocalProprio = new HashMap<>();
        energiaDespercicada = new HashMap<>();
        alphaUsuarios = new HashMap<>();
        betaTempoSim = new HashMap<>();
        betaTempoUso = new HashMap<>();
        tempoInicialExec = new HashMap<>();
        tempoFinalExec = new HashMap<>();
        turnaroundTime = new HashMap<>();

        //Historicos
        historicoSatisfacaoGeralTempoSim = new ArrayList<>();
        historicoSatisfacaoGeralTempoUso = new ArrayList<>();
        historicoConsumoTotalUsuario = new ArrayList<>();
        historicoConsumoLocal = new ArrayList<>();
        historicoSatisfacaoDesempenho = new ArrayList<>();
        historicoTarefasPreemp = new ArrayList<>();
        historicoConsumoMaxLocal = new ArrayList<>();
        historicoConsumoTotalSistema = new ArrayList<>();
        historicoLimitesConsumoTempoSim = new ArrayList<>();
        historicoLimitesConsumoTempoUso = new ArrayList<>();
        historicoConsumoLocalEstrangeiro = new ArrayList<>();
        historicoConsumoLocalProprio = new ArrayList<>();
        historicoEnergiaDeperdicada = new ArrayList<>();
        historicoAlpha = new ArrayList<>();
        historicoBetaTempoSim = new ArrayList<>();
        historicoBetaTempoUso = new ArrayList<>();
        historicoTempoInicial = new ArrayList<>();
        historicoTempoFinal = new ArrayList<>();
        historicoTurnaroundTime = new ArrayList<>();
        historicoTempoSim = new ArrayList<>();
    }

    public Metricas(RedeDeFilas redeDeFilas, double time, List<Tarefa> tarefas) {
        this.numeroDeSimulacoes = 1;
        this.metricasGlobais = new MetricasGlobais(redeDeFilas, time, tarefas);
        tarefasConcluidas = new HashMap<>();
        this.usuarios = redeDeFilas.getUsuarios();

        //Maps
        satisfacaoGeralSim = new HashMap<>();
        satisfacaoGeralUso = new HashMap<>();
        consumoEnergiaTotalUsuario = new HashMap<>();
        consumoLocal = new HashMap<>();
        satisfacaoDesempenho = new HashMap<>();
        consumoMaxLocal = new HashMap<>();
        limitesConsumoTempoSim = new HashMap<>();
        limitesConsumoTempoUso = new HashMap<>();
        tarefasPreemp = new HashMap<>();
        consumoLocalEstrangeiro = new HashMap<>();
        consumoLocalProprio = new HashMap<>();
        energiaDespercicada = new HashMap<>();
        alphaUsuarios = new HashMap<>();
        betaTempoSim = new HashMap<>();
        betaTempoUso = new HashMap<>();
        tempoInicialExec = new HashMap<>();
        tempoFinalExec = new HashMap<>();
        turnaroundTime = new HashMap<>();
        tempoSIM = 0.0;


        //Historicos
        historicoSatisfacaoGeralTempoSim = new ArrayList<>();
        historicoSatisfacaoGeralTempoUso = new ArrayList<>();
        historicoLimitesConsumoTempoSim = new ArrayList<>();
        historicoLimitesConsumoTempoUso = new ArrayList<>();
        historicoConsumoTotalUsuario = new ArrayList<>();
        historicoConsumoLocal = new ArrayList<>();
        historicoSatisfacaoDesempenho = new ArrayList<>();
        historicoTarefasPreemp = new ArrayList<>();
        historicoConsumoMaxLocal = new ArrayList<>();
        historicoConsumoTotalSistema = new ArrayList<>();
        historicoConsumoLocalEstrangeiro = new ArrayList<>();
        historicoConsumoLocalProprio = new ArrayList<>();
        historicoEnergiaDeperdicada = new ArrayList<>();
        historicoAlpha = new ArrayList<>();
        historicoBetaTempoSim = new ArrayList<>();
        historicoBetaTempoUso = new ArrayList<>();
        historicoTempoInicial = new ArrayList<>();
        historicoTempoFinal = new ArrayList<>();
        historicoTurnaroundTime = new ArrayList<>();
        historicoTempoSim = new ArrayList<>();
        consumoTotalSistema = BigDecimal.ZERO;

        for (String user : usuarios) {
            satisfacaoDesempenho.put(user, BigDecimal.ZERO);
            satisfacaoGeralSim.put(user, BigDecimal.ZERO);
            satisfacaoGeralUso.put(user, BigDecimal.ZERO);
            consumoEnergiaTotalUsuario.put(user, BigDecimal.ZERO);
            consumoLocal.put(user, BigDecimal.ZERO);
            consumoMaxLocal.put(user, BigDecimal.ZERO);
            limitesConsumoTempoSim.put(user, BigDecimal.ZERO);
            limitesConsumoTempoUso.put(user, BigDecimal.ZERO);
            energiaDespercicada.put(user, BigDecimal.ZERO);
            tarefasPreemp.put(user, 0);
            consumoLocalEstrangeiro.put(user, BigDecimal.ZERO);
            consumoLocalProprio.put(user, BigDecimal.ZERO);
            alphaUsuarios.put(user, BigDecimal.ZERO);
            betaTempoSim.put(user, BigDecimal.ZERO);
            betaTempoUso.put(user, BigDecimal.ZERO);
            tempoInicialExec.put(user, BigDecimal.ZERO);
            tempoFinalExec.put(user, BigDecimal.ZERO);
            turnaroundTime.put(user, BigDecimal.ZERO);
        }

        String propMaq;

        for (CS_Processamento maq : redeDeFilas.getMaquinas()) {

            propMaq = maq.getProprietario();

            consumoMaxLocal.put(propMaq, consumoMaxLocal.get(propMaq).add((BigDecimal.valueOf(maq.getConsumoEnergia()))));

            limitesConsumoTempoSim.put(propMaq, limitesConsumoTempoSim.get(propMaq).add((BigDecimal.valueOf(maq.getConsumoEnergia()))));

        }

        Double porcentLimite;//Limite de consumo em porcentagem do consumo total da porção do usuário

        for (String user : usuarios) {

            porcentLimite = redeDeFilas.getLimites().get(user) / 100;
            limitesConsumoTempoSim.put(user, limitesConsumoTempoSim.get(user).multiply(BigDecimal.valueOf(porcentLimite)));
            limitesConsumoTempoUso.put(user, limitesConsumoTempoSim.get(user));
        }

        getMetricaFilaTarefa(tarefas, redeDeFilas);
        getMetricaComunicacao(redeDeFilas);
        getMetricaProcessamento(redeDeFilas);

        //Historicos
        historicoSatisfacaoDesempenho.add(satisfacaoDesempenho);
        historicoTarefasPreemp.add(tarefasPreemp);
        historicoSatisfacaoGeralTempoSim.add(satisfacaoGeralSim);
        historicoSatisfacaoGeralTempoUso.add(satisfacaoGeralUso);
        historicoConsumoTotalUsuario.add(consumoEnergiaTotalUsuario);
        historicoConsumoLocal.add(consumoLocal);
        historicoConsumoMaxLocal.add(consumoMaxLocal);
        historicoConsumoTotalSistema.add(consumoTotalSistema);
        historicoLimitesConsumoTempoSim.add(limitesConsumoTempoSim);
        historicoLimitesConsumoTempoUso.add(limitesConsumoTempoUso);
        historicoConsumoLocalEstrangeiro.add(consumoLocalEstrangeiro);
        historicoConsumoLocalProprio.add(consumoLocalProprio);
        historicoEnergiaDeperdicada.add(energiaDespercicada);
        historicoAlpha.add(alphaUsuarios);
        historicoBetaTempoSim.add(betaTempoSim);
        historicoBetaTempoUso.add(betaTempoUso);
        historicoTempoInicial.add(tempoInicialExec);
        historicoTempoFinal.add(tempoFinalExec);
        historicoTurnaroundTime.add(turnaroundTime);
        historicoTempoSim.add(this.tempoSIM);
    }

    public Metricas(RedeDeFilasCloud redeDeFilas, double time, List<Tarefa> tarefas) {
        this.numeroDeSimulacoes = 1;
        this.metricasGlobais = new MetricasGlobais(redeDeFilas, time, tarefas);
        metricasSatisfacao = new HashMap<String, Double>();
        tarefasConcluidas = new HashMap<String, Integer>();
        this.usuarios = redeDeFilas.getUsuarios();
        for (String user : usuarios) {
            metricasSatisfacao.put(user, 0.0);
            tarefasConcluidas.put(user, 0);
        }
        getMetricaFilaTarefaCloud(tarefas, redeDeFilas);
        getMetricaComunicacao(redeDeFilas);
        getMetricaProcessamentoCloud(redeDeFilas);
        getMetricaAlocacao(redeDeFilas);
        getMetricaCusto(redeDeFilas);
    }

    public void addMetrica(Metricas metrica) {
        addMetricasGlobais(metrica.getMetricasGlobais());
        addMetricaFilaTarefa(metrica);
        addMetricaComunicacao(metrica.getMetricasComunicacao());
        addMetricaProcessamento(metrica.getMetricasProcessamento());
        this.numeroDeSimulacoes += metrica.numeroDeSimulacoes;

        addMetricaSatisfacaoGeralTempoSim(metrica.getMetricasSatisfacaoGeralTempoSim());
        addMetricaConsumo(metrica.getMetricasConsumo());
        this.historicoSatisfacaoGeralTempoUso.add(metrica.getMetricasSatisfacaoGeralTempoUso());
        this.historicoTarefasPreemp.add(metrica.getTarefasPreemp());
        this.historicoSatisfacaoDesempenho.add(metrica.getSatisfacaoDesempenho());
        this.historicoConsumoLocal.add(metrica.getConsumoLocal());
        this.historicoConsumoMaxLocal.add(metrica.getConsumoMaxLocal());
        this.historicoLimitesConsumoTempoSim.add(metrica.getLimitesTempoSim());
        this.historicoLimitesConsumoTempoUso.add(metrica.getLimitesTempoUso());
        this.historicoConsumoTotalSistema.add(metrica.getConsumoTotalSistema());
        this.historicoConsumoLocalEstrangeiro.add(metrica.getConsumoLocalEstrangeiro());
        this.historicoConsumoLocalProprio.add(metrica.getConsumoLocaProprio());
        this.historicoEnergiaDeperdicada.add(metrica.getEnergiaDesperdicada());
        this.historicoAlpha.add(metrica.getAplha());
        this.historicoBetaTempoSim.add(metrica.getBetaTempoSim());
        this.historicoBetaTempoUso.add(metrica.getBetaTempoUso());
        this.historicoTempoFinal.add(metrica.getTempoFinal());
        this.historicoTempoInicial.add(metrica.getTempoInicial());
        this.historicoTurnaroundTime.add(metrica.getTurnaroudTime());
        this.historicoTempoSim.add(metrica.getTEMPOSIM());
    }

    public RedeDeFilas getRedeDeFilas() {
        return redeDeFilas;
    }

    public void setRedeDeFilas(RedeDeFilas redeDeFilas) {
        this.redeDeFilas = redeDeFilas;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }

    public int getNumeroDeSimulacoes() {
        return numeroDeSimulacoes;
    }

    public MetricasGlobais getMetricasGlobais() {
        return metricasGlobais;
    }

    public List<String> getUsuarios() {
        return usuarios;
    }

    public Map<String, MetricasComunicacao> getMetricasComunicacao() {
        return metricasComunicacao;
    }

    public Map<String, MetricasProcessamento> getMetricasProcessamento() {
        return metricasProcessamento;
    }

    public Map<String, MetricasAlocacao> getMetricasAlocacao() {
        return metricasAlocacao;
    }

    public Map<String, MetricasCusto> getMetricasCusto() {
        return metricasCusto;
    }

    public Map<String, Double> getMetricasSatisfacao() {
        return metricasSatisfacao;
    }

    public Map<String, BigDecimal> getMetricasSatisfacaoGeralTempoSim() {
        return satisfacaoGeralSim;
    }
    
    public Map<String, BigDecimal> getMetricasSatisfacaoGeralTempoUso() {
        return satisfacaoGeralUso;
    }

    public Map<String, BigDecimal> getSatisfacaoDesempenho() {
        return satisfacaoDesempenho;
    }

    public Map<String, Integer> getTarefasPreemp() {
        return tarefasPreemp;
    }

    public double getTempoMedioFilaComunicacao() {
        return tempoMedioFilaComunicacao;
    }

    public double getTempoMedioComunicacao() {
        return tempoMedioComunicacao;
    }

    public double getTempoMedioFilaProcessamento() {
        return tempoMedioFilaProcessamento;
    }

    public double getTempoMedioProcessamento() {
        return tempoMedioProcessamento;
    }

    public double getMflopsDesperdicio() {
        return MflopsDesperdicio;
    }

    public int getNumTarefasCanceladas() {
        return numTarefasCanceladas;
    }

    public int getNumTarefas() {
        return numTarefas;
    }

    public void calculaMedia() {
        //Média das Metricas Globais
        metricasGlobais.setTempoSimulacao(metricasGlobais.getTempoSimulacao() / numeroDeSimulacoes);
        metricasGlobais.setSatisfacaoMedia(metricasGlobais.getSatisfacaoMedia() / numeroDeSimulacoes);
        metricasGlobais.setOciosidadeComputacao(metricasGlobais.getOciosidadeComputacao() / numeroDeSimulacoes);
        metricasGlobais.setOciosidadeComunicacao(metricasGlobais.getOciosidadeComunicacao() / numeroDeSimulacoes);
        metricasGlobais.setEficiencia(metricasGlobais.getEficiencia() / numeroDeSimulacoes);
        //Média das Metricas da rede de filas
        this.tempoMedioFilaComunicacao = this.tempoMedioFilaComunicacao / numeroDeSimulacoes;
        this.tempoMedioComunicacao = this.tempoMedioComunicacao / numeroDeSimulacoes;
        this.tempoMedioFilaProcessamento = this.tempoMedioFilaProcessamento / numeroDeSimulacoes;
        this.tempoMedioProcessamento = this.tempoMedioFilaProcessamento / numeroDeSimulacoes;
        this.MflopsDesperdicio = this.MflopsDesperdicio / numeroDeSimulacoes;
        this.numTarefasCanceladas = this.numTarefasCanceladas / numeroDeSimulacoes;
        //Média das Metricas de Comunicação
        for (Map.Entry<String, MetricasComunicacao> entry : metricasComunicacao.entrySet()) {
            String key = entry.getKey();
            MetricasComunicacao item = entry.getValue();
            item.setMbitsTransmitidos(item.getMbitsTransmitidos() / numeroDeSimulacoes);
            item.setSegundosDeTransmissao(item.getSegundosDeTransmissao() / numeroDeSimulacoes);
        }
        //Média das Metricas de Processamento
        for (Map.Entry<String, MetricasProcessamento> entry : metricasProcessamento.entrySet()) {
            String key = entry.getKey();
            MetricasProcessamento item = entry.getValue();
            item.setMflopsProcessados(item.getMFlopsProcessados() / numeroDeSimulacoes);
            item.setSegundosDeProcessamento(item.getSegundosDeProcessamento() / numeroDeSimulacoes);
        }
        
        System.out.println("Usuário \t SatisfaçãoGeralTempoSim"+"\t"+"SatisfaçãoGeralTempoUso"+"\t"+"SatisfaçãoDesempenho"+"\t"+"ConsumoTotal"+"\t"+"LimiteConsumoTempoSimulado"+"\t"+"LimiteConsumoTempoUso"+"\t"+"ConsumoLocal"+"\t"+"ConsumoLocalProprio"+"\t"+"ConsumoLocalEstrangeiro"+"\t"+"ConsumoMaxLocal"+"\t"+"TarefasPreemp"+"\t"+"Alpha"+"\t"+"BetaTempoSim"+"\t"+"BetaTempoUso"+"\t"+"Desperdicio"+"\t"+"ConsumoKJS"+"\t"+"TurnaroundTime"+"\t"+"TempoSIM");
        for (int j = 0; j < usuarios.size(); j++) {
            for (int i = 0; i < numeroDeSimulacoes; i++) {
                System.out.println(
                        usuarios.get(j) + "\t" 
                        + historicoSatisfacaoGeralTempoSim.get(i).get(usuarios.get(j)) + "\t"
                        + historicoSatisfacaoGeralTempoUso.get(i).get(usuarios.get(j)) + "\t"
                        + historicoSatisfacaoDesempenho.get(i).get(usuarios.get(j)) + "\t"
                        + String.format("%.2f", historicoConsumoTotalUsuario.get(i).get(usuarios.get(j)).doubleValue() / 1000000) + "\t"
                        + String.format("%.2f", historicoLimitesConsumoTempoSim.get(i).get(usuarios.get(j)).doubleValue() / 1000000) + "\t"
                        + String.format("%.2f", historicoLimitesConsumoTempoUso.get(i).get(usuarios.get(j)).doubleValue() / 1000000) + "\t"
                        + String.format("%.2f",(historicoConsumoLocal.get(i).get(usuarios.get(j)).divide(BigDecimal.valueOf(historicoTempoSim.get(i)),BigDecimal.ROUND_UP)).doubleValue() / 1000) + "\t"
                        + String.format("%.2f",(historicoConsumoLocalProprio.get(i).get(usuarios.get(j)).divide(BigDecimal.valueOf(historicoTempoSim.get(i)),BigDecimal.ROUND_UP)).doubleValue() / 1000) + "\t"
                        + String.format("%.2f",(historicoConsumoLocalEstrangeiro.get(i).get(usuarios.get(j)).divide(BigDecimal.valueOf(historicoTempoSim.get(i)),BigDecimal.ROUND_UP)).doubleValue() / 1000) + "\t"
                        + String.format("%.2f", historicoConsumoMaxLocal.get(i).get(usuarios.get(j)).doubleValue() / 1000000) + "\t"
                        + historicoTarefasPreemp.get(i).get(usuarios.get(j)) + "\t"
                        + historicoAlpha.get(i).get(usuarios.get(j)) + "\t"
                        + historicoBetaTempoSim.get(i).get(usuarios.get(j)) + "\t"
                        + historicoBetaTempoUso.get(i).get(usuarios.get(j)) + "\t"
                        + String.format("%.2f", historicoEnergiaDeperdicada.get(i).get(usuarios.get(j)).doubleValue() / 1000000) + "\t"
                        + String.format("%.2f",(historicoConsumoTotalUsuario.get(i).get(usuarios.get(j)).divide(BigDecimal.valueOf(historicoTempoSim.get(i)),BigDecimal.ROUND_UP)).doubleValue() / 1000) + "\t"
                        + historicoTurnaroundTime.get(i).get(usuarios.get(j)) + "\t"
                        + historicoTempoSim.get(i)
                );
            }
            System.out.println();
        }
        System.out.println("\n\n"); 

    }

    private void getMetricaFilaTarefa(List<Tarefa> tarefas, RedeDeFilas rede) {
        this.tempoMedioFilaComunicacao = 0;
        this.tempoMedioComunicacao = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento = 0;
        this.numTarefasCanceladas = 0;
        this.MflopsDesperdicio = 0;
        this.numTarefas = 0;
        String propTar;
        String propMaq;

        Double mediaPoder = 0.0;
        for (CS_Processamento maq : rede.getMaquinas()) {
            mediaPoder += maq.getPoderComputacional();
        }
        mediaPoder = mediaPoder / rede.getMaquinas().size();
        
        BigDecimal satis;

        for (String user : usuarios) {
            tarefasPreemp.put(user, 0);
            tarefasConcluidas.put(user, 0);
        }

        for (Tarefa tar : tarefas) {
            if (tar.getEstado() == Tarefa.CONCLUIDO) {
                
                tempoMedioFilaComunicacao += tar.getMetricas().getTempoEsperaComu();
                tempoMedioComunicacao += tar.getMetricas().getTempoComunicacao();
                tempoMedioFilaProcessamento = tar.getMetricas().getTempoEsperaProc();
                tempoMedioProcessamento = tar.getMetricas().getTempoProcessamento();
                numTarefas++;

                propTar = tar.getProprietario();
                tarefasConcluidas.put(propTar, tarefasConcluidas.get(propTar) + 1);
                
                BigDecimal suij;//Satisfação em relação a desempenho do usuário i, sobre a tarefa j submetida por i

                BigDecimal tempoInicio = BigDecimal.valueOf(tar.getTimeCriacao());//Instante de tempo de submissão da tarefa
                BigDecimal tempoFinal = BigDecimal.valueOf(tar.getTempoFinal().get(tar.getTempoFinal().size() - 1));//Instante de tempo em que a terafa é concluída
                BigDecimal intervaloReal = tempoFinal.subtract(tempoInicio);//Intervalo entre término e submissão
                BigDecimal intervaloIdeal = BigDecimal.valueOf(tar.getTamProcessamento() / mediaPoder);//Tempo de execução esperado pelo usuário, em que não há espera nem preempção, com a tarefa executando em uma máquina média do sistema

                suij = (intervaloIdeal.divide(intervaloReal, 2, RoundingMode.DOWN)).multiply(BigDecimal.valueOf(100.0));//Dividir os intervalos e multiplicar o resultado por 100

                if (satisfacaoGeralSim.putIfAbsent(propTar, suij) != null) {//Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no corpo no if

                    satis = satisfacaoGeralSim.get(propTar);
                    satisfacaoGeralSim.put(propTar, satis.add(suij));
                    satisfacaoGeralUso.put(propTar, satisfacaoGeralSim.get(propTar));
                }

                if (satisfacaoDesempenho.putIfAbsent(propTar, suij) != null) {//Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no corpo no if

                    satis = satisfacaoDesempenho.get(propTar);
                    satisfacaoDesempenho.put(propTar, satis.add(suij));
                }

                int i;
                BigDecimal consumo;
                for (i = 0; i < tar.getHistoricoProcessamento().size(); i++) {

                    consumo = BigDecimal.valueOf(tar.getHistoricoProcessamento().get(i).getConsumoEnergia() * (tar.getTempoFinal().get(i) - tar.getTempoInicial().get(i)));//Consumo da máquina corrente no histórico multiplicado pelo tempo em que permaneceu na máquina
                    propMaq = tar.getHistoricoProcessamento().get(i).getProprietario();
                    //System.out.println(propMaq);

                    consumoTotalSistema = consumoTotalSistema.add(consumo);
                    //////////////////////////////////////////////////////////
                    BigDecimal temp = consumoEnergiaTotalUsuario.get(propTar);
                    temp = temp.add(consumo);
                    consumoEnergiaTotalUsuario.put(propTar, temp);
                    //////////////////////////////////////////////////////////
                    consumoLocal.put(propMaq, consumoLocal.get(propMaq).add(consumo));
                    //////////////////////////////////////////////////////////
                    if (propTar.equals(propMaq)) {
                        consumoLocalProprio.put(propMaq, consumoLocalProprio.get(propMaq).add(consumo));
                    } else {
                        consumoLocalEstrangeiro.put(propMaq, consumoLocalEstrangeiro.get(propMaq).add(consumo));
                    }
                }

                if (tar.getHistoricoProcessamento().size() > 1) {//Se a tarefa passou por mais de uma máquina, houve preempção dela
                    if (tarefasPreemp.putIfAbsent(propTar, 1) != null) {//Faz a primeira incersão testando se o hashmap está vazio e, se não estiver, entra no corpo no if
                        tarefasPreemp.put(propTar, tarefasPreemp.get(propTar) + 1);
                    }
                    
                    Double tempoDesperdicio, mflopsProcessado;
                    mflopsProcessado = 0.0;
                    //Calcular Desperdício
                    for(i = 0; i < tar.getHistoricoProcessamento().size(); i++){
                        mflopsProcessado += (tar.getTempoFinal().get(i)-tar.getTempoInicial().get(i))*tar.getHistoricoProcessamento().get(i).getPoderComputacional();
                        tempoDesperdicio = (mflopsProcessado/tar.getHistoricoProcessamento().get(i).getPoderComputacional());//%300;//HardCodded
                        energiaDespercicada.put(propTar, energiaDespercicada.get(propTar).add(BigDecimal.valueOf(tempoDesperdicio*tar.getHistoricoProcessamento().get(i).getConsumoEnergia())));
                    }
                }
            } else if (tar.getEstado() == Tarefa.CANCELADO) {
                MflopsDesperdicio += tar.getTamProcessamento() * tar.getMflopsProcessado();
                numTarefasCanceladas++;
            }
            //Rever, se for informação pertinente adicionar nas métricas da tarefa ou CS_Processamento e calcula durante a simulação
            CS_Processamento temp = (CS_Processamento) tar.getLocalProcessamento();
            if (temp != null) {
                for (int i = 0; i < tar.getTempoInicial().size(); i++) {
                    temp.setTempoProcessamento(tar.getTempoInicial().get(i), tar.getTempoFinal().get(i));
                }
            }
        }

        tempoSIM = -1.0;
        for (int k = 0; k < tarefas.size(); k++) {
            if (tempoSIM == -1.0) {

                tempoSIM = tarefas.get(k).getTempoFinal().get(tarefas.get(k).getTempoFinal().size() - 1);
            } else {

                if (tarefas.get(k).getTempoFinal().get(tarefas.get(k).getTempoFinal().size() - 1) > tempoSIM) {

                    tempoSIM = tarefas.get(k).getTempoFinal().get(tarefas.get(k).getTempoFinal().size() - 1);
                }
            }
        }
        
        String user;
        Double inicio,fim;
        for (int i = 0; i < usuarios.size(); i++) {
            user = usuarios.get(i);

            satis = satisfacaoGeralSim.get(user).divide(BigDecimal.valueOf(tarefasConcluidas.get(user)), 2, RoundingMode.DOWN);
            BigDecimal consMaxLocal = consumoMaxLocal.get(user);
            BigDecimal limiteConsSim = limitesConsumoTempoSim.get(user);
            BigDecimal limiteConsUso = limitesConsumoTempoUso.get(user);

            satisfacaoGeralSim.put(user, satis);
            satisfacaoGeralUso.put(user, satis);
            satisfacaoDesempenho.put(user, satis);
            consumoMaxLocal.put(user, consMaxLocal.multiply(BigDecimal.valueOf(metricasGlobais.getTempoSimulacao())));
            limitesConsumoTempoSim.put(user, limiteConsSim.multiply(BigDecimal.valueOf(metricasGlobais.getTempoSimulacao())));
            
            inicio = -1.0;
            fim = -1.0;
            for(int j = 0; j < tarefas.size(); j++){
                
                if( tarefas.get(j).getProprietario().equals(user)){
                    
                    if( inicio == -1.0 || fim == -1.0 ){
                        
                        inicio = tarefas.get(j).getTempoInicial().get(0);
                        fim = tarefas.get(j).getTempoFinal().get(tarefas.get(j).getTempoFinal().size() -1);
                    } else{
                        
                        if( tarefas.get(j).getTempoInicial().get(0) < inicio ){
                            
                            inicio = tarefas.get(j).getTempoInicial().get(0);
                        }
                        if( tarefas.get(j).getTempoFinal().get(tarefas.get(j).getTempoFinal().size() -1) > fim ){
                            
                            fim = tarefas.get(j).getTempoFinal().get(tarefas.get(j).getTempoFinal().size() -1);
                        }
                    }
                    
                    turnaroundTime.put(user,turnaroundTime.get(user).add(BigDecimal.valueOf(tarefas.get(j).getTempoFinal().get(tarefas.get(j).getTempoFinal().size() -1)-tarefas.get(j).getTimeCriacao())));
                }
            }
            
            limitesConsumoTempoUso.put(user, limiteConsUso.multiply(BigDecimal.valueOf(fim - inicio)));
            tempoFinalExec.put(user, BigDecimal.valueOf(fim));
            tempoInicialExec.put(user, BigDecimal.valueOf(inicio));
            
            BigDecimal alpha = consumoMaxLocal.get(user).divide(consumoLocal.get(user), 2, RoundingMode.DOWN);
            BigDecimal betaSim = (((consumoEnergiaTotalUsuario.get(user).negate()).add(consumoTotalSistema)).divide(limitesConsumoTempoSim.get(user), 2, RoundingMode.DOWN)).add(BigDecimal.ONE);
            
            alphaUsuarios.put(user, alpha);
            betaTempoSim.put(user, betaSim);
            
            BigDecimal satisGeralSim = (satisfacaoGeralSim.get(user)).multiply((alpha.multiply(betaSim)));

            satisfacaoGeralSim.put(user, satisGeralSim);
            
            BigDecimal betaUso = (((consumoEnergiaTotalUsuario.get(user).negate()).add(consumoTotalSistema)).divide(limitesConsumoTempoUso.get(user), 2, RoundingMode.DOWN)).add(BigDecimal.ONE);
            betaTempoUso.put(user, betaUso);
            
            BigDecimal satisGeralUso = (satisfacaoGeralUso.get(user)).multiply((alpha.multiply(betaUso)));
            
            satisfacaoGeralUso.put(user, satisGeralUso);
            turnaroundTime.put(user,turnaroundTime.get(user).divide(BigDecimal.valueOf(tarefasConcluidas.get(user)),BigDecimal.ROUND_UP));
        }

        tempoMedioFilaComunicacao = tempoMedioFilaComunicacao / numTarefas;
        tempoMedioComunicacao = tempoMedioComunicacao / numTarefas;
        tempoMedioFilaProcessamento = tempoMedioFilaProcessamento / numTarefas;
        tempoMedioProcessamento = tempoMedioProcessamento / numTarefas;
        
    }

    private void getMetricaFilaTarefaCloud(List<Tarefa> tarefas, RedeDeFilasCloud rede) {
        this.tempoMedioFilaComunicacao = 0;
        this.tempoMedioComunicacao = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento = 0;
        this.numTarefasCanceladas = 0;
        this.MflopsDesperdicio = 0;
        this.numTarefas = 0;

        Double mediaPoder = 0.0;
        for (int i = 0; i < rede.getVMs().size(); i++) {
            mediaPoder += rede.getVMs().get(i).getPoderComputacional();
        }
        mediaPoder = mediaPoder / rede.getVMs().size();
        for (Tarefa no : tarefas) {
            if (no.getEstado() == Tarefa.CONCLUIDO) {

                Double suij;
                CS_Processamento vm = (CS_Processamento) no.getHistoricoProcessamento().get(0);
                suij = (no.getTamProcessamento() / mediaPoder / (no.getTempoFinal().get(no.getTempoFinal().size() - 1) - no.getTimeCriacao())) * (100);
                metricasSatisfacao.put(no.getProprietario(), suij + metricasSatisfacao.get(no.getProprietario()));
                tarefasConcluidas.put(no.getProprietario(), 1 + tarefasConcluidas.get(no.getProprietario()));

            }
            if (no.getEstado() == Tarefa.CONCLUIDO) {
                tempoMedioFilaComunicacao += no.getMetricas().getTempoEsperaComu();
                tempoMedioComunicacao += no.getMetricas().getTempoComunicacao();
                tempoMedioFilaProcessamento = no.getMetricas().getTempoEsperaProc();
                tempoMedioProcessamento = no.getMetricas().getTempoProcessamento();
                numTarefas++;
            } else if (no.getEstado() == Tarefa.CANCELADO) {
                MflopsDesperdicio += no.getTamProcessamento() * no.getMflopsProcessado();
                numTarefasCanceladas++;
            }
            //Rever, se for informação pertinente adicionar nas métricas da tarefa ou CS_Processamento e calcula durante a simulação
            CS_Processamento temp = (CS_Processamento) no.getLocalProcessamento();
            if (temp != null) {
                for (int i = 0; i < no.getTempoInicial().size(); i++) {
                    temp.setTempoProcessamento(no.getTempoInicial().get(i), no.getTempoFinal().get(i));
                }
            }
        }

        for (Map.Entry<String, Double> entry : metricasSatisfacao.entrySet()) {

            String string = entry.getKey();
            entry.setValue(entry.getValue() / tarefasConcluidas.get(string));

        }

        tempoMedioFilaComunicacao = tempoMedioFilaComunicacao / numTarefas;
        tempoMedioComunicacao = tempoMedioComunicacao / numTarefas;
        tempoMedioFilaProcessamento = tempoMedioFilaProcessamento / numTarefas;
        tempoMedioProcessamento = tempoMedioProcessamento / numTarefas;
    }

    private void getMetricaProcessamento(RedeDeFilas redeDeFilas) {
        metricasProcessamento = new HashMap<String, MetricasProcessamento>();
        for (CS_Processamento maq : redeDeFilas.getMestres()) {
            metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
        for (CS_Processamento maq : redeDeFilas.getMaquinas()) {
            metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
    }

    private void getMetricaProcessamentoCloud(RedeDeFilasCloud redeDeFilas) {
        metricasProcessamento = new HashMap<String, MetricasProcessamento>();
        for (CS_Processamento maq : redeDeFilas.getMestres()) {
            metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
        for (CS_Processamento maq : redeDeFilas.getVMs()) {
            metricasProcessamento.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetrica());
        }
    }

    private void getMetricaComunicacao(RedeDeFilas redeDeFilas) {
        metricasComunicacao = new HashMap<String, MetricasComunicacao>();
        for (CS_Comunicacao link : redeDeFilas.getInternets()) {
            metricasComunicacao.put(link.getId(), link.getMetrica());
        }
        for (CS_Comunicacao link : redeDeFilas.getLinks()) {
            metricasComunicacao.put(link.getId(), link.getMetrica());
        }
    }

    private void addMetricasGlobais(MetricasGlobais global) {
        metricasGlobais.setTempoSimulacao(metricasGlobais.getTempoSimulacao() + global.getTempoSimulacao());
        metricasGlobais.setSatisfacaoMedia(metricasGlobais.getSatisfacaoMedia() + global.getSatisfacaoMedia());
        metricasGlobais.setOciosidadeComputacao(metricasGlobais.getOciosidadeComputacao() + global.getOciosidadeComputacao());
        metricasGlobais.setOciosidadeComunicacao(metricasGlobais.getOciosidadeComunicacao() + global.getOciosidadeComunicacao());
        metricasGlobais.setEficiencia(metricasGlobais.getEficiencia() + global.getEficiencia());
    }

    private void addMetricaComunicacao(Map<String, MetricasComunicacao> metricasComunicacao) {
        if (numeroDeSimulacoes == 0) {
            this.metricasComunicacao = metricasComunicacao;
        } else {
            for (Map.Entry<String, MetricasComunicacao> entry : metricasComunicacao.entrySet()) {
                String key = entry.getKey();
                MetricasComunicacao item = entry.getValue();
                MetricasComunicacao base = this.metricasComunicacao.get(key);
                base.incMbitsTransmitidos(item.getMbitsTransmitidos());
                base.incSegundosDeTransmissao(item.getSegundosDeTransmissao());
            }
        }
    }

    private void addMetricaProcessamento(Map<String, MetricasProcessamento> metricasProcessamento) {
        if (numeroDeSimulacoes == 0) {
            this.metricasProcessamento = metricasProcessamento;
        } else {
            for (Map.Entry<String, MetricasProcessamento> entry : metricasProcessamento.entrySet()) {
                String key = entry.getKey();
                MetricasProcessamento item = entry.getValue();
                MetricasProcessamento base = this.metricasProcessamento.get(key);
                base.incMflopsProcessados(item.getMFlopsProcessados());
                base.incSegundosDeProcessamento(item.getSegundosDeProcessamento());
            }
        }
    }

    private void addMetricaFilaTarefa(Metricas metrica) {
        this.tempoMedioFilaComunicacao += metrica.tempoMedioFilaComunicacao;
        this.tempoMedioComunicacao += metrica.tempoMedioComunicacao;
        this.tempoMedioFilaProcessamento += metrica.tempoMedioFilaProcessamento;
        this.tempoMedioProcessamento += metrica.tempoMedioFilaProcessamento;
        this.MflopsDesperdicio += metrica.MflopsDesperdicio;
        this.numTarefasCanceladas += metrica.numTarefasCanceladas;
    }

    private void addMetricaSatisfacao(Map<String, Double> metricasSatisfacao, Map<String, Integer> tarefasConcluidasUser) {
        if (numeroDeSimulacoes == 0) {
            this.metricasSatisfacao = metricasSatisfacao;
            this.tarefasConcluidas = tarefasConcluidasUser;
        } else {
            for (Map.Entry<String, Double> entry : this.metricasSatisfacao.entrySet()) {

                String string = entry.getKey();
                entry.setValue(entry.getValue() + metricasSatisfacao.get(string));
                System.out.println("\n " + string + " " + metricasSatisfacao.get(string) + "");

            }
        }
    }

    private void getMetricaAlocacao(RedeDeFilasCloud redeDeFilas) {
        metricasAlocacao = new HashMap<String, MetricasAlocacao>();
        //percorre as máquinas recolhendo as métricas de alocação
        for (CS_MaquinaCloud maq : redeDeFilas.getMaquinasCloud()) {
            metricasAlocacao.put(maq.getId() + maq.getnumeroMaquina(), maq.getMetricaAloc());
        }
        //insere nas métricas as VMs que não foram alocadas
        MetricasAlocacao mtRej = new MetricasAlocacao("Rejected");
        for (CS_Processamento mst : redeDeFilas.getMestres()) {
            CS_VMM aux = (CS_VMM) mst;
            for (int i = 0; i < aux.getAlocadorVM().getVMsRejeitadas().size(); i++) {
                mtRej.incVMsAlocadas();
            }
        }
        metricasAlocacao.put("Rejected", mtRej);
    }

    private void getMetricaCusto(RedeDeFilasCloud redeDeFilas) {
        metricasCusto = new HashMap<String, MetricasCusto>();
        //percorre as vms inserindo as métricas de custo
        for (CS_VirtualMac vm : redeDeFilas.getVMs()) {
            if (vm.getStatus() == CS_VirtualMac.DESTRUIDA) {
                metricasCusto.put(vm.getId() + vm.getnumeroMaquina(), vm.getMetricaCusto());
            }
        }
    }

    private void addMetricaSatisfacaoGeralTempoSim(Map<String, BigDecimal> metricasSatisfacao) {
        this.historicoSatisfacaoGeralTempoSim.add(metricasSatisfacao);
    }

    public Map<String, BigDecimal> getMetricasConsumo() {
        return this.consumoEnergiaTotalUsuario;
    }

    private void addMetricaConsumo(Map<String, BigDecimal> metricasConsumo) {
        this.historicoConsumoTotalUsuario.add(metricasConsumo);
    }

    private Map<String, BigDecimal> getConsumoLocal() {
        return this.consumoLocal;
    }

    private Map<String, BigDecimal> getConsumoMaxLocal() {
        return this.consumoMaxLocal;
    }

    private Map<String, BigDecimal> getLimitesTempoSim() {
        return this.limitesConsumoTempoSim;
    }
    
    private Map<String, BigDecimal> getLimitesTempoUso() {
        return this.limitesConsumoTempoUso;
    }

    private BigDecimal getConsumoTotalSistema() {
        return this.consumoTotalSistema;
    }

    private Map<String, BigDecimal> getConsumoLocalEstrangeiro() {
        return this.consumoLocalEstrangeiro;
    }

    private Map<String, BigDecimal> getConsumoLocaProprio() {
        return this.consumoLocalProprio;
    }

    private Map<String, BigDecimal> getEnergiaDesperdicada() {
        return this.energiaDespercicada;
    }

    private Map<String, BigDecimal> getBetaTempoSim() {
        return this.betaTempoSim;
    }

    private Map<String, BigDecimal> getBetaTempoUso() {
        return this.betaTempoUso;
    }

    private Map<String, BigDecimal> getAplha() {
        return this.alphaUsuarios;
    }

    private Map<String, BigDecimal> getTempoFinal() {
        return this.tempoFinalExec; 
    }

    private Map<String, BigDecimal> getTempoInicial() {
        return this.tempoInicialExec;
    }

    private Map<String, BigDecimal> getTurnaroudTime() {
        return this.turnaroundTime;
    }

    private Double getTEMPOSIM() {
        return this.tempoSIM;
    }
}
