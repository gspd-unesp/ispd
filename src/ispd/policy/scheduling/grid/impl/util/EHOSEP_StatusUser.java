package ispd.policy.scheduling.grid.impl.util;

//Classe para dados de estado dos usuários
public class EHOSEP_StatusUser implements Comparable<EHOSEP_StatusUser> {

    private final String user;//Nome do usuario;
    private final int indexUser;//Índice do usuário;
    private final double perfShare;//Desempenho total das máquinas do
    private int demanda;//Número de tarefas na fila
    private int ownerShare;//Número de máquinas do usuario
    // usuário
    private double powerShare;//Consumo de energia total das máquinas do
    // usuário
    private int servedNum;//Número de máquinas que atendem ao usuário
    private double servedPerf;//Desempenho total que atende ao usuário
    private double servedPower;//Consumo de energia total que atende ao
    // usuario
    private double limiteConsumo;//Limite de consumo definido pelo usuario;
    private double relacaoEficienciaSistemaPorcao;//Nova métrica para
    // decisão de preempção

    public EHOSEP_StatusUser(final String user, final int indexUser,
                             final double perfShare) {
        this.user = user;
        this.indexUser = indexUser;
        this.demanda = 0;
        this.perfShare = perfShare;
        this.powerShare = 0.0;
        this.servedNum = 0;
        this.servedPerf = 0.0;
        this.servedPower = 0.0;
        this.limiteConsumo = 0.0;
    }

    public void calculaRelacaoEficienciaEficienciaSisPor(
            final Double poderSis, final Double consumoSis) {
        this.relacaoEficienciaSistemaPorcao =
                ((poderSis / consumoSis) / (this.perfShare / this.powerShare));
    }

    public void addDemanda() {
        this.demanda++;
    }

    public void rmDemanda() {
        this.demanda--;
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

    public void addServedPower(final Double power) {
        this.servedPower += power;
    }

    public void rmServedPower(final Double power) {
        this.servedPower -= power;
    }

    public String getNome() {
        return this.user;
    }

    public int getIndexUser() {
        return this.indexUser;
    }

    public int getDemanda() {
        return this.demanda;
    }

    public Double getLimite() {
        return this.limiteConsumo;
    }

    public void setLimite(final Double lim) {
        this.limiteConsumo = lim;
    }

    public int getOwnerShare() {
        return this.ownerShare;
    }

    public int getServedNum() {
        return this.servedNum;
    }

    public double getServedPower() {
        return this.servedPower;
    }

    public double getRelacaoEficienciSisPor() {
        return this.relacaoEficienciaSistemaPorcao;
    }

    //Comparador para ordenação
    @Override
    public int compareTo(final EHOSEP_StatusUser o) {

        if (((this.servedPerf - this.perfShare) / this.perfShare) < ((o.getServedPerf() - o.getPerfShare()) / o.getPerfShare())) {
            return -1;
        }

        if (((this.servedPerf - this.perfShare) / this.perfShare) > ((o.getServedPerf() - o.getPerfShare()) / o.getPerfShare())) {
            return 1;
        }

        //Se as defasagens/excessos relativos forem iguais, os ifs
        // anteriores não são executados
        if (this.perfShare > o.getPerfShare()) {
            return -1;
        } else {
            if (this.perfShare < o.getPerfShare()) {
                return 1;
            } else {
                if (this.powerShare >= o.getPowerShare()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    public double getPerfShare() {
        return this.perfShare;
    }

    public double getPowerShare() {
        return this.powerShare;
    }

    public void setPowerShare(final Double power) {
        this.powerShare = power;
    }

    public double getServedPerf() {
        return this.servedPerf;
    }
}