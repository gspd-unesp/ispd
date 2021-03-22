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
import ispd.motor.filas.servidores.implementacao.CS_VMM;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author denison_usuario
 */
public class MetricasGlobais implements Serializable {

    private double tempoSimulacao;
    private double satisfacaoMedia;
    private double ociosidadeComputacao;
    private double ociosidadeComunicacao;
    private double eficiencia;
    private double custoTotalDisco;
    private double custoTotalProc;
    private double custoTotalMem;
    private int totaldeVMs;
    private int numVMsRejeitadas;
   

    public MetricasGlobais(double tempoSimulacao, double satisfacaoMedia, double ociosidadeComputacao, double ociosidadeComunicacao, double eficiencia, double custoTotalDisco, double custoTotalProc, double custoTotalMem, int total) {
        this.tempoSimulacao = tempoSimulacao;
        this.satisfacaoMedia = satisfacaoMedia;
        this.ociosidadeComputacao = ociosidadeComputacao;
        this.ociosidadeComunicacao = ociosidadeComunicacao;
        this.eficiencia = eficiencia;
        this.custoTotalDisco = custoTotalDisco;
        this.custoTotalProc = custoTotalProc;
        this.custoTotalMem = custoTotalMem;
        this.total = total;
    }
    private int total;

    public MetricasGlobais(RedeDeFilas redeDeFilas, double tempoSimulacao, List<Tarefa> tarefas) {
        this.tempoSimulacao = tempoSimulacao;
        this.satisfacaoMedia = 100;
        this.ociosidadeComputacao = getOciosidadeComputacao(redeDeFilas);
        this.ociosidadeComunicacao = getOciosidadeComunicacao(redeDeFilas);
        this.eficiencia = getEficiencia(tarefas);
        this.total = 0;
    }

    public MetricasGlobais(RedeDeFilasCloud redeDeFilas, double tempoSimulacao, List<Tarefa> tarefas) {
        this.tempoSimulacao = tempoSimulacao;
        this.satisfacaoMedia = 100;
        this.ociosidadeComputacao = getOciosidadeComputacaoCloud(redeDeFilas);
        this.ociosidadeComunicacao = getOciosidadeComunicacao(redeDeFilas);
        this.eficiencia = getEficiencia(tarefas);
        this.custoTotalDisco = getCustoTotalDisco(redeDeFilas);
        this.custoTotalMem = getCustoTotalMem(redeDeFilas);
        this.custoTotalProc = getCustoTotalProc(redeDeFilas);
        this.totaldeVMs = getTotalVMs(redeDeFilas);
        this.numVMsRejeitadas = getNumVMsRejeitadas(redeDeFilas);
        this.total = 0;
    }

    public MetricasGlobais() {
        this.tempoSimulacao = 0;
        this.satisfacaoMedia = 0;
        this.ociosidadeComputacao = 0;
        this.ociosidadeComunicacao = 0;
        this.eficiencia = 0;
        this.total = 0;
        this.custoTotalDisco = 0;
        this.custoTotalMem = 0;
        this.custoTotalProc = 0;
    }

    //calcula o custo total de uso de disco na nuvem
    public double getCustoTotalDisco(RedeDeFilasCloud redeDeFilas) {
        for (CS_VirtualMac auxVM : redeDeFilas.getVMs()) {
            if (auxVM.getStatus() == CS_VirtualMac.DESTRUIDA) {
                custoTotalDisco = custoTotalDisco + auxVM.getMetricaCusto().getCustoDisco();
            }
        }
        return custoTotalDisco;
    }

    public double getCustoTotalDisco() {
        return custoTotalDisco;
    }

    public void setCustoTotalDisco(double custoTotalDisco) {
        this.custoTotalDisco = custoTotalDisco;
    }

    //calcula o custo total de uso de processamento da nuvem
    public double getCustoTotalProc(RedeDeFilasCloud redeDeFilas) {
        for (CS_VirtualMac auxVM : redeDeFilas.getVMs()) {
            if (auxVM.getStatus() == CS_VirtualMac.DESTRUIDA) {
                custoTotalProc = custoTotalProc + auxVM.getMetricaCusto().getCustoProc();
            }
        }
        return custoTotalProc;
    }

    public double getCustoTotalProc() {
        return custoTotalProc;
    }

    public void setCustoTotalProc(double custoTotalProc) {
        this.custoTotalProc = custoTotalProc;
    }

    //calcula custo total de uso de memória pela nuvem
    public double getCustoTotalMem(RedeDeFilasCloud redeDeFilas) {
        for (CS_VirtualMac auxVM : redeDeFilas.getVMs()) {
            if (auxVM.getStatus() == CS_VirtualMac.DESTRUIDA) {
                custoTotalMem = custoTotalMem + auxVM.getMetricaCusto().getCustoMem();
            }
        }
        return custoTotalMem;
    }

    public double getCustoTotalMem() {
        return custoTotalMem;
    }

    public void setCustoTotalMem(double custoTotalMem) {
        this.custoTotalMem = custoTotalMem;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public double getOciosidadeComputacao() {
        return ociosidadeComputacao;
    }

    public double getOciosidadeComunicacao() {
        return ociosidadeComunicacao;
    }

    public double getSatisfacaoMedia() {
        return satisfacaoMedia;
    }

    public double getTempoSimulacao() {
        return tempoSimulacao;
    }

    //calcula número de VMs rejeitadas
    private int getNumVMsRejeitadas(RedeDeFilasCloud redeDeFilas) {
        int totalRejeitadas = 0;
        for (CS_Processamento aux : redeDeFilas.getMestres()) {
            CS_VMM auxVMM = (CS_VMM) aux;
            totalRejeitadas += auxVMM.getAlocadorVM().getVMsRejeitadas().size();
        }
        return totalRejeitadas;
    }

    //calcula o total de vms do modelo
    private int getTotalVMs(RedeDeFilasCloud redeDeFilas) {
        int total = 0;
        for (CS_Processamento aux : redeDeFilas.getMestres()) {
            CS_VMM auxVMM = (CS_VMM) aux;
            total += auxVMM.getEscalonador().getEscravos().size();
        }
        return total;
    }

    private double getOciosidadeComputacao(RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (CS_Processamento maquina : redeDeFilas.getMaquinas()) {
            double aux = maquina.getMetrica().getSegundosDeProcessamento();
            aux = (this.getTempoSimulacao() - aux);
            tempoLivreMedio += aux;//tempo livre
            aux = maquina.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getMaquinas().size();
        return (tempoLivreMedio * 100) / getTempoSimulacao();
    }

    private double getOciosidadeComputacaoCloud(RedeDeFilasCloud redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (CS_Processamento auxVM : redeDeFilas.getVMs()) {
            CS_VirtualMac vm = (CS_VirtualMac) auxVM;
            if (vm.getStatus() == CS_VirtualMac.ALOCADA) {
                double aux = auxVM.getMetrica().getSegundosDeProcessamento();
                aux = (this.getTempoSimulacao() - aux);
                tempoLivreMedio += aux;//tempo livre
                aux = auxVM.getOcupacao() * aux;
                tempoLivreMedio -= aux;
            }
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getVMs().size();
        return (tempoLivreMedio * 100) / getTempoSimulacao();
    }

    private double getOciosidadeComunicacao(RedeDeFilas redeDeFilas) {
        double tempoLivreMedio = 0.0;
        for (CS_Comunicacao link : redeDeFilas.getLinks()) {
            double aux = link.getMetrica().getSegundosDeTransmissao();
            aux = (this.getTempoSimulacao() - aux);
            tempoLivreMedio += aux; //tempo livre
            aux = link.getOcupacao() * aux;
            tempoLivreMedio -= aux;
        }
        tempoLivreMedio = tempoLivreMedio / redeDeFilas.getLinks().size();
        return (tempoLivreMedio * 100) / getTempoSimulacao();
    }

    private double getEficiencia(List<Tarefa> tarefas) {
        double somaEfic = 0;
        for (Tarefa tar : tarefas) {
            somaEfic += tar.getMetricas().getEficiencia();
        }
        return somaEfic / tarefas.size();
        /*
         double tempoUtil = 0.0;
         double tempoMedio = 0.0;
         for (CS_Processamento maquina : redeDeFilas.getMaquinas()) {
         double aux = maquina.getMetrica().getSegundosDeProcessamento();
         aux = (this.getTempoSimulacao() - aux);//tempo livre
         aux = maquina.getOcupacao() * aux;//tempo processando sem ser tarefa
         tempoUtil = aux + maquina.getMetrica().getSegundosDeProcessamento();
         tempoMedio += tempoUtil / this.getTempoSimulacao();
         }
         tempoMedio = tempoMedio / redeDeFilas.getMaquinas().size();
         return tempoMedio; 
         */
    }

    public void setTempoSimulacao(double tempoSimulacao) {
        this.tempoSimulacao = tempoSimulacao;
    }

    public void setSatisfacaoMedia(double satisfacaoMedia) {
        this.satisfacaoMedia = satisfacaoMedia;
    }

    public void setOciosidadeComputacao(double ociosidadeComputacao) {
        this.ociosidadeComputacao = ociosidadeComputacao;
    }

    public void setOciosidadeComunicacao(double ociosidadeComunicacao) {
        this.ociosidadeComunicacao = ociosidadeComunicacao;
    }

    public void setEficiencia(double eficiencia) {
        this.eficiencia = eficiencia;
    }

    public int getNumVMsAlocadas() {
        return totaldeVMs - numVMsRejeitadas;
    }

    public int getNumVMsRejeitadas() {
        return numVMsRejeitadas;
    }

    public int getTotaldeVMs() {
        return totaldeVMs;
    }

    public void add(MetricasGlobais global) {
        tempoSimulacao += global.getTempoSimulacao();
        satisfacaoMedia += global.getSatisfacaoMedia();
        ociosidadeComputacao += global.getOciosidadeComputacao();
        ociosidadeComunicacao += global.getOciosidadeComunicacao();
        eficiencia += global.getEficiencia();
        total++;
    }

    @Override
    public String toString() {
        String texto = "\t\tSimulation Results:\n\n";
        texto += String.format("\tTotal Simulated Time = %g \n", tempoSimulacao / total);
        texto += String.format("\tSatisfaction = %g %%\n", satisfacaoMedia / total);
        texto += String.format("\tIdleness of processing resources = %g %%\n", ociosidadeComputacao / total);
        texto += String.format("\tIdleness of communication resources = %g %%\n", ociosidadeComunicacao / total);
        texto += String.format("\tEfficiency = %g %%\n", eficiencia / total);
        if (eficiencia / total > 70.0) {
            texto += "\tEfficiency GOOD\n ";
        } else if (eficiencia / total > 40.0) {
            texto += "\tEfficiency MEDIA\n ";
        } else {
            texto += "\tEfficiency BAD\n ";
        }
        texto += "\t\tCost Results:\n\n";
        texto += String.format("\tCost Total de Processing = %g %%\n", custoTotalProc);
        texto += String.format("\tCost Total de Memory = %g %%\n", custoTotalMem);
        texto += String.format("\tCost Total de Disk = %g %%\n", custoTotalDisco);
        texto += "\t\tVM Alocation results:";
        texto += String.format("\tTotal of VMs alocated = %g %%\n", (totaldeVMs - numVMsRejeitadas));
        texto += String.format("\tTotal of VMs rejected = %g %%\n", numVMsRejeitadas);
        return texto;
    }

}
