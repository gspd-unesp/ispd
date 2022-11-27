package ispd.policy.scheduling.grid.impl.util;

//Classe para dados de estado dos usuários
public class HOSEP_StatusUser implements Comparable<HOSEP_StatusUser> {

    private final String user;//Nome do usuario;
    private final int indexUser;//Índice do usuário;
    private final double perfShare;//Desempenho total das máquinas do
    private int demanda;//Número de tarefas na fila
    // usuário
    private double powerShare;//Consumo de energia total das máquinas do
    // usuário
    private int servedNum;//Número de máquinas que atendem ao usuário
    private double servedPerf;//Desempenho total que atende ao usuário
    private double servedPower;//Consumo de energia total que atende ao
    // usuario
    private double limiteConsumo;//Limite de consumo definido pelo usuario;
    // decisão de preempção

    public HOSEP_StatusUser(final String user, final int indexUser,
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

    public static int getOwnerShare() {
        //Número de máquinas do usuario
        return 0;
    }

    public void calculaRelacaoEficienciaEficienciaSisPor(final Double poderSis, final Double consumoSis) {
        //Nova métrica para
        final double relacaoEficienciaSistemaPorcao =
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

    public double getPowerShare() {
        return this.powerShare;
    }

    public void setPowerShare(final Double power) {
        this.powerShare = power;
    }

    public int getServedNum() {
        return this.servedNum;
    }

    public double getServedPower() {
        return this.servedPower;
    }

    //Comparador para ordenação
    @Override
    public int compareTo(final HOSEP_StatusUser o) {
        if (((this.servedPerf - this.perfShare) / this.perfShare) < ((o.getServedPerf() - o.getPerfShare()) / o.getPerfShare())) {
            return -1;
        }
        if (((this.servedPerf - this.perfShare) / this.perfShare) > ((o.getServedPerf() - o.getPerfShare()) / o.getPerfShare())) {
            return 1;
        }
        if (this.perfShare >= o.getPerfShare()) {
            return -1;
        } else {
            return 1;
        }
    }

    public double getPerfShare() {
        return this.perfShare;
    }

    public double getServedPerf() {
        return this.servedPerf;
    }
}
