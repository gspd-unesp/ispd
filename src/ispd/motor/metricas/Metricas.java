/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author denison
 */
public class Metricas implements Serializable {

    private int numeroDeSimulacoes;
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
    }

    public Metricas(RedeDeFilas redeDeFilas, double time, List<Tarefa> tarefas) {
        this.numeroDeSimulacoes = 1;
        this.metricasGlobais = new MetricasGlobais(redeDeFilas, time, tarefas);
        metricasSatisfacao = new HashMap<String, Double>();
        tarefasConcluidas = new HashMap<String, Integer>();
        this.usuarios = redeDeFilas.getUsuarios();
        for (String user : usuarios) {
            metricasSatisfacao.put(user, 0.0);
            tarefasConcluidas.put(user, 0);
        }
        getMetricaFilaTarefa(tarefas, redeDeFilas);
        getMetricaComunicacao(redeDeFilas);
        getMetricaProcessamento(redeDeFilas);

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
        addMetricaSatisfacao(metrica.getMetricasSatisfacao(), metrica.tarefasConcluidas);
        this.numeroDeSimulacoes += metrica.numeroDeSimulacoes;
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

        for (Map.Entry<String, Double> entry : this.metricasSatisfacao.entrySet()) {

            entry.setValue(entry.getValue() / numeroDeSimulacoes);

        }
    }

    private void getMetricaFilaTarefa(List<Tarefa> tarefas, RedeDeFilas rede) {
        this.tempoMedioFilaComunicacao = 0;
        this.tempoMedioComunicacao = 0;
        this.tempoMedioFilaProcessamento = 0;
        this.tempoMedioProcessamento = 0;
        this.numTarefasCanceladas = 0;
        this.MflopsDesperdicio = 0;
        this.numTarefas = 0;

        Double mediaPoder = 0.0;
        for (int i = 0; i < rede.getMaquinas().size(); i++) {
            mediaPoder += rede.getMaquinas().get(i).getPoderComputacional();
        }
        mediaPoder = mediaPoder / rede.getMaquinas().size();
        for (Tarefa no : tarefas) {
            if (no.getEstado() == Tarefa.CONCLUIDO) {

                Double suij;
                CS_Processamento maq = (CS_Processamento) no.getHistoricoProcessamento().get(0);
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
}
