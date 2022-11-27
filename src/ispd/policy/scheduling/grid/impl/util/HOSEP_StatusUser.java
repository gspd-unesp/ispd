package ispd.policy.scheduling.grid.impl.util;

//Classe para dados de estado dos usuários
public class HOSEP_StatusUser extends EHOSEP_StatusUser {

//    private final String user;//Nome do usuario;
//    private final double perfShare;//Desempenho total das máquinas do
//    private int demanda;//Número de tarefas na fila
//    // usuário
//    private double powerShare;//Consumo de energia total das máquinas do
//    // usuário
//    private int servedNum;//Número de máquinas que atendem ao usuário
//    private double servedPerf;//Desempenho total que atende ao usuário
//    private double servedPower;//Consumo de energia total que atende ao
//    // usuario
//    private double limiteConsumo;//Limite de consumo definido pelo usuario;
    // decisão de preempção

    public HOSEP_StatusUser(final String user,
                            final double perfShare) {
        super(user, perfShare);
//        this.user = user;
        this.demanda = 0;
//        this.perfShare = perfShare;
        this.powerShare = 0.0;
        this.servedNum = 0;
        this.servedPerf = 0.0;
        this.servedPower = 0.0;
        this.limiteConsumo = 0.0;
    }
}
