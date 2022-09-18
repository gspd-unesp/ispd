package ispd.alocacaoVM;

import ispd.motor.Simulation;
import ispd.motor.filas.servidores.implementacao.CS_VirtualMac;

/**
 * Interface that has methods implemented only in a Master node,
 * these methods will be used by the schedulers.
 */
public interface VMM {
    int ENQUANTO_HOUVER_VMS = 1;
    int DOISCASOS = 3;

    void enviarVM(CS_VirtualMac vm);

    void executarAlocacao();

    void setSimulacaoAlloc(Simulation simulacao);
}