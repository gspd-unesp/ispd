package ispd.policy.scheduling.grid.impl.util;

//Classe para dados de estado dos usuários
public class EHOSEP_StatusUser implements Comparable<EHOSEP_StatusUser> {

    protected final String user;//Nome do usuario;
    protected final double perfShare;//Desempenho total das máquinas do
    protected int demanda;//Número de tarefas na fila
    // usuário
    protected double powerShare;//Consumo de energia total das máquinas do
    // usuário
    protected int servedNum;//Número de máquinas que atendem ao usuário
    protected double servedPerf;//Desempenho total que atende ao usuário
    protected double servedPower;//Consumo de energia total que atende ao
    // usuario
    protected double limiteConsumo;//Limite de consumo definido pelo usuario;
    protected double relacaoEficienciaSistemaPorcao;//Nova métrica para
    // decisão de preempção

    public EHOSEP_StatusUser(final String user, final double perfShare) {
        this.user = user;
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

    public int getDemanda() {
        return this.demanda;
    }

    public Double getLimite() {
        return this.limiteConsumo;
    }

    public void setLimite(final Double lim) {
        this.limiteConsumo = lim;
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
