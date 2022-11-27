package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

public class OSEP_StatusUser {
    private final String user;//Nome do usuario;
    private final double perfShare;//Desempenho total das máquinas do
    private final double servedPower;//Consumo de energia total que
    private int demanda;//Número de tarefas na fila
    private int indexTarefaMax;//Índice da maior tarefa na fila
    private int indexTarefaMin;//Índice da menor tarefa na fila
    private int ownerShare;//Número de máquinas do usuario
    // usuário
    private double powerShare;//Consumo de energia total das máquinas do
    // usuário
    private int servedNum;//Número de máquinas que atendem ao usuário
    private double servedPerf;//Desempenho total que atende ao usuário
    // atende ao usuario

    public OSEP_StatusUser(final String user,
                           final double perfShare,
                           final List<CS_Processamento> slaves) {
        this.user = user;
        this.demanda = 0;
        this.indexTarefaMax = -1;
        this.indexTarefaMin = -1;
        this.perfShare = perfShare;
        this.powerShare = 0.0;
        this.servedNum = 0;
        this.servedPerf = 0.0;
        this.servedPower = 0.0;

        this.ownerShare = 0;
        int i;
        int j = 0;
        for (i = 0; i < slaves.size(); i++) {
            if (slaves.get(i).getProprietario().equals(user)) {
                j++;
                //this.eficienciaMedia += escravos.get(i)
                // .getPoderComputacional()/escravos.get(i)
                // .getConsumoEnergia();
            }
        }
        this.ownerShare = j;
        //this.eficienciaMedia = this.eficienciaMedia/j;


    }

    public void addDemanda() {
        this.demanda++;
    }

    public void rmDemanda() {
        this.demanda--;
    }

    public void setTarefaMinima(final int index) {
        this.indexTarefaMin = index;
    }

    public void setTarefaMaxima(final int index) {
        this.indexTarefaMax = index;
    }

    public void addShare() {
        this.ownerShare++;
    }

    public void addPowerShare(final Double power) {
        this.powerShare += power;
    }

    public void addServedNum() {
        this.servedNum++;
    }

    public void rmServedNum() {
        this.servedNum--;
    }

    public void addServedPerf(final Double perf) {
        this.servedPerf += perf;
    }

    public void rmServedPerf(final Double perf) {
        this.servedPerf -= perf;
    }

    public String getUser() {
        return this.user;
    }

    public int getDemanda() {
        return this.demanda;
    }

    public int getIndexTarefaMax() {
        return this.indexTarefaMax;
    }

    public int getIndexTarefaMin() {
        return this.indexTarefaMin;
    }

    public int getOwnerShare() {
        return this.ownerShare;
    }

    public double getPerfShare() {
        return this.perfShare;
    }

    public double getPowerShare() {
        return this.powerShare;
    }

    public int getServedNum() {
        return this.servedNum;
    }

    public double getServedPerf() {
        return this.servedPerf;
    }

    public double getServedPower() {
        return this.servedPower;
    }
}
