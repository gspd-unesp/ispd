package ispd.policy.scheduling.grid.impl.util;

import ispd.motor.filas.servidores.CS_Processamento;

import java.util.List;

public class OSEP_StatusUser extends EHOSEP_StatusUser {
    private int indexTarefaMax;//Índice da maior tarefa na fila
    private int indexTarefaMin;//Índice da menor tarefa na fila
    private int ownerShare;//Número de máquinas do usuario

    public OSEP_StatusUser(final String user,
                           final double perfShare,
                           final List<CS_Processamento> slaves) {
        super(user, perfShare);
        this.demanda = 0;
        this.indexTarefaMax = -1;
        this.indexTarefaMin = -1;
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

    public String getUser() {
        return this.user;
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
}
